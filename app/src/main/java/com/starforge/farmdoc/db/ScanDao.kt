package com.starforge.farmdoc.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    // Flow automatically emits updates whenever the database changes
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanEntity>>

    @Insert
    suspend fun insertScan(scan: ScanEntity)

    @Delete
    suspend fun deleteScan(scan: ScanEntity)
}
