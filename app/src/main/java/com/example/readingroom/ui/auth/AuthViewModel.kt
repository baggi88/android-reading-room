package com.example.readingroom.ui.auth

import android.util.Patterns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.data.UserRepository
import com.example.readingroom.model.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository // Для создания пользователя в Firestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Состояние для операции сброса пароля
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    private val minPasswordLength = 6 // Минимальная длина пароля
    // Объявляем TAG
    private val TAG = "AuthViewModel"

    init {
        // checkCurrentUser() // Убираем прямой вызов, так как слушатель сделает то же самое
        // Устанавливаем слушатель состояния аутентификации
        setupAuthStateListener()
    }

    // Устанавливаем слушатель состояния аутентификации
    private fun setupAuthStateListener() {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            viewModelScope.launch {
                if (firebaseUser != null) {
                    // Пользователь вошел (или уже был) - пытаемся загрузить профиль
                    Log.d(TAG, "AuthStateListener: User detected (UID: ${firebaseUser.uid}). Fetching profile...")
                     // Оборачиваем в try-catch на случай ошибок сети/Firestore
                    try {
                        val userProfile = userRepository.getUser(firebaseUser.uid)
                        if (userProfile != null) {
                            Log.d(TAG, "AuthStateListener: Profile found. Setting state to Authenticated.")
                            // Если текущее состояние еще не Authenticated с этим же юзером, обновляем
                            if (!(_authState.value is AuthState.Authenticated && (_authState.value as AuthState.Authenticated).user.uid == userProfile.uid)) {
                                _authState.value = AuthState.Authenticated(userProfile)
                            }
                        } else {
                            // Пользователь есть в Auth, но нет в Firestore - странная ситуация
                            Log.w(TAG, "AuthStateListener: User authenticated (UID: ${firebaseUser.uid}) but profile not found in Firestore. Setting state to Error.")
                            _authState.value = AuthState.Error("Профиль пользователя не найден после входа.")
                            // Можно также вызвать signOut(), чтобы привести состояние в консистентный вид
                            // auth.signOut() 
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "AuthStateListener: Error fetching user profile for UID: ${firebaseUser.uid}", e)
                         // Ошибка при загрузке профиля, считаем пользователя неаутентифицированным
                        _authState.value = AuthState.Error("Ошибка загрузки профиля: ${e.message}")
                    }
                } else {
                    // Пользователь вышел
                    Log.d(TAG, "AuthStateListener: No user detected. Setting state to Unauthenticated.")
                    if (_authState.value !is AuthState.Unauthenticated) { // Обновляем, только если состояние изменилось
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
        }
    }

    // Убираем checkCurrentUser, так как теперь есть listener
    /*
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Ошибка была здесь: передавали FirebaseUser вместо User
            // _authState.value = AuthState.Authenticated(currentUser) 
             viewModelScope.launch {
                 try {
                    val userProfile = userRepository.getUser(currentUser.uid)
                    if (userProfile != null) {
                         _authState.value = AuthState.Authenticated(userProfile)
                    } else {
                        _authState.value = AuthState.Error("Профиль не найден для существующего пользователя Auth")
                    }                 
                 } catch (e: Exception) {
                     _authState.value = AuthState.Error("Ошибка загрузки профиля: ${e.message}")
                 }
             }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    */

    // Функция регистрации
    fun signUp(email: String, password: String, nickname: String) {
        // Если уже идет загрузка аутентификации, ничего не делаем
        if (_authState.value is AuthState.Loading) {
            Log.w(TAG, "SignUp called while already loading")
            return
        }
        
        // Валидация
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Некорректный формат email")
            return
        }
        if (password.length < minPasswordLength) {
            _authState.value = AuthState.Error("Пароль должен содержать не менее $minPasswordLength символов")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val newUser = User(uid = firebaseUser.uid, nickname = nickname)
                    Log.d(TAG, "Attempting to create user document in Firestore for UID: ${firebaseUser.uid} with nickname: $nickname")
                    userRepository.updateUser(newUser)
                    Log.d(TAG, "Firestore updateUser called for UID: ${firebaseUser.uid}")
                    _authState.value = AuthState.Authenticated(newUser)
                    Log.d(TAG, "SignUp successful. State set to Authenticated with User object.")
                } else {
                     Log.e(TAG, "SignUp: Firebase user creation succeeded but user object is null.")
                    _authState.value = AuthState.Error("Не удалось создать пользователя (null user object)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign up or Firestore update", e)
                _authState.value = AuthState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    // Функция входа
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email и пароль не могут быть пустыми")
            return
        }
        _authState.value = AuthState.Loading
        Log.i(TAG, "Logging in as $email with empty reCAPTCHA token")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                viewModelScope.launch { 
                    if (task.isSuccessful) {
                        Log.d(TAG, "Firebase Sign In successful. Fetching user profile...")
                        // Явно получаем пользователя и его профиль ЗДЕСЬ,
                        // так как AuthStateListener может не сработать сразу.
                        val firebaseUser = task.result?.user
                        if (firebaseUser != null) {
                            try {
                                val userProfile = userRepository.getUser(firebaseUser.uid)
                                if (userProfile != null) {
                                    Log.d(TAG, "Sign In: User profile found immediately. Setting state to Authenticated.")
                                    _authState.value = AuthState.Authenticated(userProfile)
                                } else {
                                    Log.w(TAG, "Sign In: User authenticated (UID: ${firebaseUser.uid}) but profile not found in Firestore. Setting state to Error.")
                                    _authState.value = AuthState.Error("Профиль пользователя не найден. Возможно, регистрация не была завершена.")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Sign In: Error fetching user profile after login for UID: ${firebaseUser.uid}", e)
                                _authState.value = AuthState.Error("Ошибка загрузки профиля после входа: ${e.message}")
                            }
                        } else {
                            Log.e(TAG, "Sign In: Firebase task successful but user is null.")
                            _authState.value = AuthState.Error("Ошибка входа: пользователь не найден после успешной аутентификации.")
                        }
                    } else {
                        Log.e(TAG, "Firebase Sign In failed", task.exception)
                        val errorMessage = task.exception?.message ?: "Неизвестная ошибка входа"
                        _authState.value = AuthState.Error("ОШИБКА FIREBASE: $errorMessage")
                    }
                }
            }
    }

    // Функция сброса пароля
    fun sendPasswordReset(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetPasswordState.value = ResetPasswordState.Error("Некорректный формат email")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _resetPasswordState.value = ResetPasswordState.Success("Письмо для сброса пароля отправлено на $email")
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "Ошибка при отправке письма")
            }
        }
    }

    // Сброс состояния сброса пароля (чтобы сообщение не висело вечно)
    fun resetPasswordResetState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    // Функция выхода
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

// Добавляем отдельное состояние для сброса пароля
sealed class ResetPasswordState {
    object Idle : ResetPasswordState() // Начальное состояние / после закрытия диалога
    object Loading : ResetPasswordState() // Отправка письма
    data class Success(val message: String) : ResetPasswordState() // Успех
    data class Error(val message: String) : ResetPasswordState() // Ошибка
} 