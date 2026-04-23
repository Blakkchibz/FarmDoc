package com.starforge.farmdoc.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.starforge.farmdoc.R

class ResultsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        val imgResult = view.findViewById<ImageView>(R.id.img_result_leaf)

        arguments?.getString("imageUri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            imgResult.setImageURI(uri)
        }

        return view
    }
}
