package com.example.balkangamehubapp.repository

import com.example.balkangamehubapp.data.PostDao
import com.example.balkangamehubapp.model.CachedPost
import com.example.balkangamehubapp.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.balkangamehubapp.model.authorName // ⭐ VAŽNO: IMPORT

class PostRepository(
    private val api: ApiService,
    private val dao: PostDao
) {

    fun getPosts(preload: Boolean = true): Flow<List<CachedPost>> = flow {

        // Emit lokalnih odmah
        emit(dao.getAll())

        if (preload) {
            try {
                val remote = api.getPosts()

                val mapped = remote.map { post ->

                    val parsedDate = post.dateGmt ?: post.date ?: ""

                    CachedPost(
                        id = post.id,
                        title = post.title.rendered,
                        content = post.excerpt?.rendered ?: "",
                        date = parsedDate,
                        imageUrl = post.embedded?.media?.firstOrNull()?.bestImageUrl,
                        authorName = post.authorName // ✔️ sad radi
                    )
                }

                dao.clearAll()
                dao.insertAll(mapped)

                emit(mapped)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getPostsByCategory(categoryId: Int): List<CachedPost> {
        return try {
            val remote = api.getPostsByCategory(categoryId)

            remote.map { post ->
                val parsedDate = post.dateGmt ?: post.date ?: ""

                CachedPost(
                    id = post.id,
                    title = post.title.rendered,
                    content = post.excerpt?.rendered ?: "",
                    date = parsedDate,
                    imageUrl = post.embedded?.media?.firstOrNull()?.bestImageUrl,
                    authorName = post.authorName // ✔️ popravka p->post
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
