package com.example.readingroom

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import java.util.HashMap

@HiltAndroidApp
class ReadingRoomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeCloudinary()
    }

    private fun initializeCloudinary() {
        // Используй Cloud Name, который ты получил при регистрации
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "dvce6v9vp"
        // Не добавляй API key/secret для unsigned uploads
        // config["api_key"] = "YOUR_API_KEY"
        // config["api_secret"] = "YOUR_API_SECRET"
        MediaManager.init(this, config)
        // Можно добавить дополнительные конфигурации, если нужно
        // MediaManager.get().setConfiguration(config)
    }
} 