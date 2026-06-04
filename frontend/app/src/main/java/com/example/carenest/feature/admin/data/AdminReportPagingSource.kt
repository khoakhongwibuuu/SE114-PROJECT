package com.example.carenest.feature.admin.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.carenest.feature.admin.data.repository.AdminRepository

class AdminReportPagingSource(
    private val repository: AdminRepository,
) : PagingSource<Int, AdminReportSummaryResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AdminReportSummaryResponse> {
        val page = params.key ?: 0
        val pageSize = params.loadSize.coerceAtLeast(20)
        return runCatching {
            val reports = repository.getPendingReports(page = page, size = pageSize)
            LoadResult.Page(
                data = reports,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (reports.size < pageSize) null else page + 1,
            )
        }.getOrElse { error ->
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AdminReportSummaryResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
