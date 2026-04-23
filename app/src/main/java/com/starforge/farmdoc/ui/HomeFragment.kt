package com.starforge.farmdoc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.starforge.farmdoc.R
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bundle = Bundle().apply {
                putString("imageUri", uri.toString())
            }
            findNavController().navigate(R.id.nav_results, bundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btnTakePhoto = view.findViewById<MaterialButton>(R.id.btn_take_photo)
        val btnChooseGallery = view.findViewById<MaterialButton>(R.id.btn_choose_gallery)

        btnTakePhoto.setOnClickListener {
            findNavController().navigate(R.id.nav_camera)
        }

        btnChooseGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        return view
    }
}
