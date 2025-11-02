package com.visor.school.common.dto

/**
 * Paginated response wrapper
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PageResponse<T> {
            val totalPages = (totalElements / size.toDouble()).let {
                if (it % 1 == 0.0) it.toInt() else it.toInt() + 1
            }
            return PageResponse(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                first = page == 0,
                last = page >= totalPages - 1
            )
        }
    }
}

