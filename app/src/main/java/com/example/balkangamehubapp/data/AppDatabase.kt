package com.example.balkangamehubapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.balkangamehubapp.model.CachedPost

@Database(entities = [CachedPost::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
