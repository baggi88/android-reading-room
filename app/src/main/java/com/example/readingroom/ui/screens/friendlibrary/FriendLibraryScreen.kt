package com.example.readingroom.ui.screens.friendlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.model.Book
import com.example.readingroom.ui.components.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendLibraryScreen(
    // userId друга будет получен через ViewModel из SavedStateHandle
    viewModel: FriendLibraryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToBookDetails: (String) -> Unit // Если хотим кликать на книги друга
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Библиотека друга") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center // Для индикатора/ошибки
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Произошла ошибка",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    // Отображаем только список книг
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Список книг друга
                        if (uiState.friendBooks.isEmpty()) {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp), 
                                contentAlignment = Alignment.Center
                            ) {
                                Text("У друга нет книг в библиотеке")
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3), // Как в LibraryScreen
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                // Используем только внутренние отступы, т.к. padding уже применен к Box выше
                                contentPadding = PaddingValues(bottom = 8.dp + innerPadding.calculateBottomPadding()), // Учитываем нижний системный паддинг
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(items = uiState.friendBooks, key = { book: Book -> book.id }) { book: Book ->
                                    BookCard(
                                        book = book,
                                        onBookClick = { onNavigateToBookDetails(book.id) }, // Переходим к деталям книги
                                        isReadOnly = true // САМОЕ ВАЖНОЕ: режим только для чтения!
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}