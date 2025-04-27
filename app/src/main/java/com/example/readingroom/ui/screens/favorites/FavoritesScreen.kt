package com.example.readingroom.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.model.Book
import com.example.readingroom.ui.components.BookCard
import androidx.compose.ui.unit.LayoutDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    paddingValues: PaddingValues,
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBookDetails: (String) -> Unit
) {
    val favoritesState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Профиль")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { innerPadding ->
         Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                favoritesState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                favoritesState.favoriteBooks.isEmpty() && favoritesState.error == null && !favoritesState.isLoading -> {
                     Text(
                        text = "У вас пока нет избранных книг.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                         modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                favoritesState.error != null -> {
                     Text(
                        text = favoritesState.error ?: "Ошибка загрузки избранного.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                     LazyVerticalGrid(
                         columns = GridCells.Adaptive(minSize = 115.dp),
                         modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 4.dp,
                            bottom = paddingValues.calculateBottomPadding() + 4.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                     ) {
                        items(favoritesState.favoriteBooks, key = { it.id }) { book: Book ->
                            BookCard(
                                book = book,
                                onBookClick = { onNavigateToBookDetails(book.id) },
                                onRemoveClick = { viewModel.removeFromFavorites(book) },
                                isReadOnly = false
                            )
                        }
                    }
                }
            }
        }
    }
} 