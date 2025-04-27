package com.example.readingroom.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.readingroom.model.GenreData

@Composable
fun GenreDistributionCard(
   // genreDistribution: List<GenreData>, // Параметр не используется
   // modifier: Modifier = Modifier // Параметр не используется
) {
    // TODO: Реализовать отображение диаграммы или списка жанров
    Card(
        modifier = Modifier.fillMaxWidth() // Используем Modifier здесь
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Распределение по жанрам", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Отображение диаграммы жанров пока не реализовано.")
            // Здесь будет логика отображения genreDistribution
        }
    }
} 