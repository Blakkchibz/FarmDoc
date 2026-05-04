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
    private var finalDisease: String = "Analyzing..."
    private var finalConfidence: String = "Please wait"

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

        val uriString = arguments?.getString("imageUri")
        if (uriString != null) {
            try {
                imgResult.setImageURI(Uri.parse(uriString))
            } catch (e: SecurityException) {
                // The temporary permission to read this URI from the Gallery has expired
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (savedInstanceState != null) {
            // Restore from backstack (prevents re-running AI and re-logging to database)
            finalDisease = savedInstanceState.getString("diseaseName", "") ?: ""
            finalConfidence = savedInstanceState.getString("confidence", "") ?: ""
            tvDisease.text = finalDisease
            tvConfidence.text = finalConfidence
        } else if (uriString != null) {
            // First time loading: run the AI!
            tvDisease.text = finalDisease
            tvConfidence.text = finalConfidence

            // Run the ML classification in a background coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Convert the URI to a Bitmap
                    val inputStream: InputStream? = requireContext().contentResolver.openInputStream(Uri.parse(uriString))
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

                        finalDisease = result.diseaseName
                        finalConfidence = String.format("Confidence: %.1f%%", result.confidence * 100)

                        // Update the UI with the final result
                        tvDisease.text = finalDisease
                        tvConfidence.text = finalConfidence
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("diseaseName", finalDisease)
        outState.putString("confidence", finalConfidence)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the TFLite interpreter when the view is destroyed
        if (::classifier.isInitialized) {
            classifier.close()
        }
    }
}
