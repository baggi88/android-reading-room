package com.example.readingroom.di

import com.example.readingroom.data.api.GoogleBooksApi
import com.example.readingroom.data.api.OpenLibraryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"
    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("GoogleBooksRetrofit")
    fun provideGoogleBooksRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GOOGLE_BOOKS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("OpenLibraryRetrofit")
    fun provideOpenLibraryRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksApi(@Named("GoogleBooksRetrofit") retrofit: Retrofit): GoogleBooksApi {
        android.util.Log.d("NetworkModule", ">>> Creating GoogleBooksApi instance")
        val api = retrofit.create(GoogleBooksApi::class.java)
        android.util.Log.d("NetworkModule", ">>> GoogleBooksApi instance created: ${api.hashCode()}")
        return api
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(@Named("OpenLibraryRetrofit") retrofit: Retrofit): OpenLibraryApi {
        android.util.Log.d("NetworkModule", ">>> Creating OpenLibraryApi instance")
        val api = retrofit.create(OpenLibraryApi::class.java)
        android.util.Log.d("NetworkModule", ">>> OpenLibraryApi instance created: ${api.hashCode()}")
        return api
    }
} 