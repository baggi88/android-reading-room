package com.example.readingroom.data

import android.net.Uri
import com.example.readingroom.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Получить данные пользователя по его ID */
    suspend fun getUser(userId: String): User?

    /** Создать или обновить данные пользователя */
    suspend fun updateUser(user: User)

    /** Проверить, существует ли никнейм */
    suspend fun isNicknameAvailable(nickname: String): Boolean

    /** Найти пользователя по никнейму */
    fun findUsersByNickname(query: String): Flow<List<User>>

    /** Добавить пользователя в друзья */
    suspend fun addFriend(userId: String, friendId: String)

    /** Удалить пользователя из друзей */
    suspend fun removeFriend(userId: String, friendId: String)

    /** Получить список друзей пользователя */
    fun getFriends(userId: String): Flow<List<User>>

    /** Обновить поле allowAddingByNickname для пользователя */
    suspend fun setAllowAddingByNickname(userId: String, allow: Boolean)

    /**
     * Загружает изображение аватара в Firebase Storage.
     * @param userId ID пользователя.
     * @param avatarUri Локальный URI изображения.
     * @return URL загруженного изображения.
     * @throws Exception Если произошла ошибка загрузки.
     */
    suspend fun uploadUserAvatar(userId: String, avatarUri: Uri): String

    /**
     * Обновляет поле avatarUrl пользователя в Firestore.
     * @param userId ID пользователя.
     * @param avatarUrl Новый URL аватара.
     * @throws Exception Если произошла ошибка обновления.
     */
    suspend fun updateUserAvatarUrl(userId: String, avatarUrl: String)
} 