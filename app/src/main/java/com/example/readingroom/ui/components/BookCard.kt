package com.example.readingroom.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.readingroom.model.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import android.util.Log
import com.example.readingroom.R

@Composable
fun BookCard(
    book: Book,
    onBookClick: (String) -> Unit,
    onAddFromFriendClick: () -> Unit = {},
    onRemoveClick: (() -> Unit)? = null,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(255.dp)
            .width(115.dp)
            .padding(2.dp)
            .clickable(enabled = onBookClick != null) {
                onBookClick(book.id)
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = book.coverUrl,
                        contentDescription = "Обложка книги ${book.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        },
                        error = { errorState ->
                            Log.e("BookCard", "Coil Error loading cover: ${book.coverUrl}. Error: ${errorState.result.throwable.message}")
                            Image(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Нет обложки",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                } else {
                    Image(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Нет обложки",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (onRemoveClick != null) {
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = book.title?.takeIf { it.isNotBlank() } ?: "(Без названия)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeight = 14.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = book.author?.takeIf { it.isNotBlank() } ?: "Автор неизвестен",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (book.isRead) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Прочитано",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (book.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "В избранном",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!book.isRead && !book.isFavorite) {
                            Spacer(modifier = Modifier.width(14.dp))
                        }
                    }
                    
                    if (book.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Рейтинг",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format("%.1f", book.rating),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(14.dp))
                    }
                }
            }
        }
    }
} 