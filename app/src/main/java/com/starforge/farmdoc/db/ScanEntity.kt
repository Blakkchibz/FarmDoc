package com.starforge.farmdoc.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val diseaseName: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
