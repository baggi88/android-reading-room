package com.example.readingroom.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.model.User
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.example.readingroom.R
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToFriendProfile: (String) -> Unit,
    paddingValues: PaddingValues,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Друзья") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Профиль")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            label = { Text("Найти по никнейму") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.PersonSearch, contentDescription = "Иконка поиска")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (uiState.searchError != null) {
                        Text(uiState.searchError!!, color = MaterialTheme.colorScheme.error)
                    }

                    if (uiState.searchResults.isNotEmpty()) {
                        Text("Результаты поиска:", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(uiState.searchResults, key = { it.uid }) { user ->
                                SearchResultItem(user = user, onAddFriend = {
                                    viewModel.addFriend(user.uid)
                                })
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Мои друзья:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!uiState.isLoadingFriends && uiState.friendsError == null) {
                        Text("(${uiState.friendsList.size})", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if (uiState.isLoadingFriends) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.friendsError != null) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Ошибка: ${uiState.friendsError}", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else if (uiState.friendsList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text("У вас пока нет друзей")
                    }
                }
            } else {
                items(uiState.friendsList, key = { it.uid }) { friend ->
                    FriendItem(
                        user = friend,
                        onClick = { onNavigateToFriendProfile(friend.uid) },
                        onRemoveClick = { viewModel.removeFriend(friend.uid) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(user: FriendsUserUiModel, onAddFriend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(user.nickname, style = MaterialTheme.typography.bodyLarge)
        if (user.isAdding) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp).padding(6.dp))
        } else {
            IconButton(onClick = onAddFriend, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Добавить в друзья")
            }
        }
    }
}

@Composable
fun FriendItem(user: FriendsUserUiModel, onClick: () -> Unit, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Аватар ${user.nickname}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.following),
                    placeholder = painterResource(id = R.drawable.following)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(user.nickname, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (user.isRemoving) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp).padding(6.dp))
            } else {
                IconButton(onClick = {
                    onRemoveClick()
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить друга")
                }
            }
        }
    }
} 