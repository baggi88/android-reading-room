package com.example.readingroom.ui.screens.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
import com.example.readingroom.data.BookStatus
import com.example.readingroom.model.GenreData
import com.example.readingroom.model.StatisticsUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана статистики
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StatisticsUiState(
            totalBooks = 0,
            readBooks = 0,
            genreDistribution = emptyList(),
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        Log.d("StatisticsViewModel", "ViewModel initialized")
        loadCurrentUserStatistics()
    }

    fun loadCurrentUserStatistics() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            loadUserStatistics(currentUserId)
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Пользователь не вошел в систему"
            )
        }
    }

    fun loadUserStatistics(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // Собираем поток книг пользователя
            bookRepository.getAllBooks(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки статистики: ${e.message}"
                    )
                }
                .collect { books -> // Обработка успешного получения списка книг
                    // Подсчет общего количества книг
                    val totalBooks = books.size

                    // Подсчет прочитанных книг (используем поле isRead)
                    val readBooks = books.count { it.isRead }

                    // Логируем рассчитанные значения
                    Log.d("StatisticsViewModel", "Calculated stats: totalBooks=$totalBooks, readBooks=$readBooks")

                    /* // Комментируем блок с жанрами
                    val genresMap = mutableMapOf<String, Int>()
                    books.forEach { book ->
                        book.genre?.let { genre -> 
                            if (genre.isNotBlank()) { 
                                genresMap[genre] = genresMap.getOrDefault(genre, 0) + 1
                            }
                        }
                    }
                    val genreDistribution = genresMap.map { (genre, count) ->
                        GenreData(genre, count)
                    }.sortedByDescending { it.count }
                    */

                    _uiState.value = _uiState.value.copy(
                        totalBooks = totalBooks,
                        readBooks = readBooks,
                        // genreDistribution = genreDistribution, // Удаляем передачу
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
} 