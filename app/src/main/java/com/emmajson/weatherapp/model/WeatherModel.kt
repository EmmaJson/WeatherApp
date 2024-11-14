package com.emmajson.weatherapp.model

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.data.RetrofitClient
import com.emmajson.weatherapp.data.WeatherRepository
import com.emmajson.weatherapp.data.WeatherRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherModel(private val application: Application) {
    private val repository = WeatherRepository(service = RetrofitClient.weatherService)
    private val _weatherData = MutableLiveData<List<WeatherRetriever.TimeSeries>>()
    val weatherData: LiveData<List<WeatherRetriever.TimeSeries>> = _weatherData

    init {
        fetchWeatherData()
    }

    // Exampel code for threading
    fun threadTest() {
        val backgroundJob = GlobalScope.launch(Dispatchers.Default) {
            // Code to be executed on the background thread
            repeat(5) {
                println("Task on background thread: $it")
            }

            // Exampel code for threading
            fun threadTest() {
                val backgroundJob = GlobalScope.launch(Dispatchers.Default) {
                    // Code to be executed on the background thread
                    repeat(5) {
                        println("Task on background thread: $it")
                    }
                    fetchWeatherData()
                    WeatherRetriever(application)

                    // When your task is complete and you want to update the UI or perform other tasks on the main thread, use Dispatchers.Main
                    withContext(Dispatchers.Main) {
                        println("Switching to the main thread to update UI or perform other tasks.")
                    }
                }
                // Continue executing tasks on the main thread
                repeat(5) {
                    println("Task on the main thread: $it")
                }
            }

            // When your task is complete and you want to update the UI or perform other tasks on the main thread, use Dispatchers.Main
            withContext(Dispatchers.Main) {
                println("Switching to the main thread to update UI or perform other tasks.")
            }
        }

        // Continue executing tasks on the main thread
        repeat(5) {
            println("Task on the main thread: $it")
        }
    }


    // Function to check if the network is available
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        val isAvailable = capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))

        Log.d("WeatherModel", if (isAvailable) "Network is available" else "No network connection")
        return isAvailable
    }

    // Fetch weather data using a coroutine
    private fun fetchWeatherData() {
        if (!isNetworkAvailable()) {
            Log.e("WeatherModel", "No internet connection")
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getWeatherData("lon/14.333/lat/60.383")
                Log.d("WeatherModel", "API Response: $response")

                if (!response.timeSeries.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        _weatherData.value = response.timeSeries
                    }
                } else {
                    Log.e("WeatherModel", "No data available in the response")
                }
            } catch (e: Exception) {
                Log.e("WeatherModel", "Exception occurred: ${e.message}", e)
            }
        }
    }
}
