package com.example.readingroom.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.readingroom.ui.theme.ThemeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Объявляем DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeType: ThemeType,
    val monthlyGoal: Int,
    val semiAnnualGoal: Int,
    val annualGoal: Int
    // TODO: Добавить другие настройки по мере необходимости
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG: String = "UserPreferencesRepo"

    // Ключи для хранения настроек
    private object PreferencesKeys {
        val THEME_TYPE = stringPreferencesKey("theme_type")
        val MONTHLY_GOAL = intPreferencesKey("monthly_goal")
        val SEMI_ANNUAL_GOAL = intPreferencesKey("semi_annual_goal")
        val ANNUAL_GOAL = intPreferencesKey("annual_goal")
        // TODO: Добавить ключи для других настроек
    }

    // Flow для отслеживания изменений настроек
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            // Обработка ошибок чтения DataStore
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences()) // Возвращаем пустые настройки в случае ошибки
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Считываем имя темы, по умолчанию "LIGHT"
            val themeName = preferences[PreferencesKeys.THEME_TYPE] ?: ThemeType.LIGHT.name
            // Преобразуем имя в ThemeType, если не получится - используем LIGHT
            val themeType = try {
                ThemeType.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                 Log.w(TAG, "Invalid theme name '$themeName' in preferences, defaulting to LIGHT.")
                 ThemeType.LIGHT
            }

            // Считываем цели, если их нет - используем значения по умолчанию
            val monthlyGoal = preferences[PreferencesKeys.MONTHLY_GOAL] ?: 5
            val semiAnnualGoal = preferences[PreferencesKeys.SEMI_ANNUAL_GOAL] ?: 10
            val annualGoal = preferences[PreferencesKeys.ANNUAL_GOAL] ?: 50
            
            UserPreferences(
                themeType = themeType,
                monthlyGoal = monthlyGoal,
                semiAnnualGoal = semiAnnualGoal,
                annualGoal = annualGoal
            )
        }

    // Функция для обновления настройки темы
    suspend fun updateThemeType(themeType: ThemeType) {
         Log.d(TAG, "Updating theme type to: ${themeType.name}")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_TYPE] = themeType.name
        }
    }

    // Функции для обновления целей
    suspend fun updateMonthlyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_GOAL] = goal
        }
    }

    suspend fun updateSemiAnnualGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEMI_ANNUAL_GOAL] = goal
        }
    }

    suspend fun updateAnnualGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANNUAL_GOAL] = goal
        }
    }

    // TODO: Добавить функции для обновления других настроек
} 