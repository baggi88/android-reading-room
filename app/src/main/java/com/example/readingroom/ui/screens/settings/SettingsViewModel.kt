package com.example.readingroom.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.UserPreferencesRepository
import com.example.readingroom.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class SettingsUiState(
    val monthlyGoal: String = "",
    val semiAnnualGoal: String = "",
    val annualGoal: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
    // TODO: Добавить другие настройки (уведомления, цели и т.д.)
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Получаем Flow с UserPreferences и извлекаем только themeType
    val selectedTheme: StateFlow<ThemeType> = userPreferencesRepository.userPreferencesFlow
        .map { it.themeType } // Преобразуем Flow<UserPreferences> в Flow<ThemeType>
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeType.LIGHT 
        )

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Ошибка загрузки настроек: ${e.message}") }
                }
                .collect { preferences ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            monthlyGoal = preferences.monthlyGoal.toString(),
                            semiAnnualGoal = preferences.semiAnnualGoal.toString(),
                            annualGoal = preferences.annualGoal.toString()
                        )
                    }
                }
        }
    }

    fun onMonthlyGoalChange(value: String) {
        _uiState.update { it.copy(monthlyGoal = value) }
        val intValue = value.toIntOrNull()
        if (intValue != null) {
            viewModelScope.launch {
                try {
                    userPreferencesRepository.updateMonthlyGoal(intValue)
                } catch (e: Exception) { 
                    _uiState.update { it.copy(error = "Ошибка сохранения цели: ${e.message}") }
                }
            }
        } else if (value.isNotEmpty()) { 
             _uiState.update { it.copy(error = "Неверное число для месячной цели") }
        }
    }

    fun onSemiAnnualGoalChange(value: String) {
        _uiState.update { it.copy(semiAnnualGoal = value) }
        val intValue = value.toIntOrNull()
        if (intValue != null) {
             viewModelScope.launch {
                 try {
                     userPreferencesRepository.updateSemiAnnualGoal(intValue)
                 } catch (e: Exception) { 
                     _uiState.update { it.copy(error = "Ошибка сохранения цели: ${e.message}") }
                 }
            }
        } else if (value.isNotEmpty()) {
            _uiState.update { it.copy(error = "Неверное число для полугодовой цели") }
        }
    }

     fun onAnnualGoalChange(value: String) {
        _uiState.update { it.copy(annualGoal = value) }
        val intValue = value.toIntOrNull()
        if (intValue != null) {
             viewModelScope.launch {
                 try {
                     userPreferencesRepository.updateAnnualGoal(intValue)
                 } catch (e: Exception) { 
                     _uiState.update { it.copy(error = "Ошибка сохранения цели: ${e.message}") }
                 }
            }
        } else if (value.isNotEmpty()) {
            _uiState.update { it.copy(error = "Неверное число для годовой цели") }
        }
    }

    // Функция для обновления темы
    fun updateTheme(newTheme: ThemeType) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeType(newTheme)
        }
    }

    // TODO: Добавить функции для изменения других настроек
} 