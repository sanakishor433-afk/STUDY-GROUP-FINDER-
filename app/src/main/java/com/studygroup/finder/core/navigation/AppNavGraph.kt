package com.studygroup.finder.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.studygroup.finder.ui.auth.AuthViewModel
import com.studygroup.finder.ui.auth.LoginScreen
import com.studygroup.finder.ui.auth.RegisterScreen
import com.studygroup.finder.ui.auth.SplashScreen
import com.studygroup.finder.ui.chat.ChatScreen
import com.studygroup.finder.ui.chat.ChatViewModel
import com.studygroup.finder.ui.home.HomeScreen
import com.studygroup.finder.ui.home.HomeViewModel
import com.studygroup.finder.ui.groups.GroupViewModel
import com.studygroup.finder.ui.groups.GroupListScreen
import com.studygroup.finder.ui.groups.CreateGroupScreen
import com.studygroup.finder.ui.groups.GroupDetailScreen
import com.studygroup.finder.ui.search.SearchScreen
import com.studygroup.finder.ui.search.SearchViewModel

/**
 * Top-level navigation graph for the Study Group Finder app.
 *
 * Auth screens ([SplashScreen], [LoginScreen], [RegisterScreen]) are fully
 * implemented. All other destinations use placeholder composables that will be
 * replaced in later development phases.
 *
 * @param navController  the [NavHostController] that drives navigation.
 * @param startDestination the route to display on first launch (typically
 *        [Screen.Splash]).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── Auth ────────────────────────────────────
        composable(Screen.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            SplashScreen(
                onNavigateToDestination = {
                    val destination = if (authViewModel.isLoggedIn()) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main ────────────────────────────────────
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            HomeScreen(
                viewModel = homeViewModel,
                currentRoute = currentRoute,
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            PlaceholderScreen("Profile")
        }

        composable(Screen.EditProfile.route) {
            PlaceholderScreen("Edit Profile")
        }

        // ── Groups ──────────────────────────────────
        composable(Screen.GroupList.route) {
            val groupViewModel: GroupViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            GroupListScreen(
                viewModel = groupViewModel,
                currentRoute = currentRoute,
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToExplore = {
                    navController.navigate(Screen.Search.route)
                },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(
                navArgument(Screen.GroupDetail.ARG_GROUP_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(Screen.GroupDetail.ARG_GROUP_ID).orEmpty()
            val groupViewModel: GroupViewModel = hiltViewModel()

            GroupDetailScreen(
                groupId = groupId,
                viewModel = groupViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { id ->
                    navController.navigate(Screen.Chat.createRoute(id))
                },
                onNavigateToSchedule = { id ->
                    navController.navigate(Screen.ScheduleSession.createRoute(id))
                }
            )
        }

        composable(Screen.CreateGroup.route) {
            val groupViewModel: GroupViewModel = hiltViewModel()

            CreateGroupScreen(
                viewModel = groupViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Search ──────────────────────────────────
        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = hiltViewModel()

            SearchScreen(
                viewModel = searchViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }

        // ── Chat ────────────────────────────────────
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument(Screen.Chat.ARG_GROUP_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(Screen.Chat.ARG_GROUP_ID).orEmpty()
            val chatViewModel: ChatViewModel = hiltViewModel()

            ChatScreen(
                groupId = groupId,
                viewModel = chatViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Sessions ────────────────────────────────
        composable(
            route = Screen.ScheduleSession.route,
            arguments = listOf(
                navArgument(Screen.ScheduleSession.ARG_GROUP_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(Screen.ScheduleSession.ARG_GROUP_ID).orEmpty()
            PlaceholderScreen("Schedule Session\ngroupId = $groupId")
        }

        composable(Screen.SessionDetail.route) {
            PlaceholderScreen("Session Detail")
        }

        // ── Notifications ───────────────────────────
        composable(Screen.Notifications.route) {
            PlaceholderScreen("Notifications")
        }

        // ── Reviews ─────────────────────────────────
        composable(
            route = Screen.Reviews.route,
            arguments = listOf(
                navArgument(Screen.Reviews.ARG_GROUP_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(Screen.Reviews.ARG_GROUP_ID).orEmpty()
            PlaceholderScreen("Reviews\ngroupId = $groupId")
        }

        // ── Tracking ────────────────────────────────
        composable(Screen.ActivityTracking.route) {
            PlaceholderScreen("Activity Tracking")
        }

        // ── Admin ───────────────────────────────────
        composable(Screen.AdminPanel.route) {
            PlaceholderScreen("Admin Panel")
        }
    }
}

/**
 * Temporary full-screen placeholder used until real screen composables are built.
 */
@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}

