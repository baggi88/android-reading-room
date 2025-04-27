package com.example.readingroom.ui.screens.addbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.readingroom.model.Book
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManualAddBook: () -> Unit,
    onNavigateToBookDetails: (String) -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userAddedBooks by viewModel.userAddedBooks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Обработка событий для Snackbar
    LaunchedEffect(viewModel) {
        viewModel.addBookResultEvent.collectLatest { result ->
            val message = if (result.success) {
                "Книга '${result.bookTitle}' добавлена в библиотеку"
            } else {
                result.error ?: "Не удалось добавить книгу '${result.bookTitle}'"
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(viewModel) {
         viewModel.wishlistEvent.collectLatest { result ->
             val message = if (result.success) {
                 "Книга '${result.bookTitle}' добавлена в список желаемого"
             } else {
                 result.error ?: "Не удалось добавить книгу '${result.bookTitle}' в список желаемого"
             }
             snackbarHostState.showSnackbar(message)
         }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Добавить книгу") },
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
                .padding(horizontal = 16.dp)
        ) {
            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                // Используем onSearchQueryChange
                onValueChange = { viewModel.onSearchQueryChange(it) }, 
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по названию или автору") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                 trailingIcon = {
                     if (isLoading) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp))
                     }
                 }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Отображение ошибки
            if (error != null) {
                Text(
                    text = error ?: "Произошла ошибка", 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                    // Убираем clearError, т.к. ошибка сбрасывается во ViewModel
                )
            }

            // Список результатов
             if (searchResults.isEmpty() && !isLoading && error == null && searchQuery.length > 1) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Text("Ничего не найдено. Попробуйте другой запрос или добавьте книгу вручную.")
                 }
             } else {
                  LazyColumn(
                     modifier = Modifier.fillMaxSize(),
                     verticalArrangement = Arrangement.spacedBy(8.dp),
                     contentPadding = PaddingValues(vertical = 8.dp) 
                 ) {
                     items(searchResults, key = { book -> book.googleBooksId ?: book.id }) { book ->
                         val isAdded = userAddedBooks.any { addedBook -> 
                             (addedBook.googleBooksId != null && addedBook.googleBooksId == book.googleBooksId) || 
                             (addedBook.id == book.id) 
                         }
                         val isInWishlist = userAddedBooks.any { addedBook -> 
                             ((addedBook.googleBooksId != null && addedBook.googleBooksId == book.googleBooksId) || 
                              (addedBook.id == book.id)) && addedBook.isInWishlist
                         }
                         
                         SearchResultItem(
                             book = book,
                             isAdded = isAdded,
                             isInWishlist = isInWishlist,
                             onAddToLibraryClick = { viewModel.addBookToLibrary(book) },
                             onAddToWishlistClick = { viewModel.addToWishlist(book) },
                             onDetailsClick = { 
                                 val idToNavigate = book.googleBooksId ?: book.id
                                 if (idToNavigate.isNotBlank()) {
                                     onNavigateToBookDetails(idToNavigate)
                                 } else {
                                     Toast.makeText(context, "Ошибка: Не удалось получить ID книги", Toast.LENGTH_SHORT).show()
                                 }
                            }
                         )
                     }
                 }
             }
        }
    }
}

@Composable
fun SearchResultItem(
    book: Book,
    isAdded: Boolean,
    isInWishlist: Boolean,
    onAddToLibraryClick: () -> Unit,
    onAddToWishlistClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailsClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = "Обложка книги",
                modifier = Modifier
                    .height(100.dp)
                    .widthIn(max = 70.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(book.author, fontSize = 14.sp, maxLines = 1)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onAddToLibraryClick() }, 
                    enabled = !isAdded,
                    modifier = Modifier.clickable(enabled = false) { /* Пустой обработчик для блокировки распространения кликов */ }
                ) {
                    Icon(
                        imageVector = if (isAdded) Icons.Filled.CheckCircle else Icons.Outlined.AddCircleOutline,
                        contentDescription = if (isAdded) "Добавлено" else "Добавить в библиотеку",
                        tint = if (isAdded) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                
                IconButton(
                    onClick = { onAddToWishlistClick() }, 
                    enabled = !isInWishlist,
                    modifier = Modifier.clickable(enabled = false) { /* Пустой обработчик для блокировки распространения кликов */ }
                ) { 
                    Icon(
                        imageVector = if (isInWishlist) Icons.Filled.Star else Icons.Outlined.StarBorder, 
                        contentDescription = if (isInWishlist) "Убрать из желаемого" else "Добавить в желаемое",
                        tint = if (isInWishlist) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }
}

// Закомментируем секцию про placeholder
/*
// Убедитесь, что у вас есть ресурс placeholder_book_cover в drawable
// Пример: res/drawable/placeholder_book_cover.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24.0"
    android:viewportHeight="24.0">
    <path
        android:fillColor="#FFAAAAAA"
        android:pathData="M18,2H6c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM6,4h5v8l-2.5,-1.5L6,12V4z"/>
</vector>
*/ 