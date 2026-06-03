package com.example.carenest.feature.admin.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.carenest.feature.admin.data.repository.AdminRepository

class AdminUserPagingSource(
    private val repository: AdminRepository,
    private val search: String?,
) : PagingSource<Int, AdminUserSummaryResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AdminUserSummaryResponse> {
        val page = params.key ?: 0
        val pageSize = params.loadSize.coerceAtLeast(20)
        return runCatching {
            val users = repository.getUsers(page = page, size = pageSize, search = search)
            LoadResult.Page(
                data = users,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (users.size < pageSize) null else page + 1,
            )
        }.getOrElse { error ->
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AdminUserSummaryResponse>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
