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
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.ui.screencomponents.WeatherHourlyItem
import com.emmajson.weatherapp.viewmodel.WeatherViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailScreen(
    dayIndex: Int,
    viewModel: WeatherViewModel = viewModel(),
    navController: NavController
) {
    val weatherData by viewModel.weatherData.observeAsState()

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

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Back",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }

        weatherData?.let { timeSeries ->
            val dailyForecasts = timeSeries.groupBy {
                it.validTime.substring(0, 10)
            }.toSortedMap()

            val dailyForecastsList = dailyForecasts.toList()
            val selectedDayDate = dailyForecastsList.getOrNull(dayIndex)?.first

            if (selectedDayDate == null) {
                Text(
                    text = "No data available for the selected day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                return@let
            }

            val hourlyForecasts = timeSeries.filter {
                it.validTime.startsWith(selectedDayDate)
            }

            Log.d("DetailScreen", "Hourly Forecasts Before Interpolation: $hourlyForecasts")

            val filledHourlyForecasts = interpolateHourlyData(hourlyForecasts, selectedDayDate)

            Log.d("DetailScreen", "Hourly Forecasts After Interpolation: $filledHourlyForecasts")


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filledHourlyForecasts) { forecast ->
                    val time = forecast.validTime.substring(11, 16)
                    val temperature = forecast.parameters.find { it.name == "t" }?.values?.firstOrNull()
                    val weatherSymbol = forecast.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

                    WeatherHourlyItem(
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

fun interpolateHourlyData(hourlyForecasts: List<TimeSeries>, selectedDayDate: String): List<TimeSeries> {
    val filledForecasts = mutableListOf<TimeSeries>()
    var previousForecast: TimeSeries? = null

    for (hour in 0 until 24) {
        val targetHour = "%02d:00:00Z".format(hour)
        val targetValidTime = "${selectedDayDate}T$targetHour"

        val matchingForecast = hourlyForecasts.find { forecast ->
            forecast.validTime == targetValidTime
        }

        if (matchingForecast != null) {
            filledForecasts.add(matchingForecast)
            if (previousForecast != null && hour - previousForecast.validTime.substring(11, 13).toInt() > 1) {
                // Interpolate between previous and current for temperature
                filledForecasts.addAll(
                    interpolateBetweenPoints(previousForecast, matchingForecast, previousForecast.validTime.substring(11, 13).toInt(), hour)
                )
            }
            previousForecast = matchingForecast
        } else if (previousForecast != null) {
            Log.d("InterpolateHourlyData", "No valid forecast available for hour: $targetHour, using interpolation.")
        }
    }

    Log.d("InterpolateHourlyData", "Filled Forecasts: $filledForecasts")
    return filledForecasts
}


fun interpolateBetweenPoints(start: TimeSeries, end: TimeSeries, startHour: Int, endHour: Int): List<TimeSeries> {
    val interpolatedPoints = mutableListOf<TimeSeries>()

    for (hour in (startHour + 1) until endHour) {
        val fraction = (hour - startHour).toFloat() / (endHour - startHour).toFloat()

        val interpolatedParameters = start.parameters.map { parameter ->
            val endParameter = end.parameters.find { it.name == parameter.name }

            if (parameter.name == "t" && endParameter != null) {
                // Interpolate temperature (or other numerical values)
                val startValue = parameter.values.firstOrNull() ?: 0.0
                val endValue = endParameter.values.firstOrNull() ?: 0.0

                val interpolatedValue = startValue + fraction * (endValue - startValue)

                parameter.copy(values = listOf(interpolatedValue))
            } else {
                // Keep the same value for qualitative parameters like icons
                parameter
            }
        }

        val interpolatedValidTime = "${start.validTime.substring(0, 11)}${"%02d:00:00Z".format(hour)}"

        interpolatedPoints.add(
            start.copy(
                validTime = interpolatedValidTime,
                parameters = interpolatedParameters
            )
        )
    }

    return interpolatedPoints
}
