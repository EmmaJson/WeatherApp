package com.emmajson.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)

    // LiveData for weather data
    private val _weatherData = MutableLiveData<List<TimeSeries>>()
    val weatherData: LiveData<List<TimeSeries>> = _weatherData

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Fetches weather data for a given latitude and longitude.
     */
    fun fetchWeather(lon: Double, lat: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = weatherRepository.getWeatherData(lon, lat)
                if (data.isNotEmpty()) {
                    _weatherData.postValue(data)
                } else {
                    _errorMessage.postValue("No weather data found.")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to fetch weather data: ${e.message}")
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
