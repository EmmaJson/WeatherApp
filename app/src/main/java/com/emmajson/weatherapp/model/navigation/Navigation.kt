package com.emmajson.weatherapp.model.navigation

import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.emmajson.weatherapp.ui.SearchViewModel
import com.emmajson.weatherapp.ui.screens.DetailScreen
import com.emmajson.weatherapp.ui.screens.SearchScreen
import com.emmajson.weatherapp.ui.screens.WeatherScreen

sealed class Screen(val route: String) {
    object WeatherScreen : Screen("weather_screen")
    object DetailScreen : Screen("detail/{dayIndex}") {
        fun createRoute(dayIndex: Int) = "detail/$dayIndex"
    }
    object SearchScreen : Screen("search_screen")
}

@Composable
fun Navigation(vm: WeatherViewModel) {
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
                searchViewModel = SearchViewModel(),
                onCitySelected = { city ->
                    // Update WeatherViewModel with the selected city and navigate back
                    vm.setSearchedCity(city = city)
                }
            )
        }
    }
}