package com.neetquest.neetquestsaver.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.neetquest.neetquestsaver.ui.screens.categories.CategoriesScreen
import com.neetquest.neetquestsaver.ui.screens.crop.CropEditorScreen
import com.neetquest.neetquestsaver.ui.screens.crop.SaveQuestionScreen
import com.neetquest.neetquestsaver.ui.screens.detail.QuestionDetailScreen
import com.neetquest.neetquestsaver.ui.screens.home.HomeScreen
import com.neetquest.neetquestsaver.ui.screens.home.SavedQuestionsScreen
import com.neetquest.neetquestsaver.ui.screens.settings.SettingsScreen
import com.neetquest.neetquestsaver.ui.theme.NEETQuestSaverTheme
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NEETQuestSaverTheme {
                NEETQuestApp()
            }
        }
    }
}

@Composable
fun NEETQuestApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Home.route),
        BottomNavItem("Saved", Icons.Default.BookmarkBorder, Screen.SavedQuestions.route),
        BottomNavItem("Categories", Icons.Default.Category, Screen.Categories.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route),
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.SavedQuestions.route) {
                SavedQuestionsScreen(navController = navController)
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(
                route = Screen.QuestionDetail.route,
                arguments = listOf(navArgument("questionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("questionId") ?: return@composable
                QuestionDetailScreen(questionId = id, navController = navController)
            }
            composable(Screen.CropEditor.route) {
                CropEditorScreen(navController = navController)
            }
            composable(Screen.SaveQuestion.route) {
                SaveQuestionScreen(navController = navController)
            }
            composable(Screen.AddManual.route) {
                SaveQuestionScreen(navController = navController, isManual = true)
            }
        }
    }
}
