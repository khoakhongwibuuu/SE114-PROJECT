package com.example.carenest.feature.social.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.domain.repository.SocialRepository
import com.example.carenest.feature.social.domain.model.ReactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SocialFeedViewModel(
    private val repository: SocialRepository,
    private val groupId: Long
) : ViewModel() {
    val postsFlow: Flow<PagingData<Post>> = repository
        .getGroupPosts(groupId = groupId)
        .cachedIn(viewModelScope)

    fun reactToPost(postId: Long) {
        viewModelScope.launch {
            repository.reactToPost(postId, ReactionType.LIKE)
        }
    }
}

class SocialFeedViewModelFactory(
    private val repository: SocialRepository,
    private val groupId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialFeedViewModel(repository = repository, groupId = groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
