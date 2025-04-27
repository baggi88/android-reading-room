package com.example.readingroom.ui.screens.reading_stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.BookRepository
import com.example.readingroom.data.UserPreferencesRepository
import com.example.readingroom.data.UserPreferences
import com.example.readingroom.model.Book
import com.example.readingroom.model.GenreData
import com.example.readingroom.model.StatisticsUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import com.example.readingroom.model.ReaderStatus
import com.example.readingroom.model.CollectionStatus

/**
 * ViewModel для отображения статистики чтения
 */
@HiltViewModel
class ReadingStatsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUserStatisticsAndGoals()
    }

    fun loadCurrentUserStatisticsAndGoals() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || currentUserId.isBlank()) {
            _uiState.value = StatisticsUiState(isLoading = false, error = "Пользователь не вошел в систему")
            return
        }

        viewModelScope.launch {
            _uiState.value = StatisticsUiState(isLoading = true)
            try {
                combine( 
                    bookRepository.getAllBooks(currentUserId),
                    userPreferencesRepository.userPreferencesFlow 
                ) { books, preferences ->
                    processBooksAndUpdateState(books, preferences)
                }.catch { e ->
                    Log.e("ReadingStatsVM", "Error loading data", e)
                    _uiState.value = StatisticsUiState(isLoading = false, error = "Ошибка загрузки данных: ${e.message}")
                }.collect()
                 
            } catch (e: Exception) {
                 Log.e("ReadingStatsVM", "Error combining flows", e)
                 _uiState.value = StatisticsUiState(isLoading = false, error = "Ошибка загрузки данных: ${e.message}")
            }
        }
    }

    private fun processBooksAndUpdateState(books: List<Book>, preferences: UserPreferences) {
         val totalBooks = books.size
         val readBooks = books.count { it.isRead }

         val calendar = Calendar.getInstance()
         val currentMonth = calendar.get(Calendar.MONTH)
         val currentYear = calendar.get(Calendar.YEAR)

         val readThisMonth = books.count { 
             it.isRead && it.readDate?.toDate()?.let { readDate -> 
                 val bookCalendar = Calendar.getInstance()
                 bookCalendar.time = readDate
                 bookCalendar.get(Calendar.MONTH) == currentMonth && 
                 bookCalendar.get(Calendar.YEAR) == currentYear
             } ?: false
         }

         val readThisYear = books.count { 
             it.isRead && it.readDate?.toDate()?.let { readDate -> 
                 val bookCalendar = Calendar.getInstance()
                 bookCalendar.time = readDate
                 bookCalendar.get(Calendar.YEAR) == currentYear
             } ?: false
         }

         /*
         val genresMap = mutableMapOf<String, Int>()
         books.forEach { book ->
             if (book.genre.isNotBlank()) {
                 genresMap[book.genre] = genresMap.getOrDefault(book.genre, 0) + 1
             }
         }
         val genreDistribution = genresMap.map { (genre, count) ->
             GenreData(genre, count)
         }.sortedByDescending { it.count }
         */
         
         val readerStatus = ReaderStatus.fromBookCount(readBooks)
         val collectionStatus = CollectionStatus.fromBookCount(totalBooks)
         
         val progressToNextReader: Float = if (readerStatus.nextMilestone > 0 && readerStatus.minBooks < readerStatus.nextMilestone) {
             val currentMilestoneBooks = readBooks - readerStatus.minBooks
             val booksForNextMilestone = readerStatus.nextMilestone - readerStatus.minBooks
             currentMilestoneBooks.toFloat() / booksForNextMilestone.toFloat()
         } else {
             1f
         }.coerceIn(0f, 1f)

         val progressToNextCollection: Float = if (collectionStatus.nextMilestone > 0 && collectionStatus.minBooks < collectionStatus.nextMilestone) {
             val currentMilestoneBooks = totalBooks - collectionStatus.minBooks
             val booksForNextMilestone = collectionStatus.nextMilestone - collectionStatus.minBooks
             currentMilestoneBooks.toFloat() / booksForNextMilestone.toFloat()
         } else {
             1f
         }.coerceIn(0f, 1f)

         val monthlyGoal = preferences.monthlyGoal
         val semiAnnualGoal = preferences.semiAnnualGoal
         val annualGoal = preferences.annualGoal

         _uiState.value = StatisticsUiState(
             totalBooks = totalBooks,
             readBooks = readBooks,
             readThisMonth = readThisMonth,
             readThisYear = readThisYear,
             monthlyGoal = monthlyGoal,
             semiAnnualGoal = semiAnnualGoal,
             annualGoal = annualGoal,
             // genreDistribution = genreDistribution,
             readerStatus = readerStatus,
             collectionStatus = collectionStatus,
             progressToNextReaderMilestone = progressToNextReader,
             progressToNextCollectionMilestone = progressToNextCollection,
             isLoading = false,
             error = null
         )
    }
} 