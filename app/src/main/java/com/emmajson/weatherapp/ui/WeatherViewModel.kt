package com.emmajson.weatherapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.model.geoAPI.fetchCoordinates
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)

    // LiveData for weather data
    private val _weatherData = MutableLiveData<List<TimeSeries>>()
    val weatherData: LiveData<List<TimeSeries>> = _weatherData

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedDayForecast = MutableStateFlow<TimeSeries?>(null)
    val selectedDayForecast: StateFlow<TimeSeries?> = _selectedDayForecast

    // Searched data
    private val _searchedCity = MutableLiveData<String?>("Stockholm") // Default value
    val searchedCity: LiveData<String?> = _searchedCity

    // StateFlow for continuous updates
    private val _weatherFlow = MutableStateFlow<List<TimeSeries>?>(null)
    val weatherFlow: StateFlow<List<TimeSeries>?> = _weatherFlow

    private var isContinuousFetchingStarted = false

    private fun startContinuousFetching(lon: Double, lat: Double) {
        if (isContinuousFetchingStarted) return // Prevent multiple loops
        isContinuousFetchingStarted = true

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val data = weatherRepository.getWeatherData(lon, lat)
                    Log.d("WeatherViewModel", "Fetched weather data: $data")
                    _weatherData.postValue(data)

                    // Optional: Write to the database only if needed
                    // Uncomment below line if database caching is required
                    // weatherRepository.cacheWeatherData(data)
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", "Error fetching weather: ${e.message}")
                    _errorMessage.postValue("Error: ${e.message}")
                }
                delay(60000) // Update every minute
            }
        }
    }

    /**
     * Fetches weather data for a given latitude and longitude.
     */
    private fun fetchWeather(lon: Double, lat: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = weatherRepository.getWeatherData(lon, lat)
                Log.d("WeatherViewModel", "Fetched weather data: $data") // Debugging
                _weatherData.postValue(data)
                _errorMessage.postValue(null)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}")
                _errorMessage.postValue("Error: ${e.message}")
            }
        }
    }

    fun setSelectedDayForecast(dayIndex: Int) {
        val selectedForecast = _weatherData.value?.getOrNull(dayIndex)
        _selectedDayForecast.value = selectedForecast
    }

    fun searchAndUpdateWeather(cityName: String) {
        if (cityName.isBlank()) {
            _errorMessage.postValue("City name cannot be empty.")
            return
        }
        _searchedCity.postValue(cityName) // Update searchedCity before fetching data
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchCoordinates(
                    city = cityName,
                    onSuccess = { lat, lon ->
                        Log.d("WeatherViewModel", "Coordinates for $cityName: Latitude=$lat, Longitude=$lon")
                        startContinuousFetching(lon, lat)
                    },
                    onError = { error ->
                        Log.e("WeatherViewModel", "Error fetching coordinates: $error")
                        _errorMessage.postValue("Error fetching coordinates: $error")
                    }
                )
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to update weather: ${e.message}")
                _errorMessage.postValue("Failed to update weather: ${e.message}")
            }
        }
    }
}

class WeatherViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
