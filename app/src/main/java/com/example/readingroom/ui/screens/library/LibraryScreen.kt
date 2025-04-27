package com.example.readingroom.ui.screens.library

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.R
import com.example.readingroom.model.Book
import com.example.readingroom.ui.components.BookCard
import com.example.readingroom.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import com.example.readingroom.ui.screens.library.SortCriteria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    paddingValues: PaddingValues,
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToBookDetails: (String) -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val TAG = "LibraryScreen"
    Log.d(TAG, ">>> LibraryScreen Composable ENTERED.")

    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSortCriteria by viewModel.sortCriteria.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    Log.d(TAG, ">>> Collected state. isLoading=$isLoading, books=${books.size}, error=$error")
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(key1 = viewModel) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Log.d(TAG, ">>> Composing TopAppBar")
            TopAppBar(
                title = { Text("Моя Библиотека") },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort, 
                                contentDescription = "Сортировка",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            Text("Сортировать по:", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall)
                            Divider()
                            SortCriteria.entries.forEach { criteria ->
                                DropdownMenuItem(
                                    text = { Text(getSortCriteriaText(criteria)) },
                                    onClick = { 
                                        viewModel.setSortOrder(criteria)
                                        showSortMenu = false 
                                    },
                                    modifier = if (criteria == currentSortCriteria) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)) else Modifier
                                )
                            }
                        }
                    }
                    IconButton(onClick = { onNavigateToProfile() }) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddBook,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                content = { Icon(Icons.Default.Add, contentDescription = "Добавить книгу") }
            )
        }
    ) { innerScaffoldPadding ->
        Log.d(TAG, ">>> Composing Main Content Area. isLoading=${isLoading}")

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Log.d(TAG, ">>> Showing Loading Indicator")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(innerScaffoldPadding))
                }
                error != null -> {
                    Log.d(TAG, ">>> Showing Error: $error")
                    Text(
                        text = error ?: "Неизвестная ошибка",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(innerScaffoldPadding).padding(16.dp)
                    )
                }
                books.isEmpty() -> {
                    Log.d(TAG, ">>> Showing Empty Library Message")
                    Text(
                        text = "Ваша библиотека пуста. Добавьте книги!",
                        modifier = Modifier.align(Alignment.Center).padding(innerScaffoldPadding).padding(16.dp)
                    )
                }
                else -> {
                    Log.d(TAG, ">>> Composing LazyVerticalGrid for ${books.size} books with sort: ${currentSortCriteria.name}")
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 115.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(
                            top = innerScaffoldPadding.calculateTopPadding() + 4.dp,
                            bottom = paddingValues.calculateBottomPadding() + 4.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(items = books, key = { book: Book -> book.id }) { book: Book ->
                            Log.d(TAG, ">>> Composing BookCard for book: ${book.id}")
                            BookCard(
                                book = book,
                                onBookClick = { onNavigateToBookDetails(book.id) },
                                onRemoveClick = null,
                                isReadOnly = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSortCriteriaText(criteria: SortCriteria): String {
    return when (criteria) {
        SortCriteria.DATE_ADDED_DESC -> "Дата добавления (новые)"
        SortCriteria.DATE_ADDED_ASC -> "Дата добавления (старые)"
        SortCriteria.TITLE_ASC -> "Название (А-Я)"
        SortCriteria.TITLE_DESC -> "Название (Я-А)"
        SortCriteria.RATING_DESC -> "Рейтинг (высший)"
        SortCriteria.RATING_ASC -> "Рейтинг (низший)"
    }
} 