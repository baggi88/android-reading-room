package com.example.readingroom.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.readingroom.ui.theme.ReadingRoomTheme
import com.example.readingroom.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.readingroom.ui.screens.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Внедряем SettingsViewModel
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Получаем выбранную тему из ViewModel
            val selectedTheme by settingsViewModel.selectedTheme.collectAsState()

            // Передаем выбранную тему (ThemeType) в ReadingRoomTheme
            ReadingRoomTheme(selectedTheme = selectedTheme) { 
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
} 