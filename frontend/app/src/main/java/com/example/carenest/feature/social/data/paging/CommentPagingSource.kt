package com.example.carenest.feature.social.data.paging

import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.carenest.feature.social.data.remote.SocialApi
import com.example.carenest.feature.social.domain.model.Comment

class CommentPagingSource(
    private val api: SocialApi,
    private val postId: Long,
    private val parentCommentId: Long? = null
) : PagingSource<Int, Comment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        val page = params.key ?: 0
        val limit = params.loadSize.coerceAtLeast(20)

        return runCatching {
            val response = api.getPostComments(
                postId = postId,
                parentCommentId = parentCommentId,
                page = page,
                limit = limit
            )
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorMessage("Không thể tải bình luận bài viết"))
            }

            val payload = response.requireData(
                fallback = "Không thể tải bình luận bài viết",
                missingDataMessage = "Không nhận được dữ liệu bình luận bài viết"
            )

            LoadResult.Page(
                data = payload.items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = resolveNextKey(page = page, limit = limit, itemCount = payload.items.size, hasNextPage = payload.hasNextPage, totalPages = payload.totalPages)
            )
        }.getOrElse { error ->
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    private fun resolveNextKey(
        page: Int,
        limit: Int,
        itemCount: Int,
        hasNextPage: Boolean,
        totalPages: Int
    ): Int? {
        return when {
            hasNextPage -> page + 1
            totalPages > 0 && page + 1 < totalPages -> page + 1
            itemCount >= limit -> page + 1
            else -> null
        }
    }
}
