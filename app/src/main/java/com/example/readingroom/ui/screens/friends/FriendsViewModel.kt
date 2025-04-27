package com.example.readingroom.ui.screens.friends

import android.util.Log // Импорт для логгирования
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.UserRepository
import com.example.readingroom.model.User // <<< Добавляем импорт User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Импортируем все из flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.readingroom.ui.screens.friends.toFriendsUserUiModel // <<< Добавляем импорт для extension-функции

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()
    private val TAG = "FriendsViewModel"

    init {
        observeFriendsList()
    }

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Загрузка и наблюдение за списком друзей
    private fun observeFriendsList() {
        val userId = getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(isLoadingFriends = false, friendsError = "Ошибка: Пользователь не авторизован") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFriends = true, friendsError = null) }
            // Подписываемся на Flow друзей
            userRepository.getFriends(userId)
                .catch { e -> // Обработка ошибок Flow
                    Log.e(TAG, "Error observing friends", e)
                    _uiState.update {
                        it.copy(
                            isLoadingFriends = false,
                            friendsError = "Ошибка загрузки друзей: ${e.message}"
                        )
                    }
                }
                .collect { friends: List<User> -> // Теперь User должен разрешиться
                     Log.d(TAG, "Received updated friends list: ${friends.size} users")
                    val friendsUiModel = friends.distinctBy { it.uid }.map { it.toFriendsUserUiModel() } // Добавили distinctBy + map
                    _uiState.update { it.copy(isLoadingFriends = false, friendsList = friendsUiModel) }
                }
        }
    }

    // Обновление строки поиска
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchError = null) }
        // Инициируем поиск при изменении текста (можно добавить debounce)
        searchUsers(query)
    }

    // Выполнение поиска по никнейму
    private fun searchUsers(query: String) { // Принимает query как параметр
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList(), searchError = null, isSearching = false) } // Сбрасываем результаты и ошибку
            return
        }

        val currentUserId = getCurrentUserId()

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null) }
             // Подписываемся на Flow результатов поиска
            userRepository.findUsersByNickname(trimmedQuery)
                 .catch { e ->
                     Log.e(TAG, "Error searching users for query: $trimmedQuery", e)
                     _uiState.update {
                         it.copy(
                             isSearching = false,
                             searchError = "Ошибка поиска: ${e.message}"
                         )
                     }
                 }
                .collect { results: List<User> -> // Теперь User должен разрешиться
                    Log.d(TAG, "Received search results for query '$trimmedQuery': ${results.size} users")
                     // Маппим List<User> в List<FriendsUserUiModel> перед фильтрацией
                    val resultsUiModel = results.map { it.toFriendsUserUiModel() } // Теперь map должен работать
                    val friendsIds = _uiState.value.friendsList.map { it.uid }.toSet()
                    val filteredResults = resultsUiModel.filter { it.uid != currentUserId && it.uid !in friendsIds }
                    _uiState.update { it.copy(isSearching = false, searchResults = filteredResults) }
                     if (filteredResults.isEmpty() && results.isNotEmpty()) {
                         // Если были результаты, но все отфильтрованы
                         _uiState.update { it.copy(searchError = "Все найденные пользователи уже друзья или это вы") }
                     } else if (results.isEmpty()) {
                         // Если сам поиск ничего не нашел
                         _uiState.update { it.copy(searchError = "Пользователи с ником '$trimmedQuery' не найдены") }
                     }
                }
        }
    }

    // Добавление пользователя в друзья
    fun addFriend(friendId: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(searchError = "Ошибка: Не удалось определить пользователя") } // Показываем ошибку в поиске
            return
        }

        // Доп. проверка, хотя поиск уже должен фильтровать
        if (_uiState.value.friendsList.any { it.uid == friendId }) {
            _uiState.update { it.copy(searchError = "Пользователь уже в друзьях") }
            return
        }

        viewModelScope.launch {
            // Можно добавить индикатор загрузки для конкретной строки
             _uiState.update { state ->
                state.copy(searchResults = state.searchResults.map { 
                    if (it.uid == friendId) it.copy(isAdding = true) else it // Добавляем флаг загрузки к User в UIState
                })
            }
            try {
                userRepository.addFriend(userId, friendId)
                 Log.d(TAG, "Successfully added friend $friendId for user $userId")
                // Список друзей обновится автоматически благодаря Flow в observeFriendsList
                 // Убираем добавленного пользователя из результатов поиска и ОПТИМИСТИЧНО добавляем в основной список
                 _uiState.update { currentState ->
                    // Находим данные добавленного друга
                    val friendToAdd = currentState.searchResults.find { it.uid == friendId }?.copy(isAdding = false)
                    
                    // Обновляем состояние, только если нашли друга в результатах поиска
                    // И проверяем, что его еще нет в списке друзей
                    if (friendToAdd != null) {
                        val existingFriendIds = currentState.friendsList.map { it.uid }.toSet()
                        
                        // Если друг уже в списке, не добавляем дубликат
                        if (friendToAdd.uid in existingFriendIds) {
                            currentState.copy(
                                searchResults = currentState.searchResults.filterNot { it.uid == friendId },
                                searchError = null 
                            )
                        } else {
                            // Добавляем друга только если его еще нет в списке
                            currentState.copy(
                                searchResults = currentState.searchResults.filterNot { it.uid == friendId },
                                friendsList = (currentState.friendsList + friendToAdd).distinctBy { it.uid },
                                searchError = null 
                            )
                        }
                    } else {
                         // Если друга не нашли (странно, но возможно), просто убираем его из поиска 
                         // и полагаемся на обновление friendsList через Flow
                        currentState.copy(
                             searchResults = currentState.searchResults.filterNot { it.uid == friendId },
                             searchError = null
                        )
                    }
                }
            } catch (e: Exception) {
                 Log.e(TAG, "Error adding friend $friendId for user $userId", e)
                 _uiState.update { state ->
                     state.copy(
                         searchError = "Ошибка добавления: ${e.message}",
                         searchResults = state.searchResults.map { // Убираем индикатор загрузки при ошибке
                             if (it.uid == friendId) it.copy(isAdding = false) else it
                         }
                     )
                 }
            }
        }
    }

    // Удаление друга
    fun removeFriend(friendId: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(friendsError = "Ошибка: Пользователь не авторизован") }
            return
        }

        viewModelScope.launch {
             _uiState.update { state -> // Показываем загрузку для конкретного друга
                 state.copy(friendsList = state.friendsList.map {
                      if (it.uid == friendId) it.copy(isRemoving = true) else it // Добавляем флаг загрузки
                 })
             }
            try {
                userRepository.removeFriend(userId, friendId)
                 Log.d(TAG, "Successfully removed friend $friendId for user $userId")
                // Список обновится автоматически через Flow
            } catch (e: Exception) {
                 Log.e(TAG, "Error removing friend $friendId for user $userId", e)
                _uiState.update { state ->
                    state.copy(
                        friendsError = "Ошибка удаления: ${e.message}",
                         friendsList = state.friendsList.map { // Убираем индикатор при ошибке
                             if (it.uid == friendId) it.copy(isRemoving = false) else it
                         }
                    )
                 }
            }
        }
    }
}

// Удаляем весь блок ниже, так как он дублируется в других файлах
/*
// Добавляем флаги isLoading в модель User для UI
// Лучше вынести UserUiModel в отдельный файл или определить здесь
data class FriendsUserUiModel(
    val uid: String,
    val nickname: String,
    val avatarUrl: String,
    val isAdding: Boolean = false,
    val isRemoving: Boolean = false
)

// Обновляем FriendsUiState
data class FriendsUiState(
    val isLoadingFriends: Boolean = true,
    val friendsList: List<FriendsUserUiModel> = emptyList(), // Используем новую модель
    val friendsError: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<FriendsUserUiModel> = emptyList(), // Используем новую модель
    val searchError: String? = null
)

// Добавляем функции-мапперы (можно в ViewModel или как extension)
fun User.toFriendsUserUiModel(): FriendsUserUiModel {
    return FriendsUserUiModel(
        uid = this.uid,
        nickname = this.nickname,
        avatarUrl = this.avatarUrl
    )
}
*/ 