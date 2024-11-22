package com.emmajson.weatherapp.ui.screencomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.emmajson.weatherapp.R

@Composable
fun WeatherItem(date: String, minTemp: Float?, maxTemp: Float?, weatherSymbol: Float?, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF0D47A1), shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onClick() },
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
fun getWeatherIcon(symbol: Float?): Int {
    return when (symbol) {
        1.0f -> R.drawable.ic_sunny                      // Clear sky
        2.0f -> R.drawable.ic_sunny_cloudy               // Nearly clear sky
        3.0f -> R.drawable.ic_partly_cloudy              // Variable cloudiness
        4.0f -> R.drawable.ic_halfclear_sky              // Halfclear sky
        5.0f -> R.drawable.ic_cloudy                     // Cloudy sky
        6.0f -> R.drawable.ic_overcast                   // Overcast
        7.0f -> R.drawable.ic_foggy                      // Fog
        8.0f -> R.drawable.ic_light_rain_showers         // Light rain showers
        9.0f -> R.drawable.ic_moderate_rain_showers      // Moderate rain showers
        10.0f -> R.drawable.ic_heavy_rain_showers        // Heavy rain showers
        11.0f -> R.drawable.ic_thunderstorm              // Thunderstorm
        12.0f -> R.drawable.ic_light_sleet_showers       // Light sleet showers
        13.0f -> R.drawable.ic_moderate_sleet_showers    // Moderate sleet showers
        14.0f -> R.drawable.ic_heavy_sleet_showers       // Heavy sleet showers
        15.0f -> R.drawable.ic_light_snow_showers        // Light snow showers
        16.0f -> R.drawable.ic_moderate_snow_showers     // Moderate snow showers
        17.0f -> R.drawable.ic_heavy_snow_showers        // Heavy snow showers
        18.0f -> R.drawable.ic_light_rain                // Light rain
        19.0f -> R.drawable.ic_moderate_rain             // Moderate rain
        20.0f -> R.drawable.ic_heavy_rain                // Heavy rain
        21.0f -> R.drawable.ic_thunder                   // Thunder
        22.0f -> R.drawable.ic_light_sleet               // Light sleet
        23.0f -> R.drawable.ic_moderate_sleet            // Moderate sleet
        24.0f -> R.drawable.ic_heavy_sleet               // Heavy sleet
        25.0f -> R.drawable.ic_light_snowfall            // Light snowfall
        26.0f -> R.drawable.ic_moderate_snowfall         // Moderate snowfall
        27.0f -> R.drawable.ic_heavy_snowfall            // Heavy snowfall
        else -> R.drawable.ic_unknown                   // Default icon for unrecognized values
    }
}