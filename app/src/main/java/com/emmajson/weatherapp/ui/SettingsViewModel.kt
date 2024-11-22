package com.emmajson.weatherapp.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.model.data.SettingsRepository
import com.emmajson.weatherapp.model.network.TimeSeries
import com.emmajson.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _refreshRate = MutableStateFlow(10) // Default
    val refreshRate: StateFlow<Int> = _refreshRate.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false) // Default
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _forecastDays = MutableStateFlow(7) // Default to 7 days
    val forecastDays: StateFlow<Int> = _forecastDays.asStateFlow()

    val searchedCity = weatherRepository.searchedCity

    fun updateRefreshRate(rate: Int) {
        viewModelScope.launch {
            _refreshRate.value = rate // Immediate UI update
            settingsRepository.setRefreshRate(rate)
        }
    }
    init {
        viewModelScope.launch {
            settingsRepository.refreshRate.collect {
                println("Retrieved refresh rate: $it")
                _refreshRate.value = it
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _darkModeEnabled.value = enabled // Immediate UI update
            println("Updating dark mode: $enabled")
            settingsRepository.setDarkMode(enabled) // Save to DataStore
        }
    }
    init {
        viewModelScope.launch {
            settingsRepository.darkModeEnabled.collect { isDarkMode ->
                println("Retrieved dark mode: $isDarkMode")
                _darkModeEnabled.value = isDarkMode
            }
        }
    }

    fun updateForecastDays(days: Int) {
        viewModelScope.launch {
            _forecastDays.value = days
            settingsRepository.setForecastDays(days)
        }
    }
    init {
        viewModelScope.launch {
            settingsRepository.forecastDays.collect { days ->
                println("Retrieved forecast days: $days")
                _forecastDays.value = days
            }
        }
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
