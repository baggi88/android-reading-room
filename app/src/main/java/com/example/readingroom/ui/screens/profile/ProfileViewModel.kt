package com.example.readingroom.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.UserRepository
import com.example.readingroom.model.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileUserId: String? = savedStateHandle.get<String>("userId")
    private val currentUserId: String? = auth.currentUser?.uid
    private val TAG = "ProfileViewModel"

    private val _uiState = MutableStateFlow(ProfileUiState(
        isCurrentUserProfile = profileUserId == null || profileUserId == currentUserId
    ))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "Initializing ProfileViewModel for profileUserId: $profileUserId, currentUserId: $currentUserId")
        loadUserData()
    }

    private fun loadUserData() {
        val userIdToLoad = profileUserId ?: currentUserId
        Log.d(TAG, "loadUserData called for userIdToLoad: $userIdToLoad")
        if (userIdToLoad == null) {
            _uiState.update { it.copy(isLoading = false, error = "Не удалось определить ID пользователя") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(userIdToLoad)
                if (user != null) {
                    _uiState.update { it.copy(isLoading = false, user = user, allowAddingByNickname = user.allowAddingByNickname) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Не удалось загрузить профиль") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data for $userIdToLoad", e)
                _uiState.update { it.copy(isLoading = false, error = "Ошибка загрузки профиля: ${e.message}") }
            }
        }
    }

    fun updateNickname(newNickname: String) {
        val currentUser = _uiState.value.user
        if (!_uiState.value.isCurrentUserProfile || currentUser == null || currentUser.uid != currentUserId) {
            _uiState.update { it.copy(error = "Невозможно изменить чужой никнейм") }
            return
        }
        if (newNickname.length < 3) {
            _uiState.update { it.copy(error = "Никнейм должен быть не менее 3 символов") }
            return
        }

        _uiState.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                val isAvailable = userRepository.isNicknameAvailable(newNickname)
                if (!isAvailable) {
                    _uiState.update { it.copy(isUpdating = false, error = "Никнейм '$newNickname' уже занят") }
                    return@launch
                }
                
                val updatedUser = currentUser.copy(nickname = newNickname)
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(isUpdating = false, user = updatedUser, error = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating nickname for $currentUserId", e)
                _uiState.update { it.copy(isUpdating = false, error = "Ошибка обновления никнейма: ${e.message}") }
            }
        }
    }

    fun updateAvatar(uri: Uri?) {
        val currentUser = _uiState.value.user
        if (!_uiState.value.isCurrentUserProfile || currentUser == null || currentUser.uid != currentUserId || uri == null) {
            _uiState.update { it.copy(error = "Ошибка при обновлении аватара") }
            return
        }
        
        _uiState.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                val downloadUrl = userRepository.uploadUserAvatar(currentUser.uid, uri)
                
                val updatedUser = currentUser.copy(avatarUrl = downloadUrl)
                userRepository.updateUser(updatedUser)
                
                _uiState.update { it.copy(isUpdating = false, user = updatedUser, error = null) }

            } catch (e: Exception) { 
                 Log.e(TAG, "Error updating avatar", e)
                _uiState.update { it.copy(isUpdating = false, error = "Ошибка обновления аватара: ${e.message}") }
            }
        }
    }

    fun updateAllowAddingByNickname(allow: Boolean) {
        val currentUser = _uiState.value.user
        if (!_uiState.value.isCurrentUserProfile || currentUser == null || currentUser.uid != currentUserId) {
            _uiState.update { it.copy(error = "Невозможно изменить чужие настройки") }
            return
        }
        if (currentUser.allowAddingByNickname == allow) return

        _uiState.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(allowAddingByNickname = allow)
                userRepository.updateUser(updatedUser)
                _uiState.update { it.copy(isUpdating = false, user = updatedUser, allowAddingByNickname = allow, error = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating allowAddingByNickname for $currentUserId", e)
                _uiState.update { it.copy(isUpdating = false, allowAddingByNickname = currentUser.allowAddingByNickname, error = "Ошибка обновления настройки: ${e.message}") }
            }
        }
    }
} 