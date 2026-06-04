package com.example.carenest.feature.social.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow

class PostDetailViewModel(
    private val repository: SocialRepository,
    private val postId: Long,
    private val parentCommentId: Long? = null
) : ViewModel() {
    val commentsFlow: Flow<PagingData<Comment>> = repository
        .getPostComments(postId = postId, parentCommentId = parentCommentId)
        .cachedIn(viewModelScope)
}

class PostDetailViewModelFactory(
    private val repository: SocialRepository,
    private val postId: Long,
    private val parentCommentId: Long? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostDetailViewModel(
                repository = repository,
                postId = postId,
                parentCommentId = parentCommentId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
