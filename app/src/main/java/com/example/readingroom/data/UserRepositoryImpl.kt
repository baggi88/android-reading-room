package com.example.readingroom.data

import android.net.Uri
import android.util.Log
import com.example.readingroom.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import com.example.readingroom.data.remote.ImageUploader

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageUploader: ImageUploader
) : UserRepository {

    private val usersCollection = firestore.collection("users")
    private val TAG = "UserRepositoryImpl"

    override suspend fun getUser(userId: String): User? {
        Log.d(TAG, "Attempting to get user with ID: $userId")
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            Log.d(TAG, "Document snapshot exists: ${documentSnapshot.exists()}")
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                Log.d(TAG, "User object created: $user")
                user
            } else {
                Log.w(TAG, "User document does not exist for ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user for ID: $userId", e)
            null
        }
    }

    override suspend fun updateUser(user: User) {
        val userToSave = user.copy(nicknameLowercase = user.nickname.lowercase())
        Log.d(TAG, "Updating/Creating user document with UID: ${userToSave.uid}, User data: $userToSave")
        try {
            Log.i(TAG, "Executing Firestore set operation for UID: ${userToSave.uid} with data: $userToSave")
            usersCollection.document(userToSave.uid).set(userToSave).await()
            Log.d(TAG, "Successfully updated/created user document for UID: ${userToSave.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "Firestore set operation failed for UID: ${userToSave.uid}. Error: ${e.javaClass.simpleName}", e)
            throw e
        }
    }

    override suspend fun isNicknameAvailable(nickname: String): Boolean {
        val querySnapshot = usersCollection.whereEqualTo("nickname", nickname).limit(1).get().await()
        return querySnapshot.isEmpty
    }

    override fun findUsersByNickname(query: String): Flow<List<User>> {
        Log.d(TAG, "Finding users by nickname query: $query")
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            return flowOf(emptyList())
        }
        return firestore.collection("users")
            .whereGreaterThanOrEqualTo("nicknameLowercase", normalizedQuery)
            .whereLessThan("nicknameLowercase", normalizedQuery + '\uf8ff')
            .limit(20)
            .snapshots()
            .map { snapshot: QuerySnapshot ->
                Log.d(TAG, "findUsersByNickname snapshot received with ${snapshot.size()} docs")
                snapshot.documents.mapNotNull { doc: DocumentSnapshot -> doc.toObject(User::class.java) }
            }
    }

    override suspend fun addFriend(userId: String, friendId: String) {
        Log.i(TAG, "Adding friend: $friendId to user: $userId friends list")
        // Убираем транзакцию, обновляем только документ userId
        val userDocRef = usersCollection.document(userId)

        try {
             userDocRef.update("friends", FieldValue.arrayUnion(friendId)).await()
             Log.d(TAG, "Successfully updated friends list for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding friend for user: $userId", e)
            throw e
        }
    }

    override suspend fun removeFriend(userId: String, friendId: String) {
        Log.i(TAG, "Removing friend: $friendId from user: $userId friends list")
        // Убираем транзакцию, удаляем только из списка userId
        try {
            usersCollection.document(userId).update("friends", FieldValue.arrayRemove(friendId)).await()
            Log.d(TAG, "Successfully updated friends list for user: $userId after removal")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing friend for user: $userId", e)
            throw e
        }
    }

    override fun getFriends(userId: String): Flow<List<User>> {
        Log.d(TAG, "Getting friends flow for user $userId")
        return usersCollection.document(userId).snapshots()
            .map { userSnapshot: DocumentSnapshot ->
                val friendIds = userSnapshot.toObject(User::class.java)?.friends ?: emptyList()
                Log.d(TAG, "User $userId friend IDs: $friendIds")
                friendIds
            }
            .flatMapLatest { friendIds: List<String> ->
                Log.d(TAG, "User $userId flatMapLatest triggered with IDs: $friendIds")
                if (friendIds.isEmpty()) {
                    Log.d(TAG, "User $userId has no friend IDs, returning empty list flow.")
                    flowOf(emptyList())
                } else {
                    Log.d(TAG, "User $userId has friend IDs, creating friend flows...")
                    val friendFlows: List<Flow<User?>> = friendIds.map { friendId: String ->
                        Log.d(TAG, "Creating flow for friend ID: $friendId")
                        usersCollection.document(friendId).snapshots()
                            .map { friendSnapshot: DocumentSnapshot ->
                                val friendUser = friendSnapshot.toObject(User::class.java)
                                Log.d(TAG, "Snapshot received for friend ID: $friendId. User: $friendUser")
                                friendUser
                            }
                            .catch { e -> 
                                Log.e(TAG, "Error getting snapshot for friend ID: $friendId", e)
                                null
                            }
                    }
                    Log.d(TAG, "Combining ${friendFlows.size} friend flows for user $userId")
                    combine(friendFlows) { friendArray: Array<User?> ->
                        val friends = friendArray.filterNotNull().toList()
                        Log.d(TAG, "Combine result for user $userId: ${friends.size} non-null friends")
                        friends
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in getFriends flow for user $userId", e)
                emitAll(flowOf(emptyList()))
            }
    }

    override suspend fun setAllowAddingByNickname(userId: String, allow: Boolean) {
        try {
            Log.i(TAG, "Updating allowAddingByNickname for user $userId to $allow")
            usersCollection.document(userId).update("allowAddingByNickname", allow).await()
            Log.d(TAG, "Successfully updated allowAddingByNickname for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating allowAddingByNickname for user $userId", e)
            throw e
        }
    }

    override suspend fun uploadUserAvatar(userId: String, avatarUri: Uri): String {
        Log.d(TAG, "Uploading avatar for user $userId using ImageUploader from URI: $avatarUri")
        val result = imageUploader.uploadImage(avatarUri)
        
        return result.fold(
            onSuccess = { downloadUrl ->
                Log.d(TAG, "ImageUploader success for user $userId. URL: $downloadUrl")
                downloadUrl
            },
            onFailure = { exception ->
                Log.e(TAG, "ImageUploader failed for user $userId", exception)
                throw Exception("Ошибка загрузки аватара через ImageUploader: ${exception.message}", exception)
            }
        )
    }

    override suspend fun updateUserAvatarUrl(userId: String, avatarUrl: String) {
        Log.d(TAG, "Updating avatar URL for user $userId to: $avatarUrl")
        try {
            usersCollection.document(userId).update("avatarUrl", avatarUrl).await()
            Log.d(TAG, "Successfully updated avatar URL for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating avatar URL for user $userId", e)
            throw Exception("Ошибка обновления ссылки на аватар: ${e.message}", e)
        }
    }
} 