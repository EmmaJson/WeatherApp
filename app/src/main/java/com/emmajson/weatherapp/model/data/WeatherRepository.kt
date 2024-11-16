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
            fetchFromNetwork(lon, lat)
        } else {
            fetchFromDatabase()
        }
    }

    /**
     * Fetches weather data using Retrofit.
     */
    private suspend fun fetchFromNetwork(lon: Double, lat: Double): List<TimeSeries> {
        val lonLat = "lon/$lon/lat/$lat"
        val call = weatherService.getWeather(lonLat)

        return try {
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                // Save data to the database for offline access
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
