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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.emmajson.weatherapp.ui.screencomponents.WeatherHourlyItem
import com.emmajson.weatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailScreen(
    dayIndex: Int,
    viewModel: WeatherViewModel = viewModel(),
    navController: NavController
) {
    val weatherData by viewModel.weatherData.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("24-Hour Forecast") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0089FF))
                .padding(paddingValues)
        ) {
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

                val filledHourlyForecasts = viewModel.interpolateHourlyData(hourlyForecasts, selectedDayDate)

                Log.d(
                    "DetailScreen",
                    "Hourly Forecasts After Interpolation: $filledHourlyForecasts"
                )




                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filledHourlyForecasts) { forecast ->
                        val time = forecast.validTime.substring(11, 16)
                        val temperature =
                            forecast.parameters.find { it.name == "t" }?.values?.firstOrNull()
                        val weatherSymbol =
                            forecast.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

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
}
