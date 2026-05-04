package com.starforge.farmdoc.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.starforge.farmdoc.R
import com.starforge.farmdoc.db.ScanEntity
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var scans: List<ScanEntity> = emptyList()

    fun setScans(newScans: List<ScanEntity>) {
        this.scans = newScans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val scan = scans[position]
        holder.bind(scan)
    }

    override fun getItemCount(): Int = scans.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgThumbnail: ImageView = itemView.findViewById(R.id.img_scan_thumbnail)
        private val tvDisease: TextView = itemView.findViewById(R.id.tv_disease_name)
        private val tvConfidence: TextView = itemView.findViewById(R.id.tv_confidence)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(scan: ScanEntity) {
            tvDisease.text = scan.diseaseName
            tvConfidence.text = String.format("%.1f%%", scan.confidence * 100)

            // Format timestamp
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvDate.text = sdf.format(Date(scan.timestamp))

            try {
                val uri = Uri.parse(scan.imageUri)
                val inputStream = itemView.context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                imgThumbnail.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Ignore if URI cannot be resolved or permission lost
            }
        }
    }
}
