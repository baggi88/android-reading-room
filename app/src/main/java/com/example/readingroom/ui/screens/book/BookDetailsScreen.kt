package com.example.readingroom.ui.screens.book

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.readingroom.model.Book
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt
import androidx.compose.material3.IconToggleButton
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: String, // bookId все еще нужен для ключа ViewModel
    viewModel: BookViewModel = hiltViewModel(), // Используем существующий hiltViewModel вызов
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.bookState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер для выбора изображения
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Передаем URI в ViewModel для обработки
            viewModel.handleImageSelection(uri)
        } else {
            // Пользователь отменил выбор
            Toast.makeText(context, "Выбор изображения отменен", Toast.LENGTH_SHORT).show()
        }
    }

    // Обработчик событий от ViewModel
    LaunchedEffect(key1 = viewModel) {
        viewModel.eventFlow.collectLatest {
            when(it) {
                is BookScreenEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(it.message)
                }
                is BookScreenEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is BookScreenEvent.PickImage -> {
                    // Запускаем выбор изображения из медиатеки
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        }
    }
    
    // Пока простой флаг для кнопки Edit, можно усложнить позже
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Детали книги") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка редактирования (пока просто меняет вид)
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Готово" else "Редактировать"
                        )
                    }
                    // Кнопка удаления
                    IconButton(onClick = { viewModel.deleteBook() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is BookUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BookUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is BookUiState.Success -> {
                val book = state.book
                BookDetailsContent(
                    book = book,
                    paddingValues = padding,
                    isEditing = isEditing,
                    onToggleRead = { viewModel.toggleReadStatus() },
                    onToggleFavorite = { viewModel.toggleFavoriteStatus() },
                    onToggleWishlist = { viewModel.toggleWishlistStatus() },
                    onRatingChange = { newRating -> viewModel.updateRating(newRating) },
                    onChangeCoverClick = { viewModel.requestCoverImageChange() }
                )
            }
        }
    }
}

@Composable
fun BookDetailsContent(
    book: Book,
    paddingValues: PaddingValues,
    isEditing: Boolean,
    onToggleRead: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWishlist: () -> Unit,
    onRatingChange: (Float) -> Unit,
    onChangeCoverClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Обложка книги и кнопка редактирования
        Box(contentAlignment = Alignment.BottomEnd) {
             AsyncImage(
                model = book.coverUrl,
                contentDescription = "Обложка книги ${book.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Fit
            )
             if (isEditing) {
                IconButton(
                    onClick = onChangeCoverClick,
                    modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surface.copy(alpha=0.7f), CircleShape) // Полупрозрачный фон
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Сменить обложку", tint=MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Основная информация
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = book.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = book.author, style = MaterialTheme.typography.titleLarge)
        }

        // Рейтинг (отображение и редактирование)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Рейтинг", style = MaterialTheme.typography.titleMedium)
            RatingEditor(
                currentRating = book.rating.toFloat(),
                onRatingChange = onRatingChange,
                enabled = isEditing
            )
        }

        // Описание
        Text(text = "Описание", style = MaterialTheme.typography.titleMedium)
        Text(text = book.description.ifBlank { "Описание отсутствует" }, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))
        // --- Возвращаем кнопки статусов --- 
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusToggleButton( // Используем новый компонент
                label = "Прочитано", 
                isChecked = book.isRead, 
                checkedIcon = Icons.Filled.CheckCircle,
                uncheckedIcon = Icons.Outlined.CheckCircleOutline,
                onToggle = onToggleRead
            )
            StatusToggleButton(
                label = "Любимое", 
                isChecked = book.isFavorite, 
                checkedIcon = Icons.Filled.Favorite,
                uncheckedIcon = Icons.Outlined.FavoriteBorder,
                onToggle = onToggleFavorite
            )
        }
    }
}

@Composable
fun RatingEditor(
    currentRating: Float,
    onRatingChange: (Float) -> Unit,
    enabled: Boolean,
    steps: Int = 9 // 0.0, 0.5, 1.0 ... 4.5, 5.0 (10 значений, 9 шагов)
) {
    // Отображаем иконку и цифры всегда
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
         Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null, // Описание будет у Row
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = String.format("%.1f", currentRating),
            style = MaterialTheme.typography.titleMedium
        )
    }
    // Слайдер показываем только в режиме редактирования
    if (enabled) {
        Slider(
            value = currentRating,
            onValueChange = onRatingChange,
            valueRange = 0f..5f,
            steps = steps, 
            modifier = Modifier.width(150.dp) // Ограничим ширину слайдера
        )
    }
}

// Новый компонент для кнопки статуса
@Composable
fun StatusToggleButton(
    label: String,
    isChecked: Boolean,
    checkedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    uncheckedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        IconToggleButton(
            checked = isChecked,
            onCheckedChange = { onToggle() } // onCheckedChange вызывается при клике
        ) {
            Icon(
                imageVector = if (isChecked) checkedIcon else uncheckedIcon, 
                contentDescription = label,
                tint = if (isChecked) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 