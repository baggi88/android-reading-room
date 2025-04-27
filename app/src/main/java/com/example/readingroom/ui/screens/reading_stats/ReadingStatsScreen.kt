package com.example.readingroom.ui.screens.reading_stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.readingroom.model.StatisticsUiState
import com.example.readingroom.ui.components.BottomNavigationBar
import com.example.readingroom.ui.components.ReadingProgressCard
import com.example.readingroom.ui.components.StatCard
import com.example.readingroom.ui.components.StatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStatsScreen(
    viewModel: ReadingStatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    paddingValues: PaddingValues,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика чтения") },
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
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = "Произошла ошибка при загрузке статистики.", 
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Column(
                         modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                         horizontalAlignment = Alignment.CenterHorizontally,
                         verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(title = "Всего книг", value = uiState.totalBooks.toString(), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            StatCard(title = "Прочитано", value = uiState.readBooks.toString(), modifier = Modifier.weight(1f))
                        }
                        uiState.monthlyGoal?.let { goal ->
                             ReadingProgressCard(
                                 title = "Цель на месяц", 
                                 current = uiState.readThisMonth,
                                 target = goal
                             )
                        }
                        uiState.semiAnnualGoal?.let { goal ->
                             ReadingProgressCard(
                                 title = "Цель на полгода", 
                                 current = uiState.readThisYear, 
                                 target = goal
                             )
                        }
                        uiState.annualGoal?.let { goal ->
                             ReadingProgressCard(
                                 title = "Цель на год",
                                 current = uiState.readThisYear, 
                                 target = goal
                             )
                        }
                        Text("Ваши статусы:", style = MaterialTheme.typography.titleMedium)
                        StatusCard(
                            readerStatus = uiState.readerStatus, 
                            readBooksCount = uiState.readBooks,
                            progress = uiState.progressToNextReaderMilestone
                        )
                        StatusCard(
                            collectionStatus = uiState.collectionStatus,
                            totalBooksCount = uiState.totalBooks,
                            progress = uiState.progressToNextCollectionMilestone
                        )
                    }
                }
            }
        }
    }
} 