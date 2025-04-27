package com.example.readingroom.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readingroom.model.CollectionStatus
import com.example.readingroom.model.ReaderStatus

/**
 * Карточка для отображения статуса Читателя или Коллекционера
 */
@Composable
fun StatusCard(
    title: String,
    description: String,
    progress: Float? = null,
    currentValue: Int? = null,
    targetValue: Int? = null,
    modifier: Modifier = Modifier,
    cardColor: Color = MaterialTheme.colorScheme.surfaceVariant, // Цвет фона карточки
    progressColor: Color = MaterialTheme.colorScheme.primary, // Цвет прогресса
    trackColor: Color = MaterialTheme.colorScheme.surface // Цвет фона прогресс-бара
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Закругленные углы
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), // Уменьшаем вертикальный padding
            verticalArrangement = Arrangement.spacedBy(4.dp) // Уменьшаем расстояние
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)

            if (progress != null) {
                Spacer(modifier = Modifier.height(4.dp)) // Уменьшаем Spacer
                LinearProgressIndicator(
                    progress = { progress }, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp) // Делаем чуть тоньше
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = trackColor
                )
                // Отображение числового прогресса как в ReadingProgressCard
                if (currentValue != null && targetValue != null && targetValue > 0) {
                    // Убираем Spacer, так как Row сам даст отступ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentValue.toString(), 
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = targetValue.toString(), 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Обновляем перегрузки, добавляя currentValue и targetValue
@Composable
fun StatusCard(
    readerStatus: ReaderStatus,
    readBooksCount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    StatusCard(
        title = readerStatus.title,
        description = readerStatus.description,
        progress = progress,
        currentValue = readBooksCount,
        targetValue = readerStatus.nextMilestone, // Берем из enum
        modifier = modifier
    )
}

@Composable
fun StatusCard(
    collectionStatus: CollectionStatus,
    totalBooksCount: Int,
    progress: Float? = null, // Теперь можем передать прогресс
    modifier: Modifier = Modifier,
    cardColor: Color = MaterialTheme.colorScheme.secondaryContainer // Другой цвет для коллекционера?
) {
    StatusCard(
        title = collectionStatus.title,
        description = collectionStatus.description,
        progress = progress, // Передаем прогресс, если он есть
        currentValue = totalBooksCount,
        targetValue = collectionStatus.nextMilestone, // Берем из enum
        modifier = modifier,
        cardColor = cardColor,
        progressColor = MaterialTheme.colorScheme.secondary, // Другой цвет прогресса?
        trackColor = MaterialTheme.colorScheme.surface // Оставляем тот же фон бара
    )
} 