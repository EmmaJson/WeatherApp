package com.emmajson.weatherapp.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson

class WeatherRepository(private val service: WeatherService) {
    suspend fun getWeatherData(lonLat: String): WeatherRetriever.WeatherResponse {
        return try {
            service.getWeatherForecast(lonLat)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather data", e)
            throw e
        }
    }
}
