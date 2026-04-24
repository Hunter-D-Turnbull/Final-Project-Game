package edu.gvsu.cis.finalprojectgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.gvsu.cis.finalprojectgame.ui.theme.FinalProjectGameTheme

class MainActivity : ComponentActivity() {
    val viewModel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectGameTheme {
                val navc = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navc,
                        startDestination = GameRoute.ToMainMenuScreen
                    ) {
                        composable<GameRoute.ToMainMenuScreen> {
                            MainMenuScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onGoToGameScreen = {navc.navigate(GameRoute.ToGameScreen)},
                                onGoToSettingsScreen = {navc.navigate(GameRoute.ToSettingsScreen)}
                            )
                        }
                        composable<GameRoute.ToGameScreen> {
                            GameScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onGoToPostGame = {navc.navigate(GameRoute.ToPostGameScreen)}
                            )
                        }
                        composable<GameRoute.ToSettingsScreen> {
                            SettingsScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onGoToColorSelector = {navc.navigate(GameRoute.ToColorSelectorScreen)},
                                onBack = {navc.popBackStack()},
                                onGoToSignUp = {navc.navigate(GameRoute.toSignUpScreen)},
                                onGoToLogin = {navc.navigate(GameRoute.toLoginScreen)},
                                onGoToAccount = {navc.navigate(GameRoute.ToAccountScreen)},
                                onGoToLocalHistory = {navc.navigate(GameRoute.ToLocalGameHistory)},
                                onGoToRemoteGameHistory = {navc.navigate(GameRoute.ToRemoteGameHistory)},
                                onGoToLeaderboard = {navc.navigate(GameRoute.ToLeaderboardScreen)}
                            )
                        }
                        composable<GameRoute.ToColorSelectorScreen> {
                            ColorSelectorScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onBack = {navc.popBackStack()})
                        }
                        composable<GameRoute.ToLocalGameHistory> {
                            LocalGameHistory(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = { navc.popBackStack()},
                                onGoToHandDetails = { navc.navigate(GameRoute.ToGameDetailScreen)})
                        }
                        composable<GameRoute.ToGameDetailScreen> {
                            GameDetailScreen(modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()})
                        }
                        composable<GameRoute.toSignUpScreen> {
                            SignupScreen(modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()},
                                onDone = {navc.popBackStack()}
                            )
                        }
                        composable<GameRoute.toLoginScreen> {
                            LoginScreen(modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()},
                                onLoginSuccess = {navc.popBackStack()},
                                onGoToSignup = {navc.navigate(GameRoute.toSignUpScreen)}
                            )
                        }
                        composable<GameRoute.ToAccountScreen> {
                            AccountInfoScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = { navc.popBackStack() },
                                onToChangePassword = { navc.navigate(GameRoute.ToPasswordChange) },
                                onToAchievements = {navc.navigate(GameRoute.ToAchievementsScreen)},
                                onToAccountDeletion = {navc.navigate(GameRoute.ToAccountDeletionScreen)}
                            )
                        }
                        composable<GameRoute.ToPasswordChange> {
                            ChangePasswordScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()}
                            )
                        }
                        composable<GameRoute.ToPostGameScreen> {
                            PostGameScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onMainMenu = {navc.popBackStack(route = GameRoute.ToMainMenuScreen, inclusive = false)}
                            )
                        }
                        composable<GameRoute.ToRemoteGameHistory> {
                            RemoteGameHistoryScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()},
                                onGoToHandDetails = {navc.navigate(GameRoute.ToRemoteGameHistoryDetails)}
                            )
                        }
                        composable<GameRoute.ToRemoteGameHistoryDetails> {
                            RemoteGameDetailScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()}
                            )
                        }
                        composable<GameRoute.ToAchievementsScreen> {
                            AchievementsScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()}
                            )
                        }
                        composable<GameRoute.ToLeaderboardScreen> {
                            LeaderboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()}
                            )
                        }
                        composable<GameRoute.ToAccountDeletionScreen>{
                            AccountDeletionScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onDeleted = {navc.popBackStack(route = GameRoute.ToSettingsScreen, inclusive = false)},
                                onBack = {navc.popBackStack()}
                            )
                        }
                    }
                }
            }
        }
    }
}