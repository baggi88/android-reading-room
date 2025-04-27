package com.example.readingroom.model

/**
 * Данные о жанре для статистики
 */
data class GenreData(val name: String, val count: Int)

/**
 * Состояние пользовательского интерфейса для экрана статистики
 */
data class StatisticsUiState(
    val totalBooks: Int = 0,
    val readBooks: Int = 0,         // Общее количество прочитанных
    val readThisMonth: Int = 0,    // Прочитано в текущем месяце
    val readThisYear: Int = 0,     // Прочитано в текущем году
    val monthlyGoal: Int? = null,  // Цель на месяц
    val semiAnnualGoal: Int? = null, // Цель на полгода
    val annualGoal: Int? = null,   // Цель на год
    val genreDistribution: List<GenreData> = emptyList(),
    val readerStatus: ReaderStatus = ReaderStatus.ZERO_BOOKS,
    val collectionStatus: CollectionStatus = CollectionStatus.ZERO_COLL,
    val progressToNextReaderMilestone: Float = 0f,
    val progressToNextCollectionMilestone: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
) 