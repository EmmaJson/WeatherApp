package com.emmajson.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)

    val weatherData: LiveData<List<TimeSeries>> = weatherRepository.weatherData

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedDayForecast = MutableStateFlow<TimeSeries?>(null)
    val selectedDayForecast: StateFlow<TimeSeries?> = _selectedDayForecast

    val searchedCity = weatherRepository.searchedCity

    fun setSearchedCity(city:String) {
        weatherRepository.setSearchedCity(city)
    }

    fun setSelectedDayForecast(dayIndex: Int) {
        val selectedForecast = weatherData.value?.getOrNull(dayIndex)
        _selectedDayForecast.value = selectedForecast
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
