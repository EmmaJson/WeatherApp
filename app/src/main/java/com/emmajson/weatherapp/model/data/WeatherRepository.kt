package com.emmajson.weatherapp.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.emmajson.weatherapp.model.database.WeatherDatabaseHelper
import com.emmajson.weatherapp.model.network.RetrofitClient.weatherService
import com.emmajson.weatherapp.model.network.TimeSeries

class WeatherRepository(private val context: Context) {
    private val databaseHelper = WeatherDatabaseHelper(context)

    /**
     * Fetches weather data from the network or database.
     * Returns a List<TimeSeries> or an empty list if something goes wrong.
     */
    suspend fun getWeatherData(lon: Double, lat: Double): List<TimeSeries> {
        return if (isNetworkAvailable()) {
            try {
                fetchFromNetwork(lon, lat)
            } catch (e: Exception) {
                handleFallback(lon, lat, "Network error, falling back to cached data: ${e.message}")
            }
        } else {
            handleFallback(lon, lat, "No network connection. Loading cached data.")
        }
    }

    private suspend fun handleFallback(lon: Double, lat: Double, errorMessage: String): List<TimeSeries> {
        Log.w("WeatherRepository", errorMessage)
        return fetchFromDatabase().also {
            if (it.isEmpty()) {
                throw Exception("No cached data available.")
            }
        }
    }

    /**
     * Fetches weather data using Retrofit.
     */
    private suspend fun fetchFromNetwork(lon: Double, lat: Double): List<TimeSeries> {
        val call = weatherService.getWeather(lon, lat)
        Log.d("WeatherRepository", "Full URL: ${call.request().url()}")
        return try {
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                databaseHelper.insertWeatherData(weatherResponse.timeSeries)
                weatherResponse.timeSeries
            } else {
                Log.e("WeatherRepository", "API Error: ${response.code()}")
                fetchFromDatabase()
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Network Error: ${e.message}")
            fetchFromDatabase()
        }
    }

    /**
     * Fetches weather data from the local database.
     */
    private fun fetchFromDatabase(): List<TimeSeries> {
        val data = databaseHelper.getAllWeatherData()
        return if (data.isNotEmpty()) data else emptyList()
    }

    /**
     * Checks if the network is available.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }
}
