package com.starforge.farmdoc.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.starforge.farmdoc.R
import com.starforge.farmdoc.ml.DiseaseClassifier
import kotlinx.coroutines.launch
import java.io.InputStream

class ResultsFragment : Fragment() {

    private lateinit var classifier: DiseaseClassifier

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        val imgResult = view.findViewById<ImageView>(R.id.img_result_leaf)
        val tvDisease = view.findViewById<TextView>(R.id.tv_result_disease)
        val tvConfidence = view.findViewById<TextView>(R.id.tv_result_confidence)

        // Initialize the AI classifier using the fragment's context
        classifier = DiseaseClassifier(requireContext())

        arguments?.getString("imageUri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            imgResult.setImageURI(uri)

            // Set initial analyzing state
            tvDisease.text = "Analyzing..."
            tvConfidence.text = "Please wait"

            // Run the ML classification in a background coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Convert the URI to a Bitmap
                    val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
                        // Pass the real bitmap into the TFLite model!
                        val result = classifier.classifyImage(bitmap)

                        // Save the result to the local Room Database
                        val scanEntity = com.starforge.farmdoc.db.ScanEntity(
                            imageUri = uriString,
                            diseaseName = result.diseaseName,
                            confidence = result.confidence
                        )
                        com.starforge.farmdoc.db.AppDatabase.getDatabase(requireContext())
                            .scanDao().insertScan(scanEntity)

                        // Update the UI with the final result
                        tvDisease.text = result.diseaseName
                        tvConfidence.text = String.format("Confidence: %.1f%%", result.confidence * 100)
                    } else {
                        tvDisease.text = "Error"
                        tvConfidence.text = "Failed to load image"
                    }
                } catch (e: Exception) {
                    tvDisease.text = "Error"
                    tvConfidence.text = e.localizedMessage
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the TFLite interpreter when the view is destroyed
        if (::classifier.isInitialized) {
            classifier.close()
        }
    }
}
