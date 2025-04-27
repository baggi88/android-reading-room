package com.example.readingroom.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.ui.components.BottomNavigationBar
import com.example.readingroom.ui.components.StatCard
import com.example.readingroom.ui.components.ReadingProgressCard
import com.example.readingroom.ui.components.GenreDistributionCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.readingroom.model.StatisticsUiState
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.readingroom.model.ReaderStatus
import com.example.readingroom.model.CollectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "statistics",
                onNavigate = { route ->
                    if (route != "statistics") {
                        onNavigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Произошла ошибка",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            StatisticsContent(uiState, paddingValues)
        }
    }
}

@Composable
fun StatisticsContent(uiState: StatisticsUiState, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Основная статистика
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(title = "Всего книг", value = uiState.totalBooks.toString(), modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatCard(title = "Прочитано", value = uiState.readBooks.toString(), modifier = Modifier.weight(1f))
        }
        
        // Отображаем статус коллекционера
        val collectorStatus = CollectionStatus.fromBookCount(uiState.totalBooks)
        Text(
            text = "Статус коллекционера: ${collectorStatus.title}", 
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally) // Центрируем
        )
        // TODO: Можно добавить описание статуса collectorStatus.description
        // TODO: Можно добавить прогресс-бар для коллекционера, если определить уровни в CollectionStatus
        
        // Диаграмма жанров (показывать, если есть жанры)
        if (uiState.genreDistribution.isNotEmpty()) {
            GenreDistributionCard() // TODO: Передать uiState.genreDistribution внутрь?
            Spacer(modifier = Modifier.height(8.dp)) 
        }

        // Прогресс чтения
        ReadingProgressCard(
            title = "Прогресс чтения",
            current = uiState.readBooks,
            target = uiState.totalBooks // Будет 0, если книг нет
        )
        Spacer(modifier = Modifier.height(8.dp)) 

        // Статус чтения
        val currentReadBooks = uiState.readBooks
        val currentReaderStatus = ReaderStatus.fromBookCount(currentReadBooks)
        val nextReaderLevel = currentReaderStatus.nextMilestone
        if (nextReaderLevel > 0) {
            StatusProgressBar(current = currentReadBooks, nextLevel = nextReaderLevel, currentStatus = currentReaderStatus.title)
        }
    }
}

@Composable
fun StatusProgressBar(current: Int, nextLevel: Int, currentStatus: String) {
    if (nextLevel <= 0) return 
    val progress = current.toFloat() / nextLevel
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = "StatusProgress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Текущий статус: $currentStatus",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$current / $nextLevel книг",
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current // Используем цвет контента по умолчанию
            )
            Text(
                text = "До цели: ${nextLevel - current}",
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current // Используем цвет контента по умолчанию
            )
        }
    }
} 