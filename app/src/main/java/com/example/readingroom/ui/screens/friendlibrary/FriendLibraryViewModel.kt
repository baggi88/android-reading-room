package com.example.readingroom.ui.screens.friendlibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
import com.example.readingroom.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendLibraryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val friendBooks: List<Book> = emptyList()
)

@HiltViewModel
class FriendLibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String = savedStateHandle.get<String>("userId") ?: ""

    private val _uiState = MutableStateFlow(FriendLibraryUiState())
    val uiState: StateFlow<FriendLibraryUiState> = _uiState.asStateFlow()

    init {
        loadFriendBooks()
    }

    private fun loadFriendBooks() {
        if (friendId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "ID друга не найден") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Загружаем только книги пользователя
                bookRepository.getBooksByUserId(friendId)
                    .catch { e ->
                         _uiState.update { state ->
                            state.copy(isLoading = false, error = "Ошибка загрузки книг: ${e.message}")
                        }
                    }
                    .collectLatest { books ->
                        _uiState.update { state ->
                            state.copy(isLoading = false, friendBooks = books)
                        }
                    }

            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, error = "Ошибка загрузки библиотеки друга: ${e.message}") }
            }
        }
    }
}