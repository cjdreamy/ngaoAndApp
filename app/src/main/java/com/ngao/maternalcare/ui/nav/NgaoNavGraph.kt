package com.ngao.maternalcare.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository
import com.ngao.maternalcare.ui.screens.auth.AuthViewModel
import com.ngao.maternalcare.ui.screens.auth.LoginScreen
import com.ngao.maternalcare.ui.screens.auth.SignUpScreen
import com.ngao.maternalcare.ui.screens.auth.SplashScreen
import com.ngao.maternalcare.ui.screens.education.EducationScreen
import com.ngao.maternalcare.ui.screens.education.EducationViewModel
import com.ngao.maternalcare.ui.screens.mother.CheckInScreen
import com.ngao.maternalcare.ui.screens.mother.MotherDashboardScreen
import com.ngao.maternalcare.ui.screens.mother.MotherDashboardViewModel
import com.ngao.maternalcare.ui.screens.provider.ProviderDashboardScreen
import com.ngao.maternalcare.ui.screens.provider.ProviderDashboardViewModel
import com.ngao.maternalcare.util.ViewModelFactory
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MOTHER_DASHBOARD = "mother_dashboard"
    const val CHECK_IN = "check_in"
    const val EDUCATION = "education"
    const val PROVIDER_DASHBOARD = "provider_dashboard"
}

@Composable
fun NgaoNavGraph(
    repository: NgaoRepository,
    sessionManager: SessionManager
) {
    val navController: NavHostController = rememberNavController()

    fun <T : androidx.lifecycle.ViewModel> factory(creator: (NgaoRepository, SessionManager) -> T) =
        ViewModelFactory(repository, sessionManager, creator)

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(sessionManager) { destination ->
                navController.navigate(destination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(factory = factory { r, s -> AuthViewModel(r, s) })
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    val destination = if (role == "provider") Routes.PROVIDER_DASHBOARD else Routes.MOTHER_DASHBOARD
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            val authViewModel: AuthViewModel = viewModel(factory = factory { r, s -> AuthViewModel(r, s) })
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { role ->
                    val destination = if (role == "provider") Routes.PROVIDER_DASHBOARD else Routes.MOTHER_DASHBOARD
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MOTHER_DASHBOARD) {
            val vm: MotherDashboardViewModel = viewModel(factory = factory { r, s -> MotherDashboardViewModel(r, s) })
            val scope = rememberCoroutineScope()
            MotherDashboardScreen(
                viewModel = vm,
                onStartCheckIn = { navController.navigate(Routes.CHECK_IN) },
                onOpenEducation = { navController.navigate(Routes.EDUCATION) },
                onLogout = {
                    scope.launch {
                        repository.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.CHECK_IN) {
            // Reuse the parent dashboard's ViewModel instance so the new check-in
            // shows up immediately when we navigate back.
            val parentEntry = remember { navController.getBackStackEntry(Routes.MOTHER_DASHBOARD) }
            val vm: MotherDashboardViewModel = viewModel(
                parentEntry,
                factory = factory { r, s -> MotherDashboardViewModel(r, s) }
            )
            CheckInScreen(viewModel = vm, onDone = { navController.popBackStack() })
        }

        composable(Routes.EDUCATION) {
            val vm: EducationViewModel = viewModel(factory = factory { r, s -> EducationViewModel(r, s) })
            EducationScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.PROVIDER_DASHBOARD) {
            val vm: ProviderDashboardViewModel = viewModel(factory = factory { r, s -> ProviderDashboardViewModel(r, s) })
            val scope = rememberCoroutineScope()
            var fullName by remember { mutableStateOf("") }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                fullName = sessionManager.currentFullName() ?: ""
            }
            ProviderDashboardScreen(
                viewModel = vm,
                fullName = fullName,
                onLogout = {
                    scope.launch {
                        repository.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onOpenEducation = { navController.navigate(Routes.EDUCATION) }
            )
        }
    }
}
