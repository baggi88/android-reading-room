package com.example.readingroom.ui.screens.friends

import com.example.readingroom.model.User

/**
 * Представление пользователя для UI списка друзей/поиска.
 */
data class FriendsUserUiModel(
    val uid: String,
    val nickname: String,
    val avatarUrl: String,
    val isAdding: Boolean = false, // Флаг загрузки добавления
    val isRemoving: Boolean = false // Флаг загрузки удаления
)

/**
 * Состояние UI для экрана друзей.
 */
data class FriendsUiState(
    val isLoadingFriends: Boolean = true,
    val friendsList: List<FriendsUserUiModel> = emptyList(),
    val friendsError: String? = null,

    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<FriendsUserUiModel> = emptyList(),
    val searchError: String? = null
)

// Extension функция для маппинга User в FriendsUserUiModel
fun User.toFriendsUserUiModel(): FriendsUserUiModel {
    return FriendsUserUiModel(
        uid = this.uid,
        nickname = this.nickname,
        avatarUrl = this.avatarUrl
        // isAdding и isRemoving будут false по умолчанию
    )
} 