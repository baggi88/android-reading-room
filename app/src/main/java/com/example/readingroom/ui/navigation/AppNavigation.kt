package com.example.readingroom.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingroom.ui.auth.AuthState
import com.example.readingroom.ui.auth.AuthViewModel
import com.example.readingroom.ui.auth.LoginScreen
import com.example.readingroom.ui.auth.SignUpScreen
import com.example.readingroom.ui.components.BottomNavigationBar
import com.example.readingroom.ui.screens.book.BookDetailsScreen
import com.example.readingroom.ui.screens.favorites.FavoritesScreen
import com.example.readingroom.ui.screens.friends.FriendsScreen
import com.example.readingroom.ui.screens.library.LibraryScreen
import com.example.readingroom.ui.screens.profile.ProfileScreen
import com.example.readingroom.ui.screens.reading_stats.ReadingStatsScreen
import com.example.readingroom.ui.screens.settings.SettingsScreen
import com.example.readingroom.ui.screens.statistics.StatisticsScreen
import com.example.readingroom.ui.screens.wishlist.WishlistScreen
import com.example.readingroom.ui.screens.addbook.AddBookScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import com.example.readingroom.ui.screens.friendlibrary.FriendLibraryScreen
import com.example.readingroom.ui.screens.manualaddbook.ManualAddBookScreen

// Определение маршрутов навигации
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Library : Screen("library")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object Profile : Screen("profile?userId={userId}") {
        fun createRoute() = "profile"
        fun createRoute(userId: String) = "profile?userId=$userId"
    }
    object ReadingStats : Screen("reading_stats")
    object Settings : Screen("settings")
    object Statistics : Screen("statistics")
    object Favorites : Screen("favorites")
    object Wishlist : Screen("wishlist")
    object Friends : Screen("friends")
    object AddBook : Screen("add_book")
    object ManualAddBook : Screen("manual_add_book")
    object FriendLibrary : Screen("friendLibrary")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Screen.Library.route, Screen.ReadingStats.route, Screen.Wishlist.route, Screen.Favorites.route, Screen.Friends.route, Screen.Profile.route)) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (authState == AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = if (authState is AuthState.Authenticated) Screen.Library.route else Screen.Login.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Маршруты аутентификации
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                    )
                }
                
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                    )
                }
                
                // Основные маршруты приложения
                composable(Screen.Library.route) {
                    Log.d("AppNavigation", "Composing LibraryScreen for route: ${Screen.Library.route}")
                    LibraryScreen(
                        onNavigateToBookDetails = { bookId -> 
                            navController.navigate(Screen.BookDetails.createRoute(bookId)) 
                        },
                        onNavigateToAddBook = { navController.navigate(Screen.AddBook.route) },
                        paddingValues = paddingValues,
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                
                composable(
                    route = Screen.BookDetails.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    BookDetailsScreen(
                        bookId = bookId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = Screen.Profile.route,
                    arguments = listOf(
                        navArgument("userId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) {
                    Log.d("AppNavigation", "Composing ProfileScreen for route: ${Screen.Profile.route}")
                    ProfileScreen(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.ReadingStats.route) {
                    ReadingStatsScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                
                composable(Screen.Statistics.route) {
                    StatisticsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToBookDetails = { bookId -> 
                            navController.navigate(Screen.BookDetails.createRoute(bookId)) 
                        }
                    )
                }
                
                composable(Screen.Wishlist.route) {
                    WishlistScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToBookDetails = { bookId -> 
                            navController.navigate(Screen.BookDetails.createRoute(bookId)) 
                        }
                    )
                }
                
                composable(Screen.Friends.route) {
                    FriendsScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.createRoute()) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToFriendProfile = { friendId ->
                            navController.navigate(Screen.FriendLibrary.route + "/$friendId")
                        }
                    )
                }
                
                composable(Screen.AddBook.route) {
                    AddBookScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToManualAddBook = { navController.navigate(Screen.ManualAddBook.route) },
                        onNavigateToBookDetails = { bookId -> navController.navigate(Screen.BookDetails.createRoute(bookId)) }
                    )
                }
                
                // Добавляем composable для нового экрана
                composable(Screen.ManualAddBook.route) {
                    ManualAddBookScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                // Новый экран библиотеки друга
                composable(
                    route = Screen.FriendLibrary.route + "/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    // userId будет автоматически передан в SavedStateHandle FriendLibraryViewModel
                    FriendLibraryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBookDetails = { bookId ->
                            // Переходим на обычный экран деталей книги
                            navController.navigate("${Screen.BookDetails.route}/$bookId")
                        }
                    )
                }
            }
        }
    }
} 