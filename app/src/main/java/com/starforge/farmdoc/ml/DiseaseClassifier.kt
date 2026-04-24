package com.starforge.farmdoc.ml

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ClassificationResult(
    val diseaseName: String,
    val confidence: Float
)

class DiseaseClassifier {

    // These represent the common tomato classes from the PlantVillage dataset
    private val mockClasses = listOf(
        "Healthy",
        "Early Blight",
        "Late Blight",
        "Septoria Leaf Spot",
        "Tomato Yellow Leaf Curl Virus",
        "Bacterial Spot",
        "Target Spot"
    )

    /**
     * Mocks the process of running inference on a TFLite model.
     * In the real Phase 3 implementation, you will:
     * 1. Load your trained .tflite model from assets
     * 2. Resize the bitmap to 224x224 and normalize pixels
     * 3. Run it through the org.tensorflow.lite.Interpreter
     * 4. Map the highest probability to your labels.txt
     */
    suspend fun classifyImage(bitmap: Bitmap?): ClassificationResult {
        // Simulate the processing time of a deep learning model (1 to 2.5 seconds)
        delay(Random.nextLong(1000, 2500))

        // Return a random mock result to test the UI flow
        val randomDisease = mockClasses.random()
        val randomConfidence = Random.nextFloat() * (0.99f - 0.70f) + 0.70f // Random between 70% and 99%

        return ClassificationResult(
            diseaseName = randomDisease,
            confidence = randomConfidence
        )
    }
}
