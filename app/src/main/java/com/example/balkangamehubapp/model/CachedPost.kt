package com.example.balkangamehubapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_posts")
data class CachedPost(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val imageUrl: String?,
    val authorName: String = "Balkan Game Hub Team" // ⭐ DEFAULT
)
