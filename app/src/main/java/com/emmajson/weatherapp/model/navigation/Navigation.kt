package com.emmajson.weatherapp.model.navigation

import SearchViewModel
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.emmajson.weatherapp.ui.screens.DetailScreen
import com.emmajson.weatherapp.ui.screens.SearchScreen
import com.emmajson.weatherapp.ui.screens.SettingsScreen
import com.emmajson.weatherapp.ui.screens.WeatherScreen
import com.emmajson.weatherapp.ui.theme.WeatherAppTheme

sealed class Screen(val route: String) {
    object WeatherScreen : Screen("weather_screen")
    object DetailScreen : Screen("detail/{dayIndex}") {
        fun createRoute(dayIndex: Int) = "detail/$dayIndex"
    }

    object SearchScreen : Screen("search_screen")
    object SettingScreen : Screen("setting_screen")
}

@Composable
fun Navigation(vm: WeatherViewModel, svm: SearchViewModel) {
    val darkModeEnabled by vm.darkModeEnabled.collectAsState()

    WeatherAppTheme(darkTheme = darkModeEnabled) {

        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.WeatherScreen.route) {
            composable(route = Screen.WeatherScreen.route) {
                Crossfade(targetState = Screen.WeatherScreen) {
                    WeatherScreen(vm, navController = navController)
                }
            }
            composable(
                route = Screen.DetailScreen.route,
                arguments = listOf(navArgument("dayIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                Crossfade(targetState = Screen.WeatherScreen) {
                    DetailScreen(dayIndex, vm, navController = navController)
                }
            }

            composable(Screen.SearchScreen.route) {
                SearchScreen(
                    navController = navController,
                    searchViewModel = svm,
                    onCitySelected = { city ->
                        // Update WeatherViewModel with the selected city and navigate back
                        vm.setSearchedCity(city = city)
                    }
                )
            }

            composable(Screen.SettingScreen.route) {
                Crossfade(targetState = Screen.SettingScreen) {
                    SettingsScreen(vm, navController)
                }
            }
        }
    }
}