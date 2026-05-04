package com.starforge.farmdoc.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tflite.java.TfLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class ClassificationResult(
    val diseaseName: String,
    val confidence: Float
)

class DiseaseClassifier(private val context: Context) {

    private var interpreter: InterpreterApi? = null
    private var labels: List<String> = emptyList()

    private val MODEL_NAME = "mobilenetv2_finetuned.tflite"
    private val LABELS_NAME = "labels.txt"

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (interpreter != null) return@withContext

        // Load the labels from assets
        labels = context.assets.open(LABELS_NAME).bufferedReader().readLines()

        // Initialize Play Services TFLite (Downloads the latest TFLite runtime to support modern TF models)
        TfLite.initialize(context).await()

        // Load the model from assets
        val modelBuffer = loadModelFile(context, MODEL_NAME)
        val options = InterpreterApi.Options()

        // Explicitly tell the API to use the Play Services runtime instead of the standalone one
        options.setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)

        interpreter = InterpreterApi.create(modelBuffer, options)
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun classifyImage(bitmap: Bitmap): ClassificationResult = withContext(Dispatchers.Default) {
        // Ensure initialized
        if (interpreter == null) {
            initialize()
        }

        // 1. Process the input Image for MobileNetV2 (224x224)
        // We MUST use NormalizeOp(0f, 1f) to force the TensorImage to cast the UINT8 bitmap pixels into FLOAT32.
        // We leave the values in the [0, 255] range because the Python model already has 'preprocess_input' baked into the Keras graph!
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 1f))
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Prepare the output buffer (1 row, 10 columns for the 10 classes)
        val probabilityBuffer = Array(1) { FloatArray(labels.size) }

        // 3. Run Inference on the neural network
        interpreter?.run(tensorImage.buffer, probabilityBuffer)

        // 4. Find the highest probability prediction
        val probabilities = probabilityBuffer[0]
        var maxIndex = 0
        var maxConfidence = probabilities[0]

        for (i in probabilities.indices) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxIndex = i
            }
        }

        // 5. Clean up the label formatting (e.g. "Tomato___Early_blight" -> "Early blight")
        val rawLabel = labels[maxIndex]
        val cleanLabel = rawLabel.replace("Tomato___", "").replace("_", " ")

        return@withContext ClassificationResult(
            diseaseName = cleanLabel,
            confidence = maxConfidence
        )
    }

    fun close() {
        interpreter?.close()
    }
}
