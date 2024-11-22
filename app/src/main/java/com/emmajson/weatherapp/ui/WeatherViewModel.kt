package com.emmajson.weatherapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)
    private val settingsRepository = SettingsRepository(application)

    val weatherData: LiveData<List<TimeSeries>> = weatherRepository.weatherData
    val isDataFromCache: LiveData<Boolean> = weatherRepository.isDataFromCache

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedDayForecast = MutableStateFlow<TimeSeries?>(null)
    val selectedDayForecast: StateFlow<TimeSeries?> = _selectedDayForecast

    private val _refreshRate = MutableStateFlow(10) // Default
    val refreshRate: StateFlow<Int> = _refreshRate.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false) // Default
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()


    val searchedCity = weatherRepository.searchedCity

    init {
        startPeriodicWeatherUpdates()
    }

    // Function to fetch weather data every 10 minutes
    private fun startPeriodicWeatherUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    // Fetch data from repository
                    weatherRepository.searchAndUpdateWeather(cityName = searchedCity.value.toString())
                } catch (e: Exception) {
                    e.printStackTrace() // Log errors
                }
                delay(60*1000 * refreshRate.value.toLong()) // Wait 1 * refrate minutes before next fetch
            }
        }
    }

    fun setSearchedCity(city:String) {
        weatherRepository.setSearchedCity(city)
    }

    fun setSelectedDayForecast(dayIndex: Int) {
        val selectedForecast = weatherData.value?.getOrNull(dayIndex)
        _selectedDayForecast.value = selectedForecast
    }

    @SuppressLint("NewApi")
    fun interpolateHourlyData(hourlyForecasts: List<TimeSeries>, selectedDayDate: String): List<TimeSeries> {
        val filledForecasts = mutableListOf<TimeSeries>()
        var previousForecast: TimeSeries? = null

        // Sort forecasts to ensure chronological order
        val sortedForecasts = hourlyForecasts.sortedBy { it.validTime }

        // Loop through all hours from 00:00 to 24:00 (inclusive)
        for (hour in 0..23) {
            val targetHour = if (hour == 24) "24:00:00Z" else "%02d:00:00Z".format(hour)
            val targetValidTime = if (hour == 24) {
                // Handle 24:00 as 00:00 of the next day
                val nextDay = java.time.LocalDate.parse(selectedDayDate).plusDays(1).toString()
                "${nextDay}T$targetHour"
            } else {
                "${selectedDayDate}T$targetHour"
            }

            // Find a matching forecast for the current hour
            val matchingForecast = sortedForecasts.find { it.validTime == targetValidTime }

            if (matchingForecast != null) {
                // Interpolate for gaps between the previous and the current forecast
                if (previousForecast != null && matchingForecast.getHour() - previousForecast.getHour() > 1) {
                    val interpolatedPoints = interpolateBetweenPoints(previousForecast, matchingForecast)
                    interpolatedPoints.forEach { point ->
                        if (!filledForecasts.any { it.validTime == point.validTime }) {
                            filledForecasts.add(point)
                        }
                    }
                }

                // Add the current forecast to the list
                filledForecasts.add(matchingForecast)
                previousForecast = matchingForecast
            } else if (previousForecast != null) {
                // No matching forecast: Extrapolate if we have a previous forecast
                val extrapolatedPoint = previousForecast.copy(validTime = targetValidTime)
                if (!filledForecasts.any { it.validTime == extrapolatedPoint.validTime }) {
                    filledForecasts.add(extrapolatedPoint)
                }
            }
        }

        // Sort the list again to ensure proper ordering
        return filledForecasts.sortedBy { it.validTime }
    }

    private fun interpolateBetweenPoints(start: TimeSeries, end: TimeSeries): List<TimeSeries> {
        val interpolatedPoints = mutableListOf<TimeSeries>()
        val startHour = start.getHour()
        val endHour = end.getHour()

        // Debug: Validate start and end hours
        println("Start hour: $startHour, End hour: $endHour")

        if (startHour >= endHour) {
            println("Invalid time range: startHour ($startHour) >= endHour ($endHour)")
            return emptyList()
        }

        for (hour in (startHour + 1) until endHour) {
            val fraction = (hour - startHour).toFloat() / (endHour - startHour).toFloat()
            println("Interpolating for hour: $hour, fraction: $fraction")

            val interpolatedParameters = start.parameters.map { parameter ->
                val endParameter = end.parameters.find { it.name == parameter.name }
                if (parameter.name == "t" && endParameter != null) {
                    val startValue = parameter.values.firstOrNull()?.toFloat() ?: 0f
                    val endValue = endParameter.values.firstOrNull()?.toFloat() ?: 0f
                    val interpolatedValue = startValue + fraction * (endValue - startValue)
                    println("Parameter ${parameter.name}: startValue=$startValue, endValue=$endValue, interpolatedValue=$interpolatedValue")
                    parameter.copy(values = listOf(interpolatedValue))
                } else {
                    parameter
                }
            }

            val interpolatedValidTime = "${start.validTime.substring(0, 11)}${"%02d:00:00Z".format(hour)}"
            println("Interpolated validTime for hour $hour: $interpolatedValidTime")

            interpolatedPoints.add(
                start.copy(validTime = interpolatedValidTime, parameters = interpolatedParameters)
            )
        }

        // Debug: Log final interpolated points
        interpolatedPoints.forEach {
            println("Interpolated point: validTime=${it.validTime}")
        }

        return interpolatedPoints
    }

    private fun TimeSeries.getHour(): Int {
        return validTime.substring(11, 13).toInt()
    }

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

    private val _forecastDays = MutableStateFlow(7) // Default to 7 days
    val forecastDays: StateFlow<Int> = _forecastDays.asStateFlow()
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

class WeatherViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
