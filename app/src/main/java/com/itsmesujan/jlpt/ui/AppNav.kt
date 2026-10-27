package com.itsmesujan.jlpt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.ui.screens.LessonScreen
import com.itsmesujan.jlpt.ui.screens.MockScreen
import com.itsmesujan.jlpt.ui.screens.OnboardingScreen
import com.itsmesujan.jlpt.ui.screens.PathScreen
import com.itsmesujan.jlpt.ui.screens.PaywallScreen
import com.itsmesujan.jlpt.ui.screens.ProfileScreen
import com.itsmesujan.jlpt.ui.screens.ReviewScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val PATH = "path"
    const val PRACTICE = "practice"
    const val PROFILE = "profile"
    const val LESSON = "lesson/{nodeId}"
    const val MOCK = "mock/{nodeId}"
    const val PAYWALL = "paywall/{moment}"
    fun lesson(nodeId: String) = "lesson/$nodeId"
    fun mock(nodeId: String) = "mock/$nodeId"
    fun paywall(moment: String) = "paywall/$moment"
}

private val bottomDestinations = listOf(
    Triple(Routes.PATH, "Path", Icons.Filled.Map),
    Triple(Routes.PRACTICE, "Practice", Icons.Filled.SportsEsports),
    Triple(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun AppNav(viewModel: AppViewModel) {
    val profile by viewModel.profile.collectAsState()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    if (profile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val showBottomBar = currentRoute in bottomDestinations.map { it.first }
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = { navController.navigate(route) { popUpTo(Routes.PATH) { saveState = true }; launchSingleTop = true; restoreState = true } },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (profile?.onboarded == true) Routes.PATH else Routes.ONBOARDING,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onDone = { navController.navigate(Routes.PATH) { popUpTo(Routes.ONBOARDING) { inclusive = true } } },
                    onSeePremium = { navController.navigate(Routes.paywall("POST_PLACEMENT")) }
                )
            }
            composable(Routes.PATH) {
                PathScreen(
                    viewModel = viewModel,
                    onOpenNode = { node -> navController.navigate(if (node.isBoss) Routes.mock(node.id) else Routes.lesson(node.id)) },
                    onOpenReview = { navController.navigate(Routes.PRACTICE) }
                )
            }
            composable(Routes.PRACTICE) {
                ReviewScreen(
                    viewModel = viewModel,
                    onOpenMock = { nodeId -> navController.navigate(Routes.mock(nodeId)) },
                    onOpenPremium = { navController.navigate(Routes.paywall("AT_LIMIT")) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(viewModel = viewModel, onSeePremium = { navController.navigate(Routes.paywall("SETTINGS")) })
            }
            composable(Routes.LESSON, arguments = listOf(navArgument("nodeId") { type = NavType.StringType })) { entry ->
                val node = Content.node(entry.arguments?.getString("nodeId") ?: "")
                if (node == null) { Text("Lesson not found"); return@composable }
                LessonScreen(
                    viewModel = viewModel, node = node,
                    onClose = { navController.popBackStack() },
                    onReviewMistakes = { navController.navigate(Routes.PRACTICE) }
                )
            }
            composable(Routes.MOCK, arguments = listOf(navArgument("nodeId") { type = NavType.StringType })) { entry ->
                val node = Content.node(entry.arguments?.getString("nodeId") ?: "")
                if (node == null) { Text("Mock not found"); return@composable }
                MockScreen(
                    viewModel = viewModel, node = node,
                    onClose = { navController.popBackStack() },
                    onSeePremium = { navController.navigate(Routes.paywall("POST_MOCK")) }
                )
            }
            composable(Routes.PAYWALL, arguments = listOf(navArgument("moment") { type = NavType.StringType })) { entry ->
                PaywallScreen(
                    viewModel = viewModel,
                    moment = entry.arguments?.getString("moment") ?: "SETTINGS",
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
