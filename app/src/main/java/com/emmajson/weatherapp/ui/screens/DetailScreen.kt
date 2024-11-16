package com.emmajson.weatherapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.ui.screencomponents.WeatherItem
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailScreen(dayIndex: Int, viewModel: WeatherViewModel = viewModel()) {
    val weatherData by viewModel.weatherData.observeAsState()

    LaunchedEffect(weatherData) {
        viewModel.fetchWeather(lon = 14.333, lat = 60.383)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0089FF))
            .padding(16.dp)
    ) {
        Text(
            text = "24-Hour Forecast",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Check if weather data is loaded
        weatherData?.let { timeSeries ->
            Log.d("DetailScreen", "weatherData: $timeSeries")

            if (timeSeries.isEmpty()) {
                Log.d("DetailScreen", "timeSeries is empty")
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                return@let
            }

            // Get the selected day's forecast based on the dayIndex
            val selectedDayDate = timeSeries.getOrNull(dayIndex)?.validTime?.substring(0, 10)

            if (selectedDayDate == null) {
                Text(
                    text = "No data available for the selected day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                return@let
            }

            // Filter the time series for the selected date
            val filteredTimeSeries = timeSeries.filter {
                it.validTime.startsWith(selectedDayDate)
            }

            // Display the hourly forecast in a LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTimeSeries) { forecast ->
                    val time = forecast.validTime.substring(11, 16) // Extract time (HH:mm)
                    val temperature = forecast.parameters.find { it.name == "t" }?.values?.firstOrNull()
                    val weatherSymbol = forecast.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

                    WeatherItem(
                        time = time,
                        temperature = temperature,
                        weatherSymbol = weatherSymbol
                    )
                }
            }
        } ?: Text(
            text = "Loading data...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
fun WeatherItem(time: String, temperature: Double?, weatherSymbol: Double?) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Time: $time")
        Text(text = "Temperature: ${temperature ?: "N/A"}°C")
        Text(text = "Symbol: ${weatherSymbol ?: "N/A"}")
    }
}

