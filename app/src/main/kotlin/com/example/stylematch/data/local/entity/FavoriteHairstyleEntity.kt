package com.example.stylematch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_hairstyles")
data class FavoriteHairstyleEntity(
    @PrimaryKey val hairstyleName: String,
    val description: String,
    val mainReason: String,
    val confidenceAtRecommendation: Float,
    val faceShapeAtRecommendation: String, // e.g., "OVAL (FEMALE)"
    val timestamp: Long,
    val imageUrl: String? = null // <<<--- NUEVO CAMPO ---<<<
)