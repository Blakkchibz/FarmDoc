package com.starforge.farmdoc.ui

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

class ResultsFragment : Fragment() {

    // Instantiate our mock classifier
    private val classifier = DiseaseClassifier()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        val imgResult = view.findViewById<ImageView>(R.id.img_result_leaf)
        val tvDisease = view.findViewById<TextView>(R.id.tv_result_disease)
        val tvConfidence = view.findViewById<TextView>(R.id.tv_result_confidence)

        arguments?.getString("imageUri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            imgResult.setImageURI(uri)

            // Set initial analyzing state
            tvDisease.text = "Analyzing..."
            tvConfidence.text = "Please wait"

            // Run the mock ML classification in a background coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                // In the real implementation, we would convert the URI to a 224x224 Bitmap here
                val result = classifier.classifyImage(bitmap = null)

                // Update the UI with the mock results
                tvDisease.text = result.diseaseName
                tvConfidence.text = String.format("Confidence: %.1f%%", result.confidence * 100)
            }
        }

        return view
    }
}
