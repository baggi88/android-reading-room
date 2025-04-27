package com.example.readingroom.data.remote

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryImageUploader @Inject constructor() : ImageUploader {

    private val TAG = "CloudinaryUploader"
    // Используем Upload Preset, созданный в настройках Cloudinary
    private val uploadPreset = "ReadingRoomAvatarCover"

    override suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val url = upload(uri)
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image to Cloudinary", e)
            Result.failure(e)
        }
    }

    private suspend fun upload(uri: Uri): String {
        // Используем suspendCancellableCoroutine для обертки callback-based API
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get()
                .upload(uri)
                .unsigned(uploadPreset) // Указываем unsigned preset
                // Можно добавить опции трансформации при загрузке, если нужно
                // .option("", "") 
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Cloudinary upload started. Request ID: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // val progress = bytes.toDouble() / totalBytes
                        // Можно использовать для отображения прогресса
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            Log.d(TAG, "Cloudinary upload success. URL: $url")
                            if (continuation.isActive) {
                                continuation.resume(url)
                            }
                        } else {
                            Log.e(TAG, "Cloudinary upload success but URL is missing. Data: $resultData")
                            if (continuation.isActive) {
                                continuation.resumeWithException(Exception("Cloudinary upload failed: Secure URL not found in response"))
                            }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Cloudinary upload error. Code: ${error.code}, Description: ${error.description}")
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Cloudinary upload rescheduled. Code: ${error.code}, Description: ${error.description}")
                        // Обработка перепланирования, если требуется (например, из-за сети)
                    }
                })
                .dispatch() // Запускаем загрузку

            // Отмена корутины отменит запрос Cloudinary
            continuation.invokeOnCancellation { 
                Log.d(TAG, "Coroutine cancelled, cancelling Cloudinary upload request ID: $requestId")
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }
} 