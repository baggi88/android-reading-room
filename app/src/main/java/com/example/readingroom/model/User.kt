package com.example.readingroom.model

import com.google.firebase.firestore.DocumentId

/**
 * Модель пользователя для Firestore.
 *
 * @param uid Уникальный ID пользователя (обычно совпадает с Firebase Auth UID и ID документа).
 * @param nickname Уникальный никнейм пользователя.
 * @param nicknameLowercase Никнейм пользователя в нижнем регистре для поиска без учета регистра
 * @param avatarUrl URL изображения аватара пользователя (может быть пустым).
 * @param friends Список ID пользователей, являющихся друзьями.
 * @param allowAddingByNickname Разрешено ли добавлять пользователя в друзья по никнейму.
 */
data class User(
    @DocumentId // Аннотация для автоматического маппинга ID документа
    val uid: String = "", // Инициализируем пустыми значениями по умолчанию для Firestore
    val nickname: String = "",
    val nicknameLowercase: String = "", // Добавляем поле для поиска без учета регистра
    val avatarUrl: String = "", // Добавляем поле для URL аватара
    val friends: List<String> = emptyList(),
    val allowAddingByNickname: Boolean = false
)

// Удаляем старые ReadingGoals, если они больше не нужны здесь
// data class ReadingGoals(
//     val monthly: Int,
//     val semiAnnual: Int,
//     val annual: Int
// ) 