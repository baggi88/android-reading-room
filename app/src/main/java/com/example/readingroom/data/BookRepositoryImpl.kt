package com.example.readingroom.data

import com.example.readingroom.BuildConfig // Для доступа к API ключу
import com.example.readingroom.data.api.GoogleBooksApi
import com.example.readingroom.data.api.BookItem
import com.example.readingroom.data.api.OpenLibraryApi // Добавляем импорт
import com.example.readingroom.data.api.OpenLibraryDoc // Добавляем импорт
import com.example.readingroom.model.Book
import com.example.readingroom.data.BookStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async // Для параллельных вызовов
import kotlinx.coroutines.coroutineScope // Для параллельных вызовов
import android.net.Uri
import android.util.Log
import com.example.readingroom.data.remote.ImageUploader // <-- Добавляем этот импорт
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf // Для flowOf
import com.example.readingroom.data.api.toBookModel // <-- Убедимся, что этот импорт есть

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val googleBooksApi: GoogleBooksApi,
    private val openLibraryApi: OpenLibraryApi,
    private val imageUploader: ImageUploader
) : BookRepository {

    private val TAG = "BookRepositoryImpl"
    private val booksCollection = firestore.collection("books")
    private val manualBooksCollection = firestore.collection("manual_books")

    // --- Переносим определение исключения сюда ---
    class BookAlreadyExistsException(message: String) : Exception(message)
    // --------------------------------------------

    override fun getAllBooks(userId: String): Flow<List<Book>> = flow {
        Log.d(TAG, "Getting all books for user: $userId")
        val snapshot = booksCollection.whereEqualTo("userId", userId).get().await()
        val books = snapshot.documents.mapNotNull { it.toObject<Book>()?.copy(id = it.id) } // Присваиваем ID
        Log.d(TAG, "Found ${books.size} books for user $userId")
        emit(books)
    }.catch { e ->
        Log.e(TAG, "Error getting all books for user $userId", e)
        emit(emptyList()) // Emit empty list on error
    }

    override fun getAllBooksSorted(userId: String, sortBy: String, direction: Query.Direction): Flow<List<Book>> {
         Log.d(TAG, "Getting all books sorted for user: $userId by $sortBy ${direction.name}")
         return booksCollection
            .whereEqualTo("userId", userId)
            .orderBy(sortBy, direction)
            .snapshots()
            .map { snapshot ->
                val books = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject<Book>()?.copy(id = doc.id) // Присваиваем ID документа
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document ${doc.id} to Book", e)
                        null // Пропускаем документы, которые не удалось преобразовать
                    }
                }
                Log.d(TAG, "Fetched ${books.size} sorted books for user $userId")
                books
            }
            .catch { e ->
                Log.e(TAG, "Error fetching sorted books for user $userId", e)
                emit(emptyList()) // Отправляем пустой список в случае ошибки
            }
    }
    
    override fun getBookById(userId: String, bookId: String): Flow<Book?> = flow {
        Log.d(TAG, "Getting book by ID: $bookId for user $userId")
        if (bookId.isBlank()) {
             Log.w(TAG, "getBookById called with blank bookId for user $userId")
             emit(null)
             return@flow
        }
        val documentSnapshot = booksCollection.document(bookId).get().await()
        val book = try { documentSnapshot.toObject<Book>()?.copy(id = documentSnapshot.id) } catch (e: Exception) { null }

        if (book != null && book.userId == userId) {
             Log.d(TAG, "Found book in 'books': ${book.title}")
            emit(book)
        } else if (book != null && book.userId != userId) {
             Log.w(TAG, "Book $bookId found in 'books' but belongs to another user (${book.userId})")
             emit(null) // Книга найдена, но не принадлежит этому пользователю
        } else {
             Log.w(TAG, "Book $bookId not found in 'books' for user $userId. Checking 'manual_books'.")
            // Попробуем найти в manual_books, если ID совпадает
            val manualDocumentSnapshot = manualBooksCollection.document(bookId).get().await()
            val manualBook = try { manualDocumentSnapshot.toObject<Book>()?.copy(id = manualDocumentSnapshot.id) } catch (e: Exception) { null }
             if(manualBook != null && manualBook.userId == userId) {
                 Log.d(TAG, "Found manual book: ${manualBook.title}")
                 emit(manualBook)
             } else {
                 Log.w(TAG, "Book $bookId not found in 'manual_books' either for user $userId")
                 emit(null) // Не найдено нигде
             }
        }
    }.catch { e ->
        Log.e(TAG, "Error getting book $bookId for user $userId", e)
        emit(null) // Emit null on error
    }

    override fun searchBooks(userId: String, query: String): Flow<List<Book>> {
        Log.d(TAG, "Searching books for user $userId with query: $query")
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty() || normalizedQuery.length < 2) { // Минимум 2 символа для поиска
            return flowOf(emptyList())
        }
        
        // Ищем и в titleLowercase, и в authorLowercase
        val titleQuery = booksCollection
            .whereEqualTo("userId", userId)
            .orderBy("titleLowercase")
            .startAt(normalizedQuery)
            .endAt(normalizedQuery + '') // Используем хак для префиксного поиска
            .limit(15) // Ограничиваем количество результатов
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> doc.toObject<Book>()?.copy(id = doc.id) } }
            .catch { e -> Log.e(TAG, "Error in title search query", e); emit(emptyList()) } // Добавляем catch

        val authorQuery = booksCollection
            .whereEqualTo("userId", userId)
            .orderBy("authorLowercase")
            .startAt(normalizedQuery)
            .endAt(normalizedQuery + '')
            .limit(15)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> doc.toObject<Book>()?.copy(id = doc.id) } }
            .catch { e -> Log.e(TAG, "Error in author search query", e); emit(emptyList()) } // Добавляем catch

        // Объединяем результаты, удаляем дубликаты по ID
        return combine(titleQuery, authorQuery) { titleResults, authorResults ->
             Log.d(TAG, "Search results - Title: ${titleResults.size}, Author: ${authorResults.size}")
            (titleResults + authorResults).distinctBy { it.id }
        }.catch { e ->
             Log.e(TAG, "Error combining search results for user $userId", e)
             emit(emptyList())
        }
    }

    override fun getBooksByStatus(userId: String, status: BookStatus): Flow<List<Book>> {
         Log.d(TAG, "Getting books for user $userId with status: ${status.name}")
         val queryField = when (status) {
             BookStatus.READ -> "isRead"
             BookStatus.FAVORITE -> "isFavorite"
             BookStatus.WANT_TO_READ -> "isInWishlist" 
             else -> null // Для READING пока нет поля
         }
         
         return if (queryField != null) {
              booksCollection
                 .whereEqualTo("userId", userId)
                 .whereEqualTo(queryField, true)
                 .orderBy("addedDate", Query.Direction.DESCENDING) // Добавим сортировку
                 .snapshots()
                 .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Book>()?.copy(id = it.id) } }
                 .catch { e -> 
                     Log.e(TAG, "Error getting books by status $status for user $userId", e)
                     emit(emptyList()) 
                 }
         } else {
             Log.w(TAG, "Status ${status.name} is not directly queryable yet.")
             flowOf(emptyList()) // Возвращаем пустой поток, если статус не поддерживается
         }
    }
    
    override fun getManualBooks(userId: String): Flow<List<Book>> {
         Log.d(TAG, "Getting manual books for user: $userId")
         return manualBooksCollection
             .whereEqualTo("userId", userId)
             .orderBy("addedDate", Query.Direction.DESCENDING) // Сортируем по дате добавления
             .snapshots()
             .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Book>()?.copy(id = it.id) } }
             .catch { e -> 
                 Log.e(TAG, "Error getting manual books for user $userId", e)
                 emit(emptyList()) 
             }
    }

    override suspend fun addBook(book: Book) {
        if (book.userId.isBlank()) {
            throw IllegalArgumentException("Book must have a userId")
        }
        // Проверка на существование книги с таким Google Books ID
        if (!book.googleBooksId.isNullOrBlank()) {
             val existingBookQuery = booksCollection
                .whereEqualTo("userId", book.userId)
                .whereEqualTo("googleBooksId", book.googleBooksId)
                .limit(1)
                .get()
                .await()
            if (!existingBookQuery.isEmpty) {
                val existingId = existingBookQuery.documents.firstOrNull()?.id ?: "unknown"
                Log.w(TAG, "Book with Google ID ${book.googleBooksId} already exists for user ${book.userId} with doc ID $existingId")
                throw BookAlreadyExistsException("Книга '${existingBookQuery.documents.firstOrNull()?.getString("title") ?: book.googleBooksId}' уже добавлена")
            }
        }
        
        // Готовим книгу к сохранению
        val bookToSave = book.copy(
            id = "", // Firestore сам сгенерирует ID при add()
            titleLowercase = book.title.trim().lowercase(), 
            authorLowercase = book.author.trim().lowercase() 
        )
        Log.d(TAG, "Adding book: ${bookToSave.title} for user ${book.userId}")
        // Добавляем в основную коллекцию 'books'
        val addedDocRef = booksCollection.add(bookToSave).await()
        Log.d(TAG, "Book added with ID: ${addedDocRef.id}")
    }
    
    override suspend fun addManualBook(book: Book) {
        if (book.userId.isBlank()) {
            throw IllegalArgumentException("Manual book must have a userId")
        }
        if (book.id.isBlank()) {
            // Генерируем ID, если он пуст (хотя ViewModel должна была это сделать)
            Log.w(TAG, "Manual book ID is blank in addManualBook. Generating new ID.")
            val generatedId = manualBooksCollection.document().id
            val bookWithId = book.copy(id = generatedId)
            addOrUpdateManualBookInternal(bookWithId)
        } else {
             addOrUpdateManualBookInternal(book)
        }
    }
    
    // Вспомогательная функция для добавления/обновления ручной книги
    private suspend fun addOrUpdateManualBookInternal(book: Book) {
         val docRef = manualBooksCollection.document(book.id)
         val existingDoc = docRef.get().await()
         
        val bookToSave = book.copy(
            titleLowercase = book.title.trim().lowercase(), 
            authorLowercase = book.author.trim().lowercase() 
        )

         if (existingDoc.exists()) {
             Log.w(TAG, "Manual book with ID ${book.id} already exists. Updating instead of adding.")
             docRef.set(bookToSave).await() // Обновляем, если уже существует
         } else {
             Log.d(TAG, "Adding manual book with ID: ${bookToSave.id}")
             docRef.set(bookToSave).await() // Добавляем, если не существует
         }
    }

    override suspend fun updateBook(book: Book) {
        if (book.id.isBlank()) {
            throw IllegalArgumentException("Cannot update book without an ID")
        }
        // Готовим книгу к обновлению
         val bookToUpdate = book.copy(
             titleLowercase = book.title.trim().lowercase(), 
             authorLowercase = book.author.trim().lowercase() 
         )
        Log.d(TAG, "Attempting to update book with ID: ${bookToUpdate.id}")
        // Сначала пробуем обновить в 'books'
        val bookDocRef = booksCollection.document(book.id)
        
        try {
             // Используем update для частичного обновления, если документ существует
             // или set для полного перезаписывания (более предсказуемо при изменениях модели)
             bookDocRef.set(bookToUpdate).await() 
             Log.d(TAG, "Book ${book.id} updated successfully (likely in 'books').")
             // Если успешно обновили в 'books', выходим
             return 
        } catch (e: Exception) {
             // Предполагаем, что ошибка = документ не найден в 'books'
             Log.w(TAG, "Failed to update book ${book.id} in 'books', trying 'manual_books'. Error: ${e.message}")
        }
        
        // Если не удалось обновить в 'books', пробуем обновить в 'manual_books'
         val manualBookDocRef = manualBooksCollection.document(book.id)
         try {
             manualBookDocRef.set(bookToUpdate).await()
             Log.d(TAG, "Book ${book.id} updated successfully (in 'manual_books').")
         } catch (e: Exception) {
              Log.e(TAG, "Failed to update book ${book.id} in 'manual_books' as well.", e)
              // Можно выбросить исключение или просто записать лог
              throw NoSuchElementException("Book with ID ${book.id} not found in 'books' or 'manual_books'.")
         }
    }

    override suspend fun deleteBook(bookId: String) {
        if (bookId.isBlank()) {
            throw IllegalArgumentException("Cannot delete book without an ID")
        }
        Log.d(TAG, "Deleting book with ID: $bookId from both collections")
        // Удаляем из обеих коллекций
        val deleteBookTask = booksCollection.document(bookId).delete()
        val deleteManualBookTask = manualBooksCollection.document(bookId).delete()
        
        var bookDeleted = false
        var manualBookDeleted = false
        
        try {
            deleteBookTask.await()
            bookDeleted = true
            Log.d(TAG, "Book $bookId deleted from 'books' (or did not exist).")
        } catch (e: Exception) {
             Log.w(TAG, "Error deleting book $bookId from 'books'", e)
        }
        
         try {
            deleteManualBookTask.await()
            manualBookDeleted = true
             Log.d(TAG, "Book $bookId deleted from 'manual_books' (or did not exist).")
        } catch (e: Exception) {
             Log.w(TAG, "Error deleting book $bookId from 'manual_books'", e)
        }
        
        if (!bookDeleted && !manualBookDeleted) {
             Log.w(TAG, "Book $bookId was not found in either collection for deletion.")
             // Можно выбросить исключение, если удаление несуществующей книги - ошибка
             // throw NoSuchElementException("Book with ID $bookId not found for deletion.")
        }
    }

    // ---> Убираем override у getWishlistBooks <---
    /* override */ fun getWishlistBooks(userId: String): Flow<List<Book>> { // Убрали override
        Log.d(TAG, "Getting wishlist books for user $userId")
        if (userId.isBlank()) {
            return flowOf(emptyList())
        }
        return booksCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isInWishlist", true) 
            .orderBy("addedDate", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> doc.toObject<Book>()?.copy(id = doc.id) } } // Добавляем copy(id=doc.id)
            .distinctUntilChanged()
            .catch { e ->
                 Log.e(TAG, "Error getting wishlist books for user $userId", e)
                 emit(emptyList())
             }
    }

    override fun searchManualBooks(query: String): Flow<List<Book>> {
        val queryLowercase = query.trim().lowercase()
        if (queryLowercase.isEmpty() || queryLowercase.length < 2) { // Минимум 2 символа
            return flowOf(emptyList()) 
        }
        Log.d(TAG, "Searching manual books for: $queryLowercase")

        // Поиск по titleLowercase
        val titleSearchFlow = manualBooksCollection
            .orderBy("titleLowercase")
            .startAt(queryLowercase)
            .endAt(queryLowercase + '')
            .limit(10) 
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> doc.toObject<Book>()?.copy(id = doc.id) } }
            .catch { e -> Log.e(TAG, "Error searching manual books by title", e); emit(emptyList()) }

        // Поиск по authorLowercase    
        val authorSearchFlow = manualBooksCollection
            .orderBy("authorLowercase")
            .startAt(queryLowercase)
            .endAt(queryLowercase + '')
            .limit(10) 
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { doc -> doc.toObject<Book>()?.copy(id = doc.id) } }
            .catch { e -> Log.e(TAG, "Error searching manual books by author", e); emit(emptyList()) }
            
        // Объединяем результаты и убираем дубликаты
        return combine(titleSearchFlow, authorSearchFlow) { titleResults, authorResults ->
             (titleResults + authorResults).distinctBy { it.id }
        }.catch { e -> 
            Log.e(TAG, "Error combining manual search results", e)
            emit(emptyList()) 
        }
    }
    
     override suspend fun uploadBookCover(bookId: String, coverUri: Uri): Result<String> {
         Log.d(TAG, "Uploading cover for book $bookId from URI: $coverUri")
         return imageUploader.uploadImage(coverUri) // Просто передаем URI
     }

    override suspend fun searchExternalBooks(query: String): Result<List<Book>> { 
        return withContext(Dispatchers.IO) {
            try {
                coroutineScope { 
                    val googleBooksDeferred = async { 
                        try {
                            val response = googleBooksApi.searchBooks(query, BuildConfig.GOOGLE_BOOKS_API_KEY)
                            // Используем восстановленную toBookModel
                            val books: List<Book> = response.items?.mapNotNull { it?.toBookModel() } ?: emptyList()
                            Log.d(TAG, "Google Books API returned ${books.size} books for query '$query'")
                            books
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching from Google Books API for query '$query'", e)
                            emptyList<Book>()
                        }
                    }
                    
                    val googleResults: List<Book> = googleBooksDeferred.await()
                    
                    // Указываем тип явно для distinctBy, если нужно
                    val combinedResults = googleResults.distinctBy<Book, String> { it.googleBooksId ?: it.id }
                    Log.d(TAG, "External search combined results size: ${combinedResults.size} for query '$query'.")
                    // Результат coroutineScope будет Result<List<Book>>
                    Result.success(combinedResults) 
                } // конец coroutineScope
            } catch (e: Exception) {
                Log.e(TAG, "Error during external book search coroutine scope for query '$query'", e)
                // Результат withContext при ошибке будет Result<List<Book>>
                Result.failure<List<Book>>(e) 
            } // конец catch
        } // конец withContext
    } // конец функции searchExternalBooks

    // --- Реализация getBooksByUserId (проверяем сигнатуру) ---
    override fun getBooksByUserId(userId: String): Flow<List<Book>> {
        Log.d(TAG, "Getting books by user ID: $userId (using snapshots)")
        if (userId.isBlank()) {
             Log.w(TAG, "getBooksByUserId called with blank userId")
             return flowOf(emptyList())
        }
        return booksCollection
            .whereEqualTo("userId", userId)
            .orderBy("addedDate", Query.Direction.DESCENDING) // Пример сортировки
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject<Book>()?.copy(id = doc.id) // Присваиваем ID документа
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document ${doc.id} to Book in getBooksByUserId", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error getting books by user ID $userId", e)
                emit(emptyList())
            }
    }
    // ----------------------------------
    
    // --- Возвращаем реализацию getUserBooks --- 
    override fun getUserBooks(userId: String): Flow<List<Book>> = flow {
        Log.d(TAG, "Getting user books (getUserBooks) for user: $userId")
        if (userId.isBlank()) {
             Log.w(TAG, "getUserBooks called with blank userId")
             emit(emptyList())
             return@flow
        }
        val snapshot = booksCollection.whereEqualTo("userId", userId).get().await()
        val books = snapshot.documents.mapNotNull { doc ->
            try { doc.toObject<Book>()?.copy(id = doc.id) } catch (e: Exception) { null }
        }
        Log.d(TAG, "Found ${books.size} books for user $userId in getUserBooks")
        emit(books)
    }.catch { e ->
        Log.e(TAG, "Error getting user books (getUserBooks) for user $userId", e)
        emit(emptyList())
    }
    // -----------------------------------------------------------------------
}