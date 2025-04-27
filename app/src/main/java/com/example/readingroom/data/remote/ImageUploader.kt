        package com.example.readingroom.data.remote

        import android.net.Uri

        /**
         * Интерфейс для загрузки изображений на удаленный сервис.
         */
        interface ImageUploader {
            /**
             * Загружает изображение по URI.
             * @param uri URI изображения.
             * @return Результат операции: URL загруженного изображения в случае успеха, или ошибка.
             */
            suspend fun uploadImage(uri: Uri): Result<String>
        }