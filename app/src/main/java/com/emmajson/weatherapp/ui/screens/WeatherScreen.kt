package com.emmajson.weatherapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emmajson.weatherapp.model.navigation.Screen
import com.emmajson.weatherapp.ui.screencomponents.WeatherItem
import com.emmajson.weatherapp.ui.screencomponents.WeatherItemShimmer
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, navController: NavController) {
    // Observe the weather data from the ViewModel
    val isDataFromCache by viewModel.isDataFromCache.observeAsState(initial = false)
    val weatherData by viewModel.weatherData.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val searchedCity by viewModel.searchedCity.observeAsState() // For LiveData

    val forecastDays by viewModel.forecastDays.collectAsState()
    var loadedItemsCount by remember { mutableStateOf(0) }
    val totalItems = forecastDays


    LaunchedEffect(weatherData) {
        if (viewModel.searchedCity.value.isNullOrEmpty()) {
            viewModel.setSearchedCity("Stockholm") // Set to default if no value
        } else {
            viewModel.setSearchedCity(searchedCity.toString())
        }
    }

    LaunchedEffect(weatherData) {
        while (loadedItemsCount < (weatherData?.size ?: 0)) {
            delay(300)
            loadedItemsCount++
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isDataFromCache) {
        if (isDataFromCache) {
            snackbarHostState.showSnackbar(
                message = "Data loaded from cache",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(searchedCity.toString()) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SettingScreen.route) }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { navController.navigate(Screen.SearchScreen.route) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "$forecastDays-Day Forecast",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            weatherData?.let { timeSeries ->
                val currentDate = LocalDate.now()
                val filteredTimeSeries = timeSeries.filter {
                    val forecastDate = LocalDate.parse(it.validTime.substring(0, 10))
                    !forecastDate.isBefore(currentDate) && forecastDate.isBefore(currentDate.plusDays(forecastDays.toLong()))
                }

                val dailyForecasts = filteredTimeSeries.groupBy {
                    it.validTime.substring(0, 10)
                }.mapValues { (_, times) ->
                    val temperatures = times.mapNotNull { it.parameters.find { param -> param.name == "t" }?.values?.firstOrNull() }
                    val minTemp = temperatures.minOrNull()
                    val maxTemp = temperatures.maxOrNull()

                    val weatherSymbols = times.mapNotNull { it.parameters.find { param -> param.name == "Wsymb2" }?.values?.firstOrNull() }
                    val dominantSymbol = weatherSymbols.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

                    Triple(minTemp, maxTemp, dominantSymbol)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dailyForecasts.entries.take(totalItems)) { (date, forecast) ->
                        val index = dailyForecasts.keys.indexOf(date)
                        if (index < loadedItemsCount) {
                            WeatherItem(date, forecast.first, forecast.second, forecast.third) {
                                viewModel.setSelectedDayForecast(index)
                                navController.navigate(Screen.DetailScreen.createRoute(index))
                            }
                        } else {
                            WeatherItemShimmer()
                        }
                    }
                }
            } ?: run {
                Text(
                    text = "Loading data...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
