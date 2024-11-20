package com.emmajson.weatherapp.repository

import CityDb
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.emmajson.weatherapp.model.database.WeatherDatabaseHelper
import com.emmajson.weatherapp.model.geoAPI.fetchCoordinates
import com.emmajson.weatherapp.model.network.RetrofitClient.weatherService
import com.emmajson.weatherapp.model.network.TimeSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherRepository(private val context: Context) {
    private val databaseHelper = WeatherDatabaseHelper(context)

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Expose weather data to the ViewModel
    private val _weatherData = MutableLiveData<List<TimeSeries>>()
    val weatherData: LiveData<List<TimeSeries>> = _weatherData


    // Searched data
    private val _searchedCity = MutableLiveData<String?>("") // Default value
    val searchedCity: LiveData<String?> = _searchedCity
    // Update searched city and start new continuous fetching
    fun setSearchedCity(city: String) {
        if(city != searchedCity.value) {
            _searchedCity.value = city
            searchAndUpdateWeather(city)
            //TODO : relaunch
            Log.d("WeatherViewModel", "New City entered: $city")
        } else {
            Log.d("WeatherViewModel", "Same City entered: $city")
        }
    }

    /**
     * Fetches coordinates for a city and starts fetching weather data for it.
     */
     fun searchAndUpdateWeather(cityName: String) {
        if (cityName.isBlank()) {
            _errorMessage.postValue("City name cannot be empty.")
            return
        }
        // Fetch coordinates for the given city and start continuous fetching
        try {
            fetchCoordinates(
                city = cityName,
                onSuccess = { lon, lat ->
                    Log.d(
                        "WeatherViewModel",
                        "Coordinates for $cityName: Latitude=$lat, Longitude=$lon"
                    )
                    // Launch a coroutine to call the suspend function
                    repositoryScope.launch(Dispatchers.IO) {
                        val data = getWeatherData(lon = lon, lat = lat)
                        _weatherData.postValue(data) // Update the LiveData with weather data
                        // Optionally handle the fetched data here or pass it to LiveData
                        Log.d("WeatherViewModel", "Fetched weather data: $weatherData")
                    }
                },
                onError = { error ->
                    Log.e("WeatherViewModel", "Error fetching coordinates: $error")
                    _errorMessage.postValue("Error fetching coordinates: $error")
                })
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to update weather: ${e.message}")
            _errorMessage.postValue("Failed to update weather: ${e.message}")
        }
    }

    /**
     * Fetches weather data from the network or database.
     * Returns a List<TimeSeries> or an empty list if something goes wrong.
     */
    suspend fun getWeatherData(lon: Double, lat: Double): List<TimeSeries> {
        return if (isNetworkAvailable()) {
            try {
                fetchFromNetwork(lon = lon, lat = lat)
            } catch (e: Exception) {
                handleFallback(lon = lon, lat = lat, "Network error, falling back to cached data: ${e.message}")
            }
        } else {
            handleFallback(lon = lon, lat = lat, "No network connection. Loading cached data.")
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
