package com.example.readingroom.data

import com.example.readingroom.model.Book
import kotlinx.coroutines.flow.Flow
import android.net.Uri
import com.google.firebase.firestore.Query

interface BookRepository {
    // --- Методы чтения (для конкретного пользователя) ---

    /** Получить все книги пользователя */
    fun getAllBooks(userId: String): Flow<List<Book>>

    /** Получить все книги пользователя с сортировкой */
    fun getAllBooksSorted(userId: String, sortBy: String, direction: Query.Direction): Flow<List<Book>>

    /** Получить книгу пользователя по ID */
    fun getBookById(userId: String, bookId: String): Flow<Book?>

    /** Поиск книг пользователя по запросу */
    fun searchBooks(userId: String, query: String): Flow<List<Book>>

    /** Получить книги пользователя по статусу */
    fun getBooksByStatus(userId: String, status: BookStatus): Flow<List<Book>>

    /** Получить поток списка книг пользователя по его ID */
    fun getBooksByUserId(userId: String): Flow<List<Book>>

    /** Получить все книги пользователя */
    fun getUserBooks(userId: String): Flow<List<Book>>

    // ---> Метод для получения книг ТОЛЬКО из коллекции manual_books <---
    fun getManualBooks(userId: String): Flow<List<Book>>

    // --- Методы записи ---

    /** Добавить книгу для пользователя (в основную коллекцию 'books') */
    suspend fun addBook(book: Book) // Должен содержать userId

    // ---> Метод для добавления книги ТОЛЬКО в коллекцию manual_books <---
    suspend fun addManualBook(book: Book)

    /** Обновить книгу пользователя (в основной коллекции 'books') */
    suspend fun updateBook(book: Book) // Должен содержать userId

    /** Удалить книгу пользователя */
    suspend fun deleteBook(bookId: String) // Нужен только ID книги для удаления

    // --- Методы поиска (внешние) ---

    /** Поиск книг во внешних источниках (Google Books, Open Library) */
    suspend fun searchExternalBooks(query: String): Result<List<Book>>

    /** Загрузить изображение обложки и вернуть URL */
    suspend fun uploadBookCover(bookId: String, coverUri: Uri): Result<String>

    // Новый метод для поиска ручных книг
    fun searchManualBooks(query: String): Flow<List<Book>>
}

enum class BookStatus {
    READ,
    READING,
    WANT_TO_READ,
    FAVORITE
} 