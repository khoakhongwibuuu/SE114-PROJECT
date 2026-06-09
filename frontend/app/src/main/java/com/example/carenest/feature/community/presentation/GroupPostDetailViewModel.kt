package com.example.carenest.feature.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.GroupPostComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GroupPostTab(val label: String) {
    APPROVED("Bài viết"),
    MY_POSTS("Bài của tôi"),
    PENDING("Chờ duyệt")
}

data class GroupPostDetailState(
    val activeTab: GroupPostTab = GroupPostTab.APPROVED,
    val isModerator: Boolean = false,
    val approvedPosts: List<GroupPost> = emptyList(),
    val myPosts: List<GroupPost> = emptyList(),
    val pendingPosts: List<GroupPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Comment Sheet State
    val isCommentSheetVisible: Boolean = false,
    val selectedPostIdForComments: Long? = null,
    val commentsList: List<GroupPostComment> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val commentError: String? = null
)

class GroupPostDetailViewModel(
    private val groupId: Long,
    private val communityRepository: CommunityRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupPostDetailState())
    val uiState: StateFlow<GroupPostDetailState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if (secureSessionManager.getUserRole() != "ADMIN") {
                    val preview = communityRepository.preview(groupId)
                    checkRole(preview.myRole)
                } else {
                    checkRole(null) // ADMIN gets moderator status immediately
                }
            } catch (_: Exception) {
            }
        }
        loadPosts()
    }

    private fun checkRole(myRoleInGroup: String?) {
        val role = secureSessionManager.getUserRole()
        val isModerator = role == "ADMIN" || myRoleInGroup == "HOST"
        _uiState.update { current ->
            current.copy(
                isModerator = isModerator,
                activeTab = if (!isModerator && current.activeTab == GroupPostTab.PENDING) {
                    GroupPostTab.APPROVED
                } else {
                    current.activeTab
                }
            )
        }
    }

    fun setTab(tab: GroupPostTab) {
        _uiState.update { it.copy(activeTab = tab) }
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (_uiState.value.activeTab) {
                    GroupPostTab.APPROVED -> {
                        val posts = communityRepository.posts(groupId)
                        _uiState.update { it.copy(approvedPosts = posts) }
                    }
                    GroupPostTab.MY_POSTS -> {
                        val posts = communityRepository.myPosts(groupId)
                        _uiState.update { it.copy(myPosts = posts) }
                    }
                    GroupPostTab.PENDING -> {
                        val posts = communityRepository.pendingPosts(groupId)
                        _uiState.update { it.copy(pendingPosts = posts) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Đã có lỗi xảy ra") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun approvePost(postId: Long) {
        viewModelScope.launch {
            try {
                communityRepository.approvePost(postId)
                loadPosts()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Không thể duyệt bài viết") }
            }
        }
    }

    fun rejectPost(postId: Long, reason: String) {
        viewModelScope.launch {
            try {
                communityRepository.rejectPost(postId, reason)
                loadPosts()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Không thể từ chối bài viết") }
            }
        }
    }

    fun toggleLike(postId: Long) {
        _uiState.update { state ->
            val updatePosts = { posts: List<GroupPost> ->
                posts.map { post ->
                    if (post.id == postId) {
                        val newLikedByMe = !post.likedByMe
                        val newLikeCount = if (newLikedByMe) post.likeCount + 1 else maxOf(0, post.likeCount - 1)
                        post.copy(likedByMe = newLikedByMe, likeCount = newLikeCount)
                    } else post
                }
            }
            state.copy(
                approvedPosts = updatePosts(state.approvedPosts),
                myPosts = updatePosts(state.myPosts)
            )
        }

        viewModelScope.launch {
            val result = communityRepository.likeGroupPost(postId)
            if (result.isFailure) {
                loadPosts()
            }
        }
    }

    fun openCommentSheet(postId: Long) {
        _uiState.update { 
            it.copy(
                isCommentSheetVisible = true,
                selectedPostIdForComments = postId,
                commentsList = emptyList(),
                isCommentsLoading = true,
                commentError = null
            )
        }
        loadCommentsForPost(postId)
    }

    fun closeCommentSheet() {
        _uiState.update { 
            it.copy(
                isCommentSheetVisible = false,
                selectedPostIdForComments = null
            )
        }
    }

    private fun loadCommentsForPost(postId: Long) {
        viewModelScope.launch {
            val result = communityRepository.getGroupPostComments(postId)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        commentsList = result.getOrNull() ?: emptyList(),
                        isCommentsLoading = false
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        commentError = result.exceptionOrNull()?.message ?: "Lỗi tải bình luận",
                        isCommentsLoading = false
                    )
                }
            }
        }
    }

    fun submitComment(content: String) {
        val postId = _uiState.value.selectedPostIdForComments ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            val result = communityRepository.createGroupPostComment(postId, content)
            if (result.isSuccess) {
                val newComment = result.getOrThrow()
                _uiState.update { state ->
                    val updatedComments = listOf(newComment) + state.commentsList
                    val updatePosts = { posts: List<GroupPost> ->
                        posts.map { post ->
                            if (post.id == postId) {
                                post.copy(commentCount = post.commentCount + 1)
                            } else post
                        }
                    }
                    state.copy(
                        commentsList = updatedComments,
                        approvedPosts = updatePosts(state.approvedPosts),
                        myPosts = updatePosts(state.myPosts)
                    )
                }
            } else {
                _uiState.update { it.copy(commentError = result.exceptionOrNull()?.message) }
            }
        }
    }

    companion object {
        fun provideFactory(
            groupId: Long,
            communityRepository: CommunityRepository,
            secureSessionManager: SecureSessionManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupPostDetailViewModel(groupId, communityRepository, secureSessionManager) as T
            }
        }
    }
}
