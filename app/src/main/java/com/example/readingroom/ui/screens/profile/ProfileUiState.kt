package com.example.readingroom.ui.screens.profile

import com.example.readingroom.model.User
import com.example.readingroom.model.Book

/**
 * Состояние UI для экрана профиля.
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false, // Добавляем поле для индикации обновления
    val user: User? = null,
    val error: String? = null, // Переименовываем errorMessage в error
    val isCurrentUserProfile: Boolean, // Определяет, смотрим свой профиль или чужой
    val allowAddingByNickname: Boolean? = null // Добавляем поле для настройки приватности
    // Убираем ненужные поля:
    // val isLoadingBooks: Boolean = false,
    // val books: List<Book> = emptyList(),
    // val nicknameInput: String = "",
    // val isNicknameValid: Boolean = true,
    // val successMessage: String? = null,
    // val allowAddingSetting: Boolean? = null
) 