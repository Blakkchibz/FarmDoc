package com.starforge.farmdoc.ml

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
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

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val MODEL_NAME = "mobilenetv2_finetuned.tflite"
    private val LABELS_NAME = "labels.txt"

    init {
        // Load the labels from assets
        labels = context.assets.open(LABELS_NAME).bufferedReader().readLines()

        // Load the model from assets
        val modelBuffer = loadModelFile(context, MODEL_NAME)
        val options = Interpreter.Options()
        interpreter = Interpreter(modelBuffer, options)
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
        // 1. Process the input Image for MobileNetV2 (224x224, pixels between -1 and 1)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
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
