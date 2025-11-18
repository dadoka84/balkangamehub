package com.example.balkangamehubapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.balkangamehubapp.model.CachedPost

@Dao
interface PostDao {

    @Query("SELECT * FROM cached_posts ORDER BY date DESC")
    suspend fun getAll(): List<CachedPost>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<CachedPost>)

    @Query("DELETE FROM cached_posts")
    suspend fun clearAll()
}
