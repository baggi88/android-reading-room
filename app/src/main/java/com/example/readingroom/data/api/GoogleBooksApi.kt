package com.example.readingroom.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("startIndex") startIndex: Int = 0
    ): BookSearchResponse
}

data class BookSearchResponse(
    val items: List<BookItem>?,
    val totalItems: Int
)

data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
) {
    fun getCoverUrl(size: CoverSize = CoverSize.MEDIUM): String? {
        val url = volumeInfo.imageLinks?.let { links ->
            when (size) {
                CoverSize.SMALL -> links.thumbnail
                CoverSize.MEDIUM -> links.medium ?: links.thumbnail
                CoverSize.LARGE -> links.large ?: links.medium ?: links.thumbnail
            }
        }
        return url?.replace("http://", "https://")
    }
}

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val averageRating: Float?,
    val ratingsCount: Int?
)

data class ImageLinks(
    val thumbnail: String?,
    val small: String?,
    val medium: String?,
    val large: String?
)

enum class CoverSize {
    SMALL,
    MEDIUM,
    LARGE
} 