package com.example.readingroom.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingroom.ui.theme.NavigationBarColor
import com.example.readingroom.ui.navigation.Screen
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import android.util.Log

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("Библиотека", Icons.AutoMirrored.Filled.MenuBook, Screen.Library.route),
        BottomNavItem("Статистика", Icons.Default.BarChart, Screen.ReadingStats.route),
        BottomNavItem("Вишлист", Icons.Default.Star, Screen.Wishlist.route),
        BottomNavItem("Любимое", Icons.Default.Favorite, Screen.Favorites.route),
        BottomNavItem("Друзья", Icons.Default.People, Screen.Friends.route)
    )

    NavigationBar(
        modifier = Modifier.height(56.dp)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { 
                    Text(
                        item.label,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ) 
                },
                selected = currentRoute == item.route,
                onClick = { 
                    Log.d("BottomNavBar", "Navigating to: ${item.route}. Current route was: $currentRoute")
                    onNavigate(item.route) 
                }
            )
        }
    }
} 