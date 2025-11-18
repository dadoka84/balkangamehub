package com.example.balkangamehubapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.balkangamehubapp.model.CachedPost
import com.example.balkangamehubapp.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel(private val repo: PostRepository) : ViewModel() {

    private val _posts = MutableStateFlow<List<CachedPost>>(emptyList())
    val posts: StateFlow<List<CachedPost>> = _posts

    fun loadPosts() {
        viewModelScope.launch {
            repo.getPosts(preload = true).collect { list ->
                _posts.value = list
            }
        }
    }
}
