package com.example.readingroom.ui.screens.book

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
import com.example.readingroom.model.Book
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>("bookId") ?: ""

    private val _bookState = MutableStateFlow<BookUiState>(BookUiState.Loading)
    val bookState: StateFlow<BookUiState> = _bookState.asStateFlow()
    
    // SharedFlow для событий (например, сообщение об удалении, запрос выбора картинки)
    private val _eventFlow = MutableSharedFlow<BookScreenEvent>()
    val eventFlow: SharedFlow<BookScreenEvent> = _eventFlow.asSharedFlow()

    init {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null || userId.isBlank() || bookId.isBlank()) {
            _bookState.value = BookUiState.Error("Ошибка: Не удалось загрузить данные книги (нет userId или bookId)")
            return
        }

        viewModelScope.launch {
            _bookState.value = BookUiState.Loading // Показываем загрузку
            bookRepository.getBookById(userId, bookId)
                .catch { e ->
                    Log.e("BookViewModel", "Error loading book $bookId", e)
                    _bookState.value = BookUiState.Error("Ошибка загрузки: ${e.message}")
                }
                .collectLatest { book ->
                    if (book != null) {
                        _bookState.value = BookUiState.Success(book)
                    } else {
                        _bookState.value = BookUiState.Error("Книга с ID $bookId не найдена")
                    }
                }
        }
    }

    // --- Функции обновления статусов --- 
    fun toggleReadStatus() = updateBookField { 
        val newReadStatus = !it.isRead
        val newReadDate: Timestamp? = if (newReadStatus) Timestamp.now() else null
        it.copy(isRead = newReadStatus, readDate = newReadDate) 
    }
    
    // Изменяем toggleFavoriteStatus
    fun toggleFavoriteStatus() = updateBookField { book ->
        val newFavoriteStatus = !book.isFavorite
        if (newFavoriteStatus) {
            // Если делаем избранной, то также делаем прочитанной и ставим дату прочтения
            book.copy(isFavorite = true, isRead = true, readDate = book.readDate ?: Timestamp.now()) // Используем существующую дату, если есть, иначе текущую
        } else {
            // Если убираем из избранного, статус прочитанной НЕ меняем
            book.copy(isFavorite = false)
        }
    }
    
    fun toggleWishlistStatus() = updateBookField { it.copy(isInWishlist = !it.isInWishlist) }

    // --- Функция обновления рейтинга (пока просто принимает Float) ---
    fun updateRating(newRating: Float) {
         // Округляем до ближайших 0.5
        val roundedRating = (newRating * 2).roundToInt() / 2f 
        // Сохраняем как Float вместо Int, чтобы поддерживать дробные значения
        updateBookField { it.copy(rating = roundedRating) }
    }

    // --- Удаление книги ---
    fun deleteBook() {
        viewModelScope.launch {
             val currentState = _bookState.value
             if (currentState is BookUiState.Success) {
                 try {
                     bookRepository.deleteBook(currentState.book.id)
                     _eventFlow.emit(BookScreenEvent.ShowMessage("Книга удалена"))
                     _eventFlow.emit(BookScreenEvent.NavigateBack) // Сообщаем UI, что нужно уйти с экрана
                 } catch (e: Exception) {
                      Log.e("BookViewModel", "Error deleting book ${currentState.book.id}", e)
                     _eventFlow.emit(BookScreenEvent.ShowMessage("Ошибка удаления: ${e.message}"))
                 }
             } else {
                 _eventFlow.emit(BookScreenEvent.ShowMessage("Невозможно удалить книгу в текущем состоянии"))
             }
        }
    }
    
    // --- Редактирование обложки (инициирует событие) ---
    fun requestCoverImageChange() {
         viewModelScope.launch {
             _eventFlow.emit(BookScreenEvent.PickImage)
         }
    }
    
    // --- Обработка результата выбора изображения --- 
    fun handleImageSelection(imageUri: Uri) {
        Log.d("BookViewModel", "Image selected: $imageUri")
        val currentState = _bookState.value
        if (currentState !is BookUiState.Success) {
            Log.w("BookViewModel", "Cannot handle image selection, current state is not Success")
            // Можно показать сообщение об ошибке, если нужно
            // viewModelScope.launch { _eventFlow.emit(BookScreenEvent.ShowMessage("Ошибка: Данные книги не загружены")) }
            return
        }
        val currentBook = currentState.book
        
        viewModelScope.launch {
            try {
                _eventFlow.emit(BookScreenEvent.ShowMessage("Загрузка новой обложки..."))
                
                // 1. Загрузить изображение через репозиторий (который использует ImageUploader)
                val uploadResult = bookRepository.uploadBookCover(currentBook.id, imageUri)
                
                uploadResult.fold(
                   onSuccess = { downloadUrl ->
                       Log.d("BookViewModel", "Cover upload successful: $downloadUrl")
                       // 2. Обновить URL обложки в Firestore
                       updateCoverImage(downloadUrl)
                       _eventFlow.emit(BookScreenEvent.ShowMessage("Обложка успешно обновлена!"))
                   },
                   onFailure = { exception ->
                       Log.e("BookViewModel", "Cover upload failed", exception)
                       _eventFlow.emit(BookScreenEvent.ShowMessage("Ошибка загрузки обложки: ${exception.message}"))
                   }
                )
                
            } catch (e: Exception) {
                // Этот catch больше для неожиданных ошибок, т.к. Result обрабатывается выше
                Log.e("BookViewModel", "Unexpected error handling image selection", e)
                _eventFlow.emit(BookScreenEvent.ShowMessage("Неожиданная ошибка обработки изображения: ${e.message}"))
            }
        }
    }

    // --- Обновление URL обложки в Firestore ---
    private fun updateCoverImage(imageUrl: String) {
        // Эта функция вызывается ПОСЛЕ загрузки изображения в Storage
        updateBookField { it.copy(coverUrl = imageUrl) }
    }

    // Вспомогательная функция для обновления полей книги
    private fun updateBookField(updateAction: (Book) -> Book) {
        viewModelScope.launch {
            val currentState = _bookState.value
            if (currentState is BookUiState.Success) {
                val originalBook = currentState.book
                val updatedBook = updateAction(originalBook)
                // Оптимистичное обновление UI (можно убрать, если не нужно)
                _bookState.value = BookUiState.Success(updatedBook)
                try {
                    bookRepository.updateBook(updatedBook)
                    // Данные обновятся через Flow из loadBookDetails, если он активен
                } catch (e: Exception) {
                    Log.e("BookViewModel", "Error updating book ${originalBook.id}", e)
                    // Возвращаем старое состояние в UI при ошибке (если было оптимистичное обновление)
                    _bookState.value = BookUiState.Success(originalBook) 
                     _eventFlow.emit(BookScreenEvent.ShowMessage("Ошибка обновления: ${e.message}"))
                }
            } else {
                 _eventFlow.emit(BookScreenEvent.ShowMessage("Невозможно обновить книгу в текущем состоянии"))
            }
        }
    }
}

// Состояния UI для экрана деталей
sealed class BookUiState {
    object Loading : BookUiState()
    data class Success(val book: Book) : BookUiState()
    data class Error(val message: String) : BookUiState()
}

// События, которые ViewModel может отправлять в UI
sealed class BookScreenEvent {
    data class ShowMessage(val message: String) : BookScreenEvent()
    object NavigateBack : BookScreenEvent()
    object PickImage : BookScreenEvent() // Запрос на выбор изображения
} 