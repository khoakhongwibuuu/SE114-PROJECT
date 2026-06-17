package com.example.carenest.feature.community.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.GroupMember
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
    val canManageMembers: Boolean = false,
    val canLeaveGroup: Boolean = true,
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
    val commentError: String? = null,

    val currentUserId: Long? = null,
    val members: List<GroupMember> = emptyList(),
    val isMembersSheetVisible: Boolean = false,
    val isMembersLoading: Boolean = false,
    val memberOperationUserId: Long? = null,
    val message: String? = null
)

class GroupPostDetailViewModel(
    private val groupId: Long,
    private val communityRepository: CommunityRepository,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupPostDetailState())
    val uiState: StateFlow<GroupPostDetailState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(currentUserId = secureSessionManager.getUserId()) }
        viewModelScope.launch {
            try {
                val preview = communityRepository.preview(groupId)
                checkRole(preview.myRole, preview.joined)
            } catch (_: Exception) {
                if (secureSessionManager.getUserRole() == "ADMIN") {
                    checkRole(null, false)
                }
            }
        }
        loadPosts()
    }

    private fun checkRole(myRoleInGroup: String?, joined: Boolean = true) {
        val role = secureSessionManager.getUserRole()
        val normalizedAppRole = role?.removePrefix("ROLE_")?.uppercase()
        val normalizedGroupRole = myRoleInGroup?.uppercase()
        val isModerator = normalizedAppRole == "ADMIN" || normalizedGroupRole == "HOST" || normalizedGroupRole == "MODERATOR"
        val canManageMembers = normalizedAppRole == "ADMIN" || normalizedGroupRole == "HOST"
        _uiState.update { current ->
            current.copy(
                isModerator = isModerator,
                canManageMembers = canManageMembers,
                canLeaveGroup = joined,
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

    fun updatePost(postId: Long, title: String, content: String, tags: String?) {
        if (title.isBlank() || content.isBlank()) {
            _uiState.update { it.copy(error = "Tiêu đề và nội dung không được để trống") }
            return
        }
        viewModelScope.launch {
            runCatching {
                communityRepository.updatePost(postId, title.trim(), content.trim(), tags?.trim())
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        activeTab = GroupPostTab.MY_POSTS,
                        error = null,
                        message = "Bài viết đã được gửi lại để duyệt"
                    )
                }
                loadPosts()
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể cập nhật bài viết") }
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                communityRepository.deletePost(postId)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        approvedPosts = state.approvedPosts.filterNot { it.id == postId },
                        myPosts = state.myPosts.filterNot { it.id == postId },
                        pendingPosts = state.pendingPosts.filterNot { it.id == postId },
                        error = null,
                        message = "Đã xóa bài viết"
                    )
                }
                loadPosts()
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể xóa bài viết") }
            }
        }
    }

    fun reportPost(postId: Long, reason: String) {
        if (reason.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập lý do báo cáo") }
            return
        }
        viewModelScope.launch {
            runCatching {
                communityRepository.reportPost(postId, reason.trim())
            }.onSuccess {
                _uiState.update { it.copy(error = null, message = "Đã gửi báo cáo cho quản trị viên") }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể báo cáo bài viết") }
            }
        }
    }

    fun openMembersSheet() {
        _uiState.update { it.copy(isMembersSheetVisible = true, error = null) }
        loadMembers()
    }

    fun closeMembersSheet() {
        _uiState.update { it.copy(isMembersSheetVisible = false) }
    }

    fun loadMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMembersLoading = true, error = null) }
            runCatching {
                communityRepository.getMembers(groupId)
            }.onSuccess { members ->
                _uiState.update { it.copy(members = members, isMembersLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isMembersLoading = false,
                        error = error.message ?: "Không thể tải danh sách thành viên"
                    )
                }
            }
        }
    }

    fun leaveGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                communityRepository.leave(groupId)
            }.onSuccess {
                _uiState.update { it.copy(message = "Đã rời nhóm") }
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể rời nhóm") }
            }
        }
    }

    fun kickMember(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberOperationUserId = userId, error = null) }
            runCatching {
                communityRepository.kickMember(groupId, userId)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        members = state.members.filterNot { it.userId == userId },
                        memberOperationUserId = null,
                        message = "Đã mời thành viên rời nhóm"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        memberOperationUserId = null,
                        error = error.message ?: "Không thể mời thành viên rời nhóm"
                    )
                }
            }
        }
    }

    fun updateMemberRole(userId: Long, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberOperationUserId = userId, error = null) }
            runCatching {
                communityRepository.updateMemberRole(groupId, userId, role)
            }.onSuccess { 
                _uiState.update { state ->
                    state.copy(
                        members = state.members.map { member ->
                            if (member.userId == userId) member.copy(role = role) else member
                        },
                        memberOperationUserId = null,
                        message = "Đã cập nhật vai trò thành viên"
                    )
                }
                val myRole = if (userId == secureSessionManager.getUserId()) role else null
                if (myRole != null) checkRole(myRole, joined = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        memberOperationUserId = null,
                        error = error.message ?: "Không thể cập nhật quyền thành viên"
                    )
                }
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

    fun clearTransientMessage() {
        _uiState.update { it.copy(error = null, message = null) }
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
