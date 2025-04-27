package com.example.readingroom.ui.screens.library

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.model.Book
import com.example.readingroom.data.BookRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---> 1. Enum для критериев сортировки <---
enum class SortCriteria(val field: String, val direction: Query.Direction) {
    DATE_ADDED_DESC("addedDate", Query.Direction.DESCENDING),
    DATE_ADDED_ASC("addedDate", Query.Direction.ASCENDING),
    TITLE_ASC("title", Query.Direction.ASCENDING),
    TITLE_DESC("title", Query.Direction.DESCENDING),
    RATING_DESC("rating", Query.Direction.DESCENDING),
    RATING_ASC("rating", Query.Direction.ASCENDING)
}
// <--- --- --- --- --- --- --- --- --- --->

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Получаем ID пользователя для просмотра (если передан как ключ Hilt)
    private val viewingUserId: String? = savedStateHandle.get<String>("friendId") 
    // Определяем, является ли это просмотром своей библиотеки или чужой
    val isReadOnly: Boolean = viewingUserId != null
    // Определяем ID пользователя, чьи книги загружать
    private val targetUserId: String? = viewingUserId ?: firebaseAuth.currentUser?.uid

    // ---> 2. Состояние для текущей сортировки <---
    private val _sortCriteria = MutableStateFlow(SortCriteria.DATE_ADDED_DESC) // По умолчанию - новые сверху
    val sortCriteria: StateFlow<SortCriteria> = _sortCriteria.asStateFlow()
    // <--- --- --- --- --- --- --- --- --- --->

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Начинаем с true
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Используем SharedFlow для показа временных сообщений (например, в Snackbar)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        Log.d("LibraryViewModel", "Initializing. viewingUserId: $viewingUserId, isReadOnly: $isReadOnly, targetUserId: $targetUserId")
        observeBooks() // Начинаем слушать книги (теперь с учетом сортировки)
    }

    // ---> 4. Модифицируем observeBooks <---
    private fun observeBooks() {
        if (targetUserId == null || targetUserId.isBlank()) {
            _error.value = "Ошибка: Не удалось определить ID пользователя для загрузки библиотеки"
            _isLoading.value = false
            _books.value = emptyList()
            return
        }

        viewModelScope.launch {
            // Используем combine для реагирования на изменение sortCriteria
            _sortCriteria.flatMapLatest { currentSortCriteria ->
                Log.d("LibraryViewModel", "Criteria changed or initial load. Sorting by: ${currentSortCriteria.name}")
                // Запрашиваем книги с нужной сортировкой
                bookRepository.getAllBooksSorted(targetUserId, currentSortCriteria.field, currentSortCriteria.direction)
            }
                .onStart { 
                    Log.d("LibraryViewModel", "Starting to collect books for user: $targetUserId with sort: ${_sortCriteria.value.name}")
                    _isLoading.value = true 
                    _error.value = null 
                }
                .catch { e -> 
                    Log.e("LibraryViewModel", "Error collecting books for user $targetUserId", e)
                    _error.value = "Ошибка загрузки библиотеки: ${e.message}" 
                    _isLoading.value = false
                }
                .collectLatest { userBooks ->
                    Log.d("LibraryViewModel", "Collected ${userBooks.size} books for user $targetUserId.")
                    // ---> Добавляем лог ПЕРЕД фильтрацией <---
                    Log.d("LibraryViewModel", "RAW userBooks size before filtering: ${userBooks.size}")
                    // +++ ДОПОЛНИТЕЛЬНЫЙ ЛОГ ДЛЯ КАЖДОЙ КНИГИ ПЕРЕД ФИЛЬТРОМ +++
                    userBooks.forEach { book ->
                        Log.d("LibraryViewModel", "PRE-FILTER: ID=${book.id}, Title='${book.title}', isRead=${book.isRead}, isFavorite=${book.isFavorite}, isInWishlist=${book.isInWishlist}")
                    }
                    // +++ КОНЕЦ ДОПОЛНИТЕЛЬНОГО ЛОГА +++
                    // Фильтрация остается
                    val filteredBooks = userBooks.filter { book ->
                         book.isRead || book.isFavorite || !book.isInWishlist
                    }
                    Log.d("LibraryViewModel", "Filtered list size: ${filteredBooks.size}")
                    _books.value = filteredBooks
                    _isLoading.value = false
                    if (_error.value?.startsWith("Ошибка загрузки библиотеки") != true) {
                       _error.value = null 
                    } 
                }
        }
    }
    // <--- --- --- --- --- --- --- --- --- --->

    // ---> 3. Функция для установки сортировки <---
    fun setSortOrder(criteria: SortCriteria) {
        Log.d("LibraryViewModel", "Setting sort order to: ${criteria.name}")
        _sortCriteria.value = criteria
        // observeBooks перезапустится автоматически из-за flatMapLatest
    }
    // <--- --- --- --- --- --- --- --- --- --->

    // Можно добавить функцию для обновления, если понадобится
    /*
    fun refreshBooks() {
        observeBooks()
    }
    */

    // --- Функции для обновления статуса книги ---

    fun toggleReadStatus(bookId: String) {
        if (isReadOnly) return // Не позволяем менять статус в чужой библиотеке
        viewModelScope.launch {
            val book = _books.value.firstOrNull { it.id == bookId }
            if (book != null) {
                val newReadStatus = !book.isRead
                // Используем Timestamp
                val newReadDate: Timestamp? = if (newReadStatus) Timestamp.now() else null 
                val updatedBook = book.copy(isRead = newReadStatus, readDate = newReadDate)
                updateBookInRepository(updatedBook, "Ошибка обновления статуса 'Прочитано'")
            }
        }
    }

    fun toggleFavoriteStatus(bookId: String) {
        if (isReadOnly) return
        viewModelScope.launch {
            val book = _books.value.firstOrNull { it.id == bookId }
            if (book != null) {
                val updatedBook = book.copy(isFavorite = !book.isFavorite)
                updateBookInRepository(updatedBook, "Ошибка обновления статуса 'Избранное'")
            }
        }
    }

    fun toggleWishlistStatus(bookId: String) {
        if (isReadOnly) return
        viewModelScope.launch {
            val book = _books.value.firstOrNull { it.id == bookId }
            if (book != null) {
                val updatedBook = book.copy(isInWishlist = !book.isInWishlist)
                updateBookInRepository(updatedBook, "Ошибка обновления статуса 'Вишлист'")
            }
        }
    }

    // Добавляем функцию добавления книги из библиотеки друга
    fun addBookFromFriend(book: Book) {
         val currentUserId = firebaseAuth.currentUser?.uid
         if (currentUserId == null || currentUserId.isBlank()) {
             viewModelScope.launch { _userMessage.emit("Ошибка: Не удалось добавить книгу (пользователь не найден)") }
             return
         }
         // Создаем копию для текущего пользователя, сбрасываем статусы
         val bookToAdd = book.copy(
             userId = currentUserId, 
             isRead = false, 
             isFavorite = false, 
             isInWishlist = false, 
             readDate = null,
             addedDate = null // Устанавливаем null, @ServerTimestamp сработает при записи
         )
         viewModelScope.launch {
             try {
                 bookRepository.addBook(bookToAdd)
                 _userMessage.emit("${book.title} добавлена в вашу библиотеку")
             } catch (e: Exception) {
                 Log.e("LibraryViewModel", "Error adding book from friend", e)
                 _userMessage.emit("Ошибка добавления книги: ${e.message}")
             }
         }
    }

    // Вспомогательная функция для обновления книги в репозитории
    private suspend fun updateBookInRepository(book: Book, errorPrefix: String) {
        Log.d("LibraryViewModel", "Attempting to update book ${book.id} with new status: isRead=${book.isRead}, isFavorite=${book.isFavorite}, isInWishlist=${book.isInWishlist}, readDate=${book.readDate}")
        try {
            bookRepository.updateBook(book)
            Log.d("LibraryViewModel", "Book ${book.id} update successful (in theory)")
        } catch (e: Exception) {
            Log.e("LibraryViewModel", "$errorPrefix for book ${book.id}", e)
            _userMessage.emit("$errorPrefix: ${e.message}")
        }
    }

    // TODO: Добавить функции для взаимодействия с книгами (отметить прочитанной и т.д.)
} 