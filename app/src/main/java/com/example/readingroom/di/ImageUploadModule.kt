        package com.example.readingroom.di

        import android.net.Uri
        import com.example.readingroom.data.remote.ImageUploader
        import com.example.readingroom.data.remote.CloudinaryImageUploader // Добавляем новый импорт
        import dagger.Binds // Используем Binds для интерфейсов
        import dagger.Module
        import dagger.Provides
        import dagger.hilt.InstallIn
        import dagger.hilt.components.SingletonComponent
        import javax.inject.Singleton
        // import kotlin.NotImplementedError // Больше не нужен

        /* // Удаляем заглушку
        class DummyImageUploader : ImageUploader {
            override suspend fun uploadImage(uri: Uri): Result<String> {
                println("DUMMY UPLOAD: Pretending to upload $uri")
                 return Result.failure(NotImplementedError("Dummy Image Uploader not implemented yet"))
                // return Result.success("https://via.placeholder.com/150.png?text=Uploaded+${uri.lastPathSegment}")
            }
        }
        */

        @Module
        @InstallIn(SingletonComponent::class)
        // Используем abstract class или interface для @Binds
        abstract class ImageUploadModule {

            // Используем @Binds для связи интерфейса с реализацией
            @Binds
            @Singleton
            abstract fun bindImageUploader(impl: CloudinaryImageUploader): ImageUploader
            
            /* // Заменяем @Provides на @Binds
            @Provides
            @Singleton 
            fun provideImageUploader(): ImageUploader { 
                // Возвращаем конкретную реализацию (пока заглушку)
                return DummyImageUploader()
            }
            */
        }