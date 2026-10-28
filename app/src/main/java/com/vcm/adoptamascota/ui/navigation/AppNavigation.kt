package com.vcm.adoptamascota.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import com.vcm.adoptamascota.auth.LoginScreen
import com.vcm.adoptamascota.ui.screens.AdoptionRequestScreen
import com.vcm.adoptamascota.ui.screens.LocationPickerScreen
import com.vcm.adoptamascota.ui.screens.LocationTarget
import com.vcm.adoptamascota.ui.viewmodel.AdoptionViewModel
import com.vcm.adoptamascota.utils.coordinatesOf
import com.vcm.adoptamascota.data.ServicesData
import com.vcm.adoptamascota.ui.screens.AddLostPetScreen
import com.vcm.adoptamascota.ui.screens.LostPetsScreen
import com.vcm.adoptamascota.ui.screens.ServiceScreen
import com.vcm.adoptamascota.ui.viewmodel.LostPetViewModel
import com.vcm.adoptamascota.ui.screens.AddPetScreen
import com.vcm.adoptamascota.ui.screens.AdoptionListScreen
import com.vcm.adoptamascota.ui.screens.HomeScreen
import com.vcm.adoptamascota.ui.screens.OnboardingScreen
import com.vcm.adoptamascota.ui.screens.PetDetailScreen
import com.vcm.adoptamascota.ui.screens.WelcomeScreen
import com.vcm.adoptamascota.ui.viewmodel.PetViewModel

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val LOGIN_FROM_HOME = "login_home"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ADOPTION = "adoption"
    const val ADD = "add"
    const val DETAIL = "detail/{petId}"
    const val ADOPT_REQUEST = "adopt/{petId}"
    const val PICK_LOCATION = "pick_location/{target}"
    fun adoptRequest(id: Int) = "adopt/$id"
    fun pickLocation(target: LocationTarget) = "pick_location/${target.name}"
    const val LOST = "lost"
    const val ADD_LOST = "add_lost"
    const val SERVICE = "service/{name}"
    fun detail(id: Int) = "detail/$id"
    fun service(name: String) = "service/$name"
}

/**
 * REQ. 3 (Navigation Compose). Flujo completo:
 * Welcome -> Onboarding -> Home -> AdoptionList -> (AddPet | Detalle).
 * Un único PetViewModel (scope de la Activity) se comparte entre las pantallas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: PetViewModel = viewModel()
    val lostViewModel: LostPetViewModel = viewModel()
    val adoptionViewModel: AdoptionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
        // Animaciones de transición entre pantallas: deslizamiento + fundido
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onStart = { navController.navigate(Routes.LOGIN) })
        }
        composable(Routes.LOGIN) {
            // Login con Google; al entrar (o al omitir) se sigue al Onboarding sin dejar Login en la pila
            val goOnboarding = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
                Unit
            }
            LoginScreen(onLoggedIn = goOnboarding, onSkip = goOnboarding)
        }
        composable(Routes.ONBOARDING) {
            // Al terminar (o al omitir) se entra a Home y se sacan Welcome/Onboarding de la pila
            OnboardingScreen(onFinish = { navController.goHomeClearingIntro() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                lostViewModel = lostViewModel,
                onAdopt = { navController.navigate(Routes.ADOPTION) },
                onLost = { navController.navigate(Routes.LOST) },
                onService = { name -> navController.navigate(Routes.service(name)) },
                onLogin = { navController.navigate(Routes.LOGIN_FROM_HOME) }
            )
        }
        composable(Routes.LOGIN_FROM_HOME) {
            // Login abierto desde Home: al terminar (o al omitir) se vuelve a Home
            LoginScreen(
                onLoggedIn = { navController.popBackStack() },
                onSkip = { navController.popBackStack() }
            )
        }
        composable(Routes.LOST) {
            LostPetsScreen(
                viewModel = lostViewModel,
                onBack = { navController.popBackStack() },
                onReport = {
                    lostViewModel.resetForm()
                    navController.navigate(Routes.ADD_LOST)
                }
            )
        }
        composable(Routes.ADD_LOST) {
            AddLostPetScreen(viewModel = lostViewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.SERVICE,
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { entry ->
            // Busca el contenido del servicio por nombre; si no existe, vuelve atrás
            val info = ServicesData.find(entry.arguments?.getString("name").orEmpty())
            if (info == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                ServiceScreen(
                    info = info,
                    onBack = { navController.popBackStack() },
                    onOpenAdoption = { navController.navigate(Routes.ADOPTION) }
                )
            }
        }
        composable(Routes.ADOPTION) {
            AdoptionListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddPet = {
                    viewModel.resetForm()
                    navController.navigate(Routes.ADD)
                },
                onPetClick = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
        composable(Routes.ADD) {
            AddPetScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPickLocation = { target -> navController.navigate(Routes.pickLocation(target)) }
            )
        }
        composable(
            route = Routes.PICK_LOCATION,
            arguments = listOf(navArgument("target") { type = NavType.StringType })
        ) { entry ->
            // El resultado se guarda directamente en el formulario compartido del PetViewModel
            val target = LocationTarget.valueOf(entry.arguments?.getString("target") ?: LocationTarget.FOUND.name)
            val form = viewModel.formState.value
            val initial = if (target == LocationTarget.FOUND) coordinatesOf(form.foundLat, form.foundLng)
            else coordinatesOf(form.lostLat, form.lostLng)
            LocationPickerScreen(
                target = target,
                initial = initial,
                onConfirm = { picked ->
                    if (target == LocationTarget.FOUND) viewModel.onFoundLocation(picked.lat, picked.lng)
                    else viewModel.onLostLocation(picked.lat, picked.lng)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("petId") { type = NavType.IntType })
        ) { entry ->
            val petId = entry.arguments?.getInt("petId") ?: 0
            PetDetailScreen(
                petId = petId,
                viewModel = viewModel,
                adoptionViewModel = adoptionViewModel,
                onRequestAdoption = { navController.navigate(Routes.adoptRequest(petId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.ADOPT_REQUEST,
            arguments = listOf(navArgument("petId") { type = NavType.IntType })
        ) { entry ->
            AdoptionRequestScreen(
                petId = entry.arguments?.getInt("petId") ?: 0,
                petViewModel = viewModel,
                adoptionViewModel = adoptionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavController.goHomeClearingIntro() {
    navigate(Routes.HOME) {
        popUpTo(Routes.WELCOME) { inclusive = true }
        launchSingleTop = true
    }
}
