package com.emmajson.weatherapp.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.emmajson.weatherapp.model.database.WeatherDatabaseHelper
import com.emmajson.weatherapp.model.network.RetrofitClient.weatherService
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.model.network.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.emmajson.weatherapp.network.WeatherRetriever

class WeatherRepository(private val context: Context) {

    private val weatherRetriever = WeatherRetriever()
    private val databaseHelper = WeatherDatabaseHelper(context)

    /**
     * Fetches weather data from the network or database.
     * Returns a List<TimeSeries> or an empty list if something goes wrong.
     */
    suspend fun getWeatherData(lat: Double, lon: Double): List<TimeSeries> {
        return try {
            // Fetch data from the network
            val networkData = weatherRetriever.getWeather(lat, lon)
            if (networkData.isNotEmpty()) {
                // Save to the database for offline access
                databaseHelper.insertWeatherData(networkData)
                networkData
            } else {
                // Fallback to the database if network data is empty
                databaseHelper.getAllWeatherData()
            }
        } catch (e: Exception) {
            // Fallback to the database if network request fails
            databaseHelper.getAllWeatherData()
        }
    }

    fun fetchFromNetwork(lat: Double, lon: Double, callback: (List<TimeSeries>?) -> Unit) {
        val call = weatherService.getWeather(lat, lon)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        // Save to database and pass data to callback
                        databaseHelper.insertWeatherData(weatherResponse.timeSeries)
                        callback(weatherResponse.timeSeries)
                    } else {
                        Log.e("WeatherRepository", "Response body is null")
                        callback(null)
                    }
                } else {
                    Log.e("WeatherRepository", "Error: ${response.code()}")
                    fetchFromDatabase(callback)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("WeatherRepository", "Network error: ${t.message}")
                fetchFromDatabase(callback)
            }
        })
    }

    private fun fetchFromDatabase(callback: (List<TimeSeries>?) -> Unit) {
        val data = databaseHelper.getAllWeatherData()
        if (data.isNotEmpty()) {
            callback(data)
        } else {
            callback(null)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }
}
