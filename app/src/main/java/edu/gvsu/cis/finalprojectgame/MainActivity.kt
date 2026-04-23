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
                                onGoToSettings = {navc.navigate(GameRoute.ToSettingsScreen)},
                                onGoToPostGame = {navc.navigate(GameRoute.ToPostGameScreen)}
                            )
                        }
                        composable<GameRoute.ToSettingsScreen> {
                            SettingsScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onGoToColorSelector = {navc.navigate(GameRoute.ToColorSelectorScreen)},
                                onGoToPostGame = {navc.navigate(GameRoute.ToPostGameScreen)},
                                onBack = {navc.popBackStack()},
                                onGoToSignUp = {navc.navigate(GameRoute.toSignUpScreen)},
                                onGoToLogin = {navc.navigate(GameRoute.toLoginScreen)},
                                onGoToAccount = {navc.navigate(GameRoute.ToAccountScreen)}
                            )
                        }
                        composable<GameRoute.ToColorSelectorScreen> {
                            ColorSelectorScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
                                onBack = {navc.popBackStack()})
                        }
                        composable<GameRoute.ToPostGameScreen> {
                            PostGameScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onGoToSettings = { navc.navigate(GameRoute.ToSettingsScreen) },
                                onBack = { navc.popBackStack(route = GameRoute.ToMainMenuScreen, inclusive = false) },
                                onGoToHandDetails = { navc.navigate(GameRoute.ToGameDetailScreen)})
                        }
                        composable<GameRoute.ToGameDetailScreen> {
                            GameDetailScreen(modifier = Modifier.padding(innerPadding),
                                viewModel,
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
                            AccountInfoScreen(modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()},
                                onToChangePassword = {navc.navigate(GameRoute.ToPasswordChange)}
                            )
                        }
                        composable<GameRoute.ToPasswordChange> {
                            ChangePasswordScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onBack = {navc.popBackStack()}
                            )
                        }
                    }
                }
            }
        }
    }
}
