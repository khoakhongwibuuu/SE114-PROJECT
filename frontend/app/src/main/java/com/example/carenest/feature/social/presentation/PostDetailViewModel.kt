package com.example.carenest.feature.social.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import com.example.carenest.feature.social.domain.model.ReactionType

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CommentMutationState {
    object Idle : CommentMutationState
    object Loading : CommentMutationState
    data class Success(val comment: Comment) : CommentMutationState
    data class Error(val message: String) : CommentMutationState
}

class PostDetailViewModel(
    private val repository: SocialRepository,
    private val postId: Long,
    private val parentCommentId: Long? = null
) : ViewModel() {
    val commentsFlow: Flow<PagingData<Comment>> = repository
        .getPostComments(postId = postId, parentCommentId = parentCommentId)
        .cachedIn(viewModelScope)

    private val _mutationState = MutableStateFlow<CommentMutationState>(CommentMutationState.Idle)
    val mutationState: StateFlow<CommentMutationState> = _mutationState.asStateFlow()

    fun createComment(content: String) {
        viewModelScope.launch {
            _mutationState.value = CommentMutationState.Loading
            repository.createComment(postId = postId, content = content, parentCommentId = null)
                .onSuccess { comment ->
                    _mutationState.value = CommentMutationState.Success(comment)
                }
                .onFailure { error ->
                    _mutationState.value = CommentMutationState.Error(error.localizedMessage ?: "Khong the gui binh luan")
                }
        }
    }

    fun createReply(parentCommentId: Long, content: String) {
        viewModelScope.launch {
            _mutationState.value = CommentMutationState.Loading
            repository.createComment(postId = postId, content = content, parentCommentId = parentCommentId)
                .onSuccess { comment ->
                    _mutationState.value = CommentMutationState.Success(comment)
                }
                .onFailure { error ->
                    _mutationState.value = CommentMutationState.Error(error.localizedMessage ?: "Khong the gui cau tra loi")
                }
        }
    }

    fun clearMutationState() {
        _mutationState.value = CommentMutationState.Idle
    }

    fun reactToPost() {
        viewModelScope.launch {
            repository.reactToPost(postId, ReactionType.LIKE)
        }
    }
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
