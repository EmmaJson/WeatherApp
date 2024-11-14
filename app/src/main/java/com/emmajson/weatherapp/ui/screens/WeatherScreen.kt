@file:OptIn(ExperimentalMaterial3Api::class)

package com.emmajson.weatherapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emmajson.weatherapp.ui.screencomponents.WeatherItem
import com.emmajson.weatherapp.ui.screencomponents.WeatherItemShimmer
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    // Observe the weather data from the ViewModel
    val weatherData by viewModel.weatherData.observeAsState()
    var loadedItemsCount by remember { mutableStateOf(0) }
    val totalItems = 7

    LaunchedEffect(weatherData) {
        viewModel.fetchWeather(14.333,60.383)
    }
    // Gradually increase the loaded items count with a delay
    LaunchedEffect(weatherData) {
        while (loadedItemsCount < (weatherData?.size ?: 0)) {
            delay(300)
            loadedItemsCount++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0089FF))
            .padding(16.dp)
    ) {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Check if weather data is loaded
        weatherData?.let { timeSeries ->
            Log.d("WeatherScreen", "weatherData: $timeSeries")

            if (timeSeries.isEmpty()) {
                Log.d("WeatherScreen", "timeSeries is empty")
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                return@let
            }

            // Define the start date and filter data for the next 7 days
            val currentDate = LocalDate.of(2021, 11, 3)
            val filteredTimeSeries = timeSeries.filter {
                val forecastDate = LocalDate.parse(it.validTime.substring(0, 10))
                !forecastDate.isBefore(currentDate) && forecastDate.isBefore(currentDate.plusDays(7))
            }

            // Group the filtered data by date and aggregate temperature data
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

            // Display the weather forecast in a LazyColumn with incremental loading
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dailyForecasts.entries.take(totalItems)) { (date, forecast) ->
                    val index = dailyForecasts.keys.indexOf(date)
                    if (index < loadedItemsCount) {
                        WeatherItem(date, forecast.first, forecast.second, forecast.third)
                    } else {
                        WeatherItemShimmer()
                    }
                }
            }
        } ?: run {
            // Show loading text if data is not yet available
            Text(
                text = "Loading data...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}
