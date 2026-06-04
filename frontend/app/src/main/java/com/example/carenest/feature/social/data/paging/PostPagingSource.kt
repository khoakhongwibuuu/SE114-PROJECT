package com.example.carenest.feature.social.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.carenest.feature.social.data.remote.SocialApi
import com.example.carenest.feature.social.domain.model.Post

class PostPagingSource(
    private val api: SocialApi,
    private val groupId: Long
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: 0
        val limit = params.loadSize.coerceAtLeast(20)

        return runCatching {
            val response = api.getGroupPosts(groupId = groupId, page = page, limit = limit)
            if (!response.isSuccessful) {
                throw IllegalStateException(response.body()?.message ?: "Khong the tai bai viet nhom")
            }

            val payload = response.body()?.data
                ?: throw IllegalStateException("Khong nhan duoc du lieu bai viet nhom")

            LoadResult.Page(
                data = payload.items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = resolveNextKey(page = page, limit = limit, itemCount = payload.items.size, hasNextPage = payload.hasNextPage, totalPages = payload.totalPages)
            )
        }.getOrElse { error ->
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
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
