package com.example.balkangamehubapp.network

import com.example.balkangamehubapp.model.Post
import com.example.balkangamehubapp.model.Category
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 🔥 BRZA LISTA POSTOVA — VRAĆA SAMO FEATURED IMAGE
    @GET("wp-json/wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 20,
        @Query("_embed") embed: String = "wp:featuredmedia" // VAŽNO: vraća embedded samo za sliku
    ): List<Post>

    // 🔥 BRZI POSTOVI PO KATEGORIJI
    @GET("wp-json/wp/v2/posts")
    suspend fun getPostsByCategory(
        @Query("categories") categoryId: Int,
        @Query("per_page") perPage: Int = 50,
        @Query("_embed") embed: String = "wp:featuredmedia"
    ): List<Post>

    // ✔ kategorije (20 ili 50 komada)
    @GET("wp-json/wp/v2/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 20
    ): List<Category>

    // 🔥 FULL POST — koristi se u DetailsActivity
    @GET("wp-json/wp/v2/posts/{id}")
    suspend fun getPostById(
        @Path("id") postId: Int,
        @Query("_embed") embed: String = "1" // full embedded → featured media + galerije + content
    ): Post
}
