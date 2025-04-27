package com.example.readingroom.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
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

// Состояние UI для экрана Любимого
data class FavoritesUiState(
    val favoriteBooks: List<Book> = emptyList(), // Убедимся, что тип List<Book>
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Пользователь не авторизован")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // TODO: Нужен метод в BookRepository для получения любимых книг!
            // Например: bookRepository.getFavoriteBooks(userId)
            // Пока что используем getAllBooks и фильтруем
            bookRepository.getAllBooks(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки: ${e.message}")
                }
                .collect { allBooks ->
                    val favorites = allBooks.filter { it.isFavorite } // Фильтруем по флагу isFavorite
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        favoriteBooks = favorites // Убедимся, что присваиваем List<Book>
                    )
                }
        }
    }

    // Пример функции удаления из любимого
    fun removeFromFavorites(book: Book) {
        viewModelScope.launch {
            try {
                val updatedBook = book.copy(isFavorite = false)
                bookRepository.updateBook(updatedBook)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка удаления из любимого: ${e.message}")
            }
        }
    }
} 