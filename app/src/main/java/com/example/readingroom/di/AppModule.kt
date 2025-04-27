package com.example.readingroom.di

import android.content.Context
// import androidx.room.Room // Убираем импорт
import com.example.readingroom.data.BookRepository
import com.example.readingroom.data.BookRepositoryImpl
import com.example.readingroom.data.UserRepository
import com.example.readingroom.data.UserRepositoryImpl
// import com.example.readingroom.data.local.AppDatabase // Убираем импорт
// import com.example.readingroom.data.local.BookDao // Убираем импорт
import com.example.readingroom.data.api.GoogleBooksApi
import com.example.readingroom.data.api.OpenLibraryApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.readingroom.data.remote.ImageUploader // Добавляем импорт

/**
 * Упрощенный модуль Hilt
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /* // Удаляем провайдеры Room
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "reading_room_database" // Имя файла базы данных
        ).build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }
    */

    // --- Оставляем провайдеры Firebase --- 
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /* // Удаляем провайдер для FirebaseStorage
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    */

    // --- Оставляем провайдеры репозиториев --- 
    @Provides
    @Singleton
    fun provideBookRepository(
        firestore: FirebaseFirestore,
        googleBooksApi: GoogleBooksApi,
        openLibraryApi: OpenLibraryApi,
        imageUploader: ImageUploader // Добавляем ImageUploader
    ): BookRepository {
        // Передаем все четыре зависимости
        return BookRepositoryImpl(firestore, googleBooksApi, openLibraryApi, imageUploader)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        imageUploader: ImageUploader // Меняем FirebaseStorage на ImageUploader
    ): UserRepository {
        return UserRepositoryImpl(firestore, imageUploader) // Передаем imageUploader
    }
} 