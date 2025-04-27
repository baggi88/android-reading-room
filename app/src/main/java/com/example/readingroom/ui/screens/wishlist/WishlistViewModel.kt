package com.example.readingroom.ui.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository // Предполагаем, что BookRepository есть
import com.example.readingroom.model.Book
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

// Состояние UI для экрана Вишлиста
data class WishlistUiState(
    val wishlistBooks: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val bookRepository: BookRepository, // Запрашиваем репозиторий
    private val auth: FirebaseAuth // Для получения ID текущего пользователя
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Пользователь не авторизован")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // TODO: Нужен метод в BookRepository для получения книг из вишлиста!
            // Например: bookRepository.getWishlistBooks(userId)
            // Пока что используем getAllBooks и фильтруем
            bookRepository.getAllBooks(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки: ${e.message}")
                }
                .collect { allBooks ->
                    val wishlist = allBooks.filter { it.isInWishlist } // Фильтруем по флагу isInWishlist
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        wishlistBooks = wishlist
                    )
                }
        }
    }

    // Переименовываем обратно в removeFromWishlist
    fun removeFromWishlist(book: Book) {
        viewModelScope.launch {
            try {
                // Просто убираем флаг isInWishlist
                val updatedBook = book.copy(isInWishlist = false) 
                bookRepository.updateBook(updatedBook)
                // Сообщение об успехе можно добавить через SharedFlow, если нужно
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(error = "Ошибка удаления из вишлиста: ${e.message}")
            }
        }
    }
} 