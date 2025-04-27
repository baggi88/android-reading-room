package com.example.readingroom.data.api

import com.example.readingroom.model.Book

/**
 * Функция-расширение для преобразования объекта BookItem (из Google Books API)
 * в нашу модель данных Book.
 */
fun BookItem.toBookModel(): Book {
    val volumeInfo = this.volumeInfo
    return Book(
        googleBooksId = this.id, // Используем ID из Google Books
        title = volumeInfo.title ?: "",
        author = volumeInfo.authors?.joinToString(", ") ?: "Автор неизвестен",
        description = volumeInfo.description ?: "",
        coverUrl = this.getCoverUrl(CoverSize.MEDIUM) ?: "", // Используем метод из BookItem
        // Остальные поля инициализируем по умолчанию или оставляем пустыми,
        // так как не вся информация есть в Google Books API в нужном формате.
        id = this.id, // Пока используем Google ID и как основной ID
        genre = "", // Google Books API не всегда предоставляет жанр в удобном виде
        pageCount = 0, // Google Books API не всегда предоставляет кол-во страниц
        status = "", // Статус будет устанавливаться пользователем
        rating = 0f, // Рейтинг будет устанавливаться пользователем
        isFavorite = false,
        isRead = false,
        addedDate = null, // Установит сервер
        readDate = null,
        userId = "", // Будет установлен при добавлении в библиотеку пользователя
        titleLowercase = volumeInfo.title?.lowercase() ?: "", // Сразу создаем lowercase
        authorLowercase = volumeInfo.authors?.joinToString(", ")?.lowercase() ?: "", // Сразу создаем lowercase
        isInWishlist = false
    )
} 