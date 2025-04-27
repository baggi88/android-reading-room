package com.example.readingroom.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.readingroom.model.User
import com.example.readingroom.ui.auth.AuthViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.readingroom.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileViewModel.updateAvatar(uri)
        } else {
            Toast.makeText(context, "Выбор изображения отменен", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileState.isCurrentUserProfile) "Мой профиль" else "Профиль пользователя") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
    ) { paddingValues ->
        if (profileState.isLoading && profileState.user == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (profileState.user != null) {
                        val user = profileState.user!!
                        val isCurrentUser = profileState.isCurrentUserProfile

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isCurrentUser) Modifier.clickable { 
                                        pickAvatarLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    else Modifier
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val model = if (user.avatarUrl.isNotBlank()) user.avatarUrl else R.drawable.following

                            AsyncImage(
                                model = model,
                                contentDescription = "Аватар пользователя ${user.nickname}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.following),
                                error = painterResource(id = R.drawable.following)
                            )

                            if (isCurrentUser) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Редактировать аватар",
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                        .padding(4.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = user.nickname.takeIf { it.isNotBlank() } ?: "(Никнейм не указан)",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (isCurrentUser) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            Text("Редактировать никнейм:", style = MaterialTheme.typography.titleMedium)
                            
                            var nicknameInput by remember { mutableStateOf(user.nickname) }
                            var isNicknameValid by remember { mutableStateOf(true) }

                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { 
                                    nicknameInput = it
                                    isNicknameValid = it.length >= 3 
                                },
                                label = { Text("Новый никнейм") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isNicknameValid,
                                trailingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = "Редактировать никнейм")
                                }
                            )
                            if (!isNicknameValid) {
                                Text(
                                    text = "Никнейм должен быть не менее 3 символов",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                                )
                            }
                            
                             Button(
                                onClick = { profileViewModel.updateNickname(nicknameInput) },
                                enabled = isNicknameValid && nicknameInput != user.nickname && !profileState.isUpdating,
                                modifier = Modifier.align(Alignment.End).padding(top=8.dp)
                            ) {
                                Text("Сохранить никнейм")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            // Добавляем переключатель "Разрешить добавление в друзья"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Разрешить добавление в друзья", style = MaterialTheme.typography.bodyLarge)
                                Switch(
                                    checked = profileState.allowAddingByNickname ?: false,
                                    onCheckedChange = { profileViewModel.updateAllowAddingByNickname(it) },
                                    enabled = profileState.user != null && !profileState.isUpdating,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            
                            OutlinedButton(
                                onClick = { authViewModel.signOut() },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    tint = LocalContentColor.current
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Выход")
                            }
                        }
                    } else if(profileState.error != null && !profileState.isLoading) {
                        Text(
                            text = profileState.error ?: "Не удалось загрузить профиль",
                            color = MaterialTheme.colorScheme.error,
                             modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                if (profileState.error != null && !profileState.isLoading && profileState.user != null) {
                    SnackbarHost(
                        hostState = remember { SnackbarHostState() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Snackbar(snackbarData = it)
                    }
                     LaunchedEffect(profileState.error) {
                         Toast.makeText(context, profileState.error, Toast.LENGTH_LONG).show()
                     }
                }
                
                if (profileState.isUpdating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
} 