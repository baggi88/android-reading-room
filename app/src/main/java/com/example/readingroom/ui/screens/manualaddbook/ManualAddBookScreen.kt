package com.example.readingroom.ui.screens.manualaddbook

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManualAddBookViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.saveEvent.collectLatest { success ->
            if (success) {
                Toast.makeText(context, "Книга успешно добавлена!", Toast.LENGTH_SHORT).show()
                onNavigateBack() // Возвращаемся назад после успеха
            } else {
                // Сообщение об ошибке отобразится через viewModel.error
                 Toast.makeText(context, viewModel.error ?: "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить книгу вручную") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Добавляем скролл
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Название*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = viewModel.error?.contains("Название") == true
            )

            OutlinedTextField(
                value = viewModel.author,
                onValueChange = viewModel::onAuthorChange,
                label = { Text("Автор") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Делаем поле повыше
            )
            
            // TODO: Добавить выбор обложки

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveBook,
                enabled = !viewModel.isLoading,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Сохранить")
                }
            }
            
            if (viewModel.error != null && !viewModel.isLoading) {
                 Text(
                    text = viewModel.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                 )
            }
        }
    }
} 