package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val hour: Int, // 0..23
    val steps: Int,
    val durationMinutes: Int = 0,
    val calories: Float = 0f
)
