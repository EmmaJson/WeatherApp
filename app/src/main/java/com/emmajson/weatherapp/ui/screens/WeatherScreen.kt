package com.emmajson.weatherapp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emmajson.weatherapp.R
import com.emmajson.weatherapp.ui.FakeWeatherViewModel
import com.emmajson.weatherapp.ui.WeatherVM
import com.emmajson.weatherapp.ui.WeatherViewModel

import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherScreen(viewModel: WeatherVM) {
    val weatherData by viewModel.weatherData.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0089FF))
            .padding(16.dp, 16.dp,16.dp, 16.dp)
    ) {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()
            .background(Color.Cyan)) {

        }
        // TODO: Add searchbar!

        weatherData?.let { timeSeries ->
            Log.d("WeatherScreen", "weatherData: $weatherData")

            if (timeSeries.isEmpty()) {
                Log.d("WeatherScreen", "timeSeries is empty")
            }

            // Get the current date and time
            val currentDate = LocalDate.of(2021, 11, 3) //TODO : change this adter adding real api LocalDate.now()

            // Filter the time series for the next 7 days
            val filteredTimeSeries = timeSeries.filter {
                val forecastDate = java.time.LocalDate.parse(it.validTime.substring(0, 10))
                !forecastDate.isBefore(currentDate) && forecastDate.isBefore(currentDate.plusDays(7))
            }

            // Group the filtered data by date
            val dailyForecasts = filteredTimeSeries.groupBy {
                it.validTime.substring(0, 10) // Extract date "yyyy-MM-dd"
            }.mapValues { (_, times) ->
                val temperatures = times.mapNotNull { it.parameters.find { param -> param.name == "t" }?.values?.firstOrNull() }
                val minTemp = temperatures.minOrNull()
                val maxTemp = temperatures.maxOrNull()

                // Determine the dominant weather symbol (Wsymb2) for the day
                val weatherSymbols = times.mapNotNull { it.parameters.find { param -> param.name == "Wsymb2" }?.values?.firstOrNull() }
                val dominantSymbol = weatherSymbols.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

                Triple(minTemp, maxTemp, dominantSymbol)
            }

            // Display the forecast in a LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 50.dp, 0.dp,0.dp),
                horizontalAlignment = Alignment.End
            ) {
                items(dailyForecasts.entries.toList()) { (date, forecast) ->
                    WeatherItem(date, forecast.first, forecast.second, forecast.third)
                }
            }
        } ?: run {
            Text(
                text = "Loading data...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}


@Composable
fun WeatherItem(date: String, minTemp: Double?, maxTemp: Double?, weatherSymbol: Double?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF0D47A1), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = "High: ${maxTemp?.toInt() ?: "N/A"}°C",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Text(
                text = "Low: ${minTemp?.toInt() ?: "N/A"}°C",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Display the weather symbol
        val iconRes = getWeatherIcon(weatherSymbol)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
    }
}

// Helper function to get the correct drawable resource based on the weather symbol
fun getWeatherIcon(symbol: Double?): Int {
    return when (symbol) {
        1.0 -> R.drawable.ic_sunny                      // Clear sky
        2.0 -> R.drawable.ic_sunny_cloudy               // Nearly clear sky
        3.0 -> R.drawable.ic_partly_cloudy              // Variable cloudiness
        4.0 -> R.drawable.ic_halfclear_sky              // Halfclear sky
        5.0 -> R.drawable.ic_cloudy                     // Cloudy sky
        6.0 -> R.drawable.ic_overcast                   // Overcast
        7.0 -> R.drawable.ic_foggy                      // Fog
        8.0 -> R.drawable.ic_light_rain_showers         // Light rain showers
        9.0 -> R.drawable.ic_moderate_rain_showers      // Moderate rain showers
        10.0 -> R.drawable.ic_heavy_rain_showers        // Heavy rain showers
        11.0 -> R.drawable.ic_thunderstorm              // Thunderstorm
        12.0 -> R.drawable.ic_light_sleet_showers       // Light sleet showers
        13.0 -> R.drawable.ic_moderate_sleet_showers    // Moderate sleet showers
        14.0 -> R.drawable.ic_heavy_sleet_showers       // Heavy sleet showers
        15.0 -> R.drawable.ic_light_snow_showers        // Light snow showers
        16.0 -> R.drawable.ic_moderate_snow_showers     // Moderate snow showers
        17.0 -> R.drawable.ic_heavy_snow_showers        // Heavy snow showers
        18.0 -> R.drawable.ic_light_rain                // Light rain
        19.0 -> R.drawable.ic_moderate_rain             // Moderate rain
        20.0 -> R.drawable.ic_heavy_rain                // Heavy rain
        21.0 -> R.drawable.ic_thunder                   // Thunder
        22.0 -> R.drawable.ic_light_sleet               // Light sleet
        23.0 -> R.drawable.ic_moderate_sleet            // Moderate sleet
        24.0 -> R.drawable.ic_heavy_sleet               // Heavy sleet
        25.0 -> R.drawable.ic_light_snowfall            // Light snowfall
        26.0 -> R.drawable.ic_moderate_snowfall         // Moderate snowfall
        27.0 -> R.drawable.ic_heavy_snowfall            // Heavy snowfall
        else -> R.drawable.ic_unknown                   // Default icon for unrecognized values
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val fakeViewModel = FakeWeatherViewModel()
    WeatherScreen(fakeViewModel)
}


