package com.example.readingroom.ui.auth

// Удаляем импорт FirebaseUser, если он больше не нужен напрямую
// import com.google.firebase.auth.FirebaseUser 
// import com.example.readingroom.data.model.User // Неправильный путь
import com.example.readingroom.model.User // Правильный импорт нашего User

/**
 * Представляет состояние аутентификации.
 */
sealed class AuthState {
    object Loading : AuthState() // Состояние загрузки
    // Изменяем тип user на наш User
    data class Authenticated(val user: User) : AuthState() // Пользователь аутентифицирован
    object Unauthenticated : AuthState() // Пользователь не аутентифицирован
    data class Error(val message: String) : AuthState() // Ошибка аутентификации
} 