package com.example.readingroom.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20, // Ограничение количества результатов
        @Query("fields") fields: String = "key,title,author_name,isbn,cover_i" // Запрашиваемые поля
    ): OpenLibrarySearchResponse
}

data class OpenLibrarySearchResponse(
    val numFound: Int,
    val start: Int,
    val numFoundExact: Boolean,
    val docs: List<OpenLibraryDoc>?
)

data class OpenLibraryDoc(
    val key: String?, // Пример: /works/OL45883W
    val title: String?,
    val author_name: List<String>?,
    val isbn: List<String>?, // Список ISBN
    val cover_i: Int? // ID обложки OpenLibrary
) {
    // Функция для получения URL обложки (может потребовать доработки)
    // Размеры: S, M, L
    fun getCoverUrl(size: String = "M"): String? {
        return cover_i?.let { "https://covers.openlibrary.org/b/id/$it-$size.jpg" }
    }
} 