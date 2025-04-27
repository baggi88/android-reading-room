package com.example.readingroom.ui.screens.manualaddbook

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
import com.example.readingroom.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ManualAddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var author by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    // TODO: Добавить поле для выбора/загрузки обложки (пока будет пустой)
    // var coverUri by mutableStateOf<Uri?>(null)

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val _saveEvent = MutableSharedFlow<Boolean>() // true = успех, false = ошибка
    val saveEvent = _saveEvent.asSharedFlow()

    fun onTitleChange(newTitle: String) {
        title = newTitle
        error = null // Сбрасываем ошибку при изменении поля
    }

    fun onAuthorChange(newAuthor: String) {
        author = newAuthor
    }

    fun onDescriptionChange(newDescription: String) {
        description = newDescription
    }

    fun saveBook() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            error = "Ошибка: Пользователь не авторизован."
            viewModelScope.launch { _saveEvent.emit(false) }
            return
        }

        if (title.isBlank()) {
            error = "Название книги не может быть пустым."
            viewModelScope.launch { _saveEvent.emit(false) }
            return
        }

        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                // TODO: Реализовать загрузку обложки, если coverUri != null
                val coverUrl = "" // Пока без обложки

                val newBook = Book(
                    id = "",  // Пустой ID, Firestore сгенерирует свой
                    title = title.trim(),
                    author = author.trim(),
                    description = description.trim(),
                    coverUrl = coverUrl,
                    userId = currentUserId,
                    googleBooksId = null, // Для вручную добавленных книг GoogleBooksId не используется
                    genre = "", // Поле genre есть в Book
                    pageCount = 0, // Поле pageCount есть в Book
                    status = "library", // Поле status есть в Book
                    isRead = false,
                    readDate = null,
                    isFavorite = false,
                    rating = 0f, // Инициализируем рейтинг как Float
                    isInWishlist = false,
                    titleLowercase = title.trim().lowercase(),
                    authorLowercase = author.trim().lowercase(),
                    addedDate = null // addedDate установит сервер
                )
                // Используем addBook, который добавит в основную коллекцию
                // и установит titleLowercase/authorLowercase
                bookRepository.addBook(newBook)
                isLoading = false
                _saveEvent.emit(true)
            } catch (e: Exception) {
                isLoading = false
                error = "Не удалось сохранить книгу: ${e.message}"
                _saveEvent.emit(false)
            }
        }
    }
} 