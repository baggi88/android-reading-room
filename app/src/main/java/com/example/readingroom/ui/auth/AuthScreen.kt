package com.example.readingroom.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    // Состояние для диалога сброса пароля
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    // Получаем состояние сброса из ViewModel
    val resetState by authViewModel.resetPasswordState.collectAsState()

    // Сбрасываем состояние ViewModel при закрытии диалога
    DisposableEffect(showResetDialog) {
        onDispose {
            if (!showResetDialog) {
                authViewModel.resetPasswordResetState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Вход", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.signIn(email, password) },
            enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Войти")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка "Забыли пароль?"
        TextButton(onClick = { 
            resetEmail = email // Предзаполняем email из поля ввода
            authViewModel.resetPasswordResetState() // Сбрасываем предыдущее состояние
            showResetDialog = true 
        }) {
            Text("Забыли пароль?")
        }

        TextButton(onClick = onNavigateToSignUp) {
            Text("Нет аккаунта? Зарегистрироваться")
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    // Диалоговое окно для сброса пароля
    if (showResetDialog) {
        val isLoading = resetState is ResetPasswordState.Loading
        var statusMessage: String? = null
        var isError = false
        when (resetState) {
            is ResetPasswordState.Success -> {
                statusMessage = (resetState as ResetPasswordState.Success).message
            }
            is ResetPasswordState.Error -> {
                statusMessage = (resetState as ResetPasswordState.Error).message
                isError = true
            }
            else -> {}
        }

        AlertDialog(
            onDismissRequest = { if (!isLoading) showResetDialog = false },
            title = { Text("Сброс пароля") },
            text = {
                Column {
                    Text("Введите ваш email, чтобы получить инструкции по сбросу пароля.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        // Можно добавить isError для поля ввода на основе валидации email
                        isError = isError && statusMessage?.contains("email") == true // Пример: подсвечиваем ошибку, если она про email
                    )
                    statusMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        authViewModel.sendPasswordReset(resetEmail)
                    },
                    // TODO: Добавить более строгую валидацию email 
                    enabled = !isLoading && resetEmail.isNotBlank() 
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Отправить")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }, enabled = !isLoading) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Отображение ошибок
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.signUp(email, password, "") },
            enabled = authState != AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
             if (authState == AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Зарегистрироваться")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Уже есть аккаунт? Войти")
        }
    }
} 