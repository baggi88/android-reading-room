package com.example.readingroom.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlin.jvm.JvmField

/**
 * Модель книги
 */
data class Book(
    @DocumentId
    val id: String = "", // Google Books ID или уникальный ID для ручных книг
    val title: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val description: String = "",
    val genre: String = "",
    val pageCount: Int = 0,
    val status: String = "", // например, "library", "wishlist"
    val rating: Float = 0f,
    @JvmField
    val isFavorite: Boolean = false,
    @JvmField
    val isRead: Boolean = false,
    @ServerTimestamp // Автоматически устанавливается сервером при создании
    val addedDate: Timestamp? = null,
    val readDate: Timestamp? = null, // Дата, когда книга была отмечена как прочитанная
    val userId: String = "", // ID пользователя, которому принадлежит запись
    val googleBooksId: String? = null, // Сохраняем Google Books ID отдельно
    // Поля для поиска без учета регистра
    val titleLowercase: String = "", 
    val authorLowercase: String = "",
    @JvmField
    val isInWishlist: Boolean = false, // Доп. флаг для удобства запросов
    // val isManual: Boolean = false // Флаг для отметки ручных книг (опционально)
)

{
    constructor() : this(
        id = "", userId = "", title = "", author = "", description = "", coverUrl = "", 
        rating = 0f, isRead = false, isFavorite = false, 
        isInWishlist = false, addedDate = null, readDate = null, googleBooksId = null,
        titleLowercase = "", authorLowercase = "", genre = "", pageCount = 0, status = ""
    )
} 