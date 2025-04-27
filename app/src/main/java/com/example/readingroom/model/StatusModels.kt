package com.example.readingroom.model

/**
 * Статусы для прогресса чтения
 */
enum class ReaderStatus(
    val title: String,
    val description: String,
    val minBooks: Int,
    val nextMilestone: Int
) {
    ZERO_BOOKS(
        title = "Читатель",
        description = "Прочитай первую книгу",
        minBooks = 0,
        nextMilestone = 1
    ),
    ONE_BOOK(
        title = "Начинающий чтец",
        description = "Первая книга - отличное начало!",
        minBooks = 1,
        nextMilestone = 10
    ),
    TEN_BOOKS(
        title = "Юный книголюб",
        description = "Уже 10 книг позади!",
        minBooks = 10,
        nextMilestone = 20
    ),
    TWENTY_BOOKS(
        title = "Увлеченный читатель",
        description = "Чтение затягивает, не правда ли?",
        minBooks = 20,
        nextMilestone = 30
    ),
    THIRTY_BOOKS(
        title = "Книжный странник",
        description = "Ты уверенно движешься по книжным мирам",
        minBooks = 30,
        nextMilestone = 40
    ),
    FORTY_BOOKS(
        title = "Библиофил",
        description = "Книги - твоя страсть!",
        minBooks = 40,
        nextMilestone = 50
    ),
    FIFTY_BOOKS(
        title = "Книжный гурман",
        description = "Полсотни книг - серьезный результат!",
        minBooks = 50,
        nextMilestone = 60
    ),
    SIXTY_BOOKS(
        title = "Опытный чтец",
        description = "Тебя уже сложно удивить сюжетом",
        minBooks = 60,
        nextMilestone = 70
    ),
    SEVENTY_BOOKS(
        title = "Книжный эксперт",
        description = "Твои знания впечатляют",
        minBooks = 70,
        nextMilestone = 80
    ),
    EIGHTY_BOOKS(
        title = "Книжный сомелье",
        description = "Книги открыли тебе многое",
        minBooks = 80,
        nextMilestone = 100
    ),
    HUNDRED_BOOKS(
        title = "Книжный маэстро",
        description = "Сотня книг - это великолепно!",
        minBooks = 100,
        nextMilestone = 101
    ),
    MASTER_READER(
        title = "Великий читатель",
        description = "Ты достиг вершин читательского мастерства!",
        minBooks = 101,
        nextMilestone = 101
    );

    companion object {
        fun fromBookCount(count: Int): ReaderStatus = when {
            count >= MASTER_READER.minBooks -> MASTER_READER
            count >= HUNDRED_BOOKS.minBooks -> HUNDRED_BOOKS
            count >= EIGHTY_BOOKS.minBooks -> EIGHTY_BOOKS
            count >= SEVENTY_BOOKS.minBooks -> SEVENTY_BOOKS
            count >= SIXTY_BOOKS.minBooks -> SIXTY_BOOKS
            count >= FIFTY_BOOKS.minBooks -> FIFTY_BOOKS
            count >= FORTY_BOOKS.minBooks -> FORTY_BOOKS
            count >= THIRTY_BOOKS.minBooks -> THIRTY_BOOKS
            count >= TWENTY_BOOKS.minBooks -> TWENTY_BOOKS
            count >= TEN_BOOKS.minBooks -> TEN_BOOKS
            count >= ONE_BOOK.minBooks -> ONE_BOOK
            else -> ZERO_BOOKS
        }
    }
}


/**
 * Статусы для коллекции книг
 */
enum class CollectionStatus(
    val title: String,
    val description: String,
    val minBooks: Int,
    val nextMilestone: Int
) {
    ZERO_COLL(
        title = "Новичок",
        description = "Добавь книги в библиотеку",
        minBooks = 0,
        nextMilestone = 1
    ),
    ONE_COLL(
        title = "Коллекционер",
        description = "Начало положено!",
        minBooks = 1,
        nextMilestone = 10
    ),
    TEN_COLL(
        title = "Собиратель",
        description = "У тебя уже небольшая стопка книг",
        minBooks = 10,
        nextMilestone = 20
    ),
    TWENTY_COLL(
        title = "Любитель",
        description = "Коллекция растет",
        minBooks = 20,
        nextMilestone = 40
    ),
    FORTY_COLL(
        title = "Энтузиаст",
        description = "Полки начинают заполняться",
        minBooks = 40,
        nextMilestone = 60
    ),
    SIXTY_COLL(
        title = "Куратор",
        description = "Ты неплохо разбираешься в книгах",
        minBooks = 60,
        nextMilestone = 80
    ),
    EIGHTY_COLL(
        title = "Библиотекарь",
        description = "Внушительная коллекция",
        minBooks = 80,
        nextMilestone = 100
    ),
    HUNDRED_COLL(
        title = "Знаток",
        description = "Сотня книг - это серьезно!",
        minBooks = 100,
        nextMilestone = 125
    ),
    ONE_TWENTY_FIVE_COLL(
        title = "Архивариус",
        description = "Твоя библиотека достойна уважения",
        minBooks = 125,
        nextMilestone = 150
    ),
    ONE_FIFTY_COLL(
        title = "Хранитель",
        description = "Ты собрал целое состояние",
        minBooks = 150,
        nextMilestone = 200
    ),
    TWO_HUNDRED_COLL(
        title = "Великий Коллекционер",
        description = "Масштабы твоей библиотеки поражают!",
        minBooks = 200,
        nextMilestone = 201
    ),
    MASTER_COLLECTOR(
        title = "Легенда",
        description = "Твоя коллекция - легендарна!",
        minBooks = 201,
        nextMilestone = 201
    );

    companion object {
        fun fromBookCount(count: Int): CollectionStatus = when {
            count >= MASTER_COLLECTOR.minBooks -> MASTER_COLLECTOR
            count >= TWO_HUNDRED_COLL.minBooks -> TWO_HUNDRED_COLL
            count >= ONE_FIFTY_COLL.minBooks -> ONE_FIFTY_COLL
            count >= ONE_TWENTY_FIVE_COLL.minBooks -> ONE_TWENTY_FIVE_COLL
            count >= HUNDRED_COLL.minBooks -> HUNDRED_COLL
            count >= EIGHTY_COLL.minBooks -> EIGHTY_COLL
            count >= SIXTY_COLL.minBooks -> SIXTY_COLL
            count >= FORTY_COLL.minBooks -> FORTY_COLL
            count >= TWENTY_COLL.minBooks -> TWENTY_COLL
            count >= TEN_COLL.minBooks -> TEN_COLL
            count >= ONE_COLL.minBooks -> ONE_COLL
            else -> ZERO_COLL
        }
    }
} 