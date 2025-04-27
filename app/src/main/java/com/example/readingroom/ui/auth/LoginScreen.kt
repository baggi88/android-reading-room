package com.example.readingroom.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.R
import android.widget.Toast

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()

    LaunchedEffect(authState) {
        isLoading = authState == AuthState.Loading
        if (authState is AuthState.Error) {
            error = (authState as AuthState.Error).message
        } else {
            error = null
        }
        // Навигация происходит в AppNavigation
    }

    // Отслеживаем изменения в состоянии сброса пароля
    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is ResetPasswordState.Success -> {
                Toast.makeText(context, 
                    (resetPasswordState as ResetPasswordState.Success).message, 
                    Toast.LENGTH_LONG).show()
                showResetDialog = false
                resetEmail = ""
                viewModel.resetPasswordResetState()
            }
            is ResetPasswordState.Error -> {
                Toast.makeText(context, 
                    (resetPasswordState as ResetPasswordState.Error).message, 
                    Toast.LENGTH_LONG).show()
                viewModel.resetPasswordResetState()
            }
            else -> { /* Ничего не делаем для других состояний */ }
        }
    }

    // Простой диалог для сброса пароля
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Восстановление пароля") },
            text = {
                Column {
                    Text("Введите email, указанный при регистрации. На него будет отправлена ссылка для сброса пароля.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.sendPasswordReset(resetEmail)
                    },
                    enabled = resetEmail.isNotBlank() && resetPasswordState !is ResetPasswordState.Loading
                ) {
                    if (resetPasswordState is ResetPasswordState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Обертка Box для фона
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Центрируем Column внутри Box
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Масштабируем, чтобы заполнить экран
        )

        // ---> Добавляем Card как полупрозрачный фон для контента <---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp), // Ограничиваем ширину карты
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f) // Полупрозрачный фон
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Основной контент экрана (теперь внутри Card)
            Column(
                modifier = Modifier
                    // Убираем fillMaxWidth и padding отсюда, так как они теперь у Card
                    .padding(all = 24.dp), // Внутренний padding для Card
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Вход", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.signIn(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Войти")
                    }
                }

                TextButton(onClick = onNavigateToSignUp) {
                    Text("У вас нет аккаунта? Зарегистрируйтесь")
                }
                
                TextButton(onClick = { showResetDialog = true }) {
                    Text("Забыли пароль?")
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        // <--- Конец Card <---
    }
} 