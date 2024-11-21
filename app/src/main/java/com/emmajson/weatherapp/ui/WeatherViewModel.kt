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

    fun interpolateHourlyData(hourlyForecasts: List<TimeSeries>, selectedDayDate: String): List<TimeSeries> {
        val filledForecasts = mutableListOf<TimeSeries>()
        var previousForecast: TimeSeries? = null

        for (hour in 0 until 24) {
            val targetHour = "%02d:00:00Z".format(hour)
            val targetValidTime = "${selectedDayDate}T$targetHour"

            val matchingForecast = hourlyForecasts.find { it.validTime == targetValidTime }

            if (matchingForecast != null) {
                filledForecasts.add(matchingForecast)
                if (previousForecast != null && hour - previousForecast.getHour() > 1) {
                    filledForecasts.addAll(
                        interpolateBetweenPoints(previousForecast, matchingForecast)
                    )
                }
                previousForecast = matchingForecast
            }
        }
        return filledForecasts
    }

    private fun interpolateBetweenPoints(start: TimeSeries, end: TimeSeries): List<TimeSeries> {
        val interpolatedPoints = mutableListOf<TimeSeries>()
        val startHour = start.getHour()
        val endHour = end.getHour()

        for (hour in (startHour + 1) until endHour) {
            val fraction = (hour - startHour).toFloat() / (endHour - startHour).toFloat()

            val interpolatedParameters = start.parameters.map { parameter ->
                val endParameter = end.parameters.find { it.name == parameter.name }
                if (parameter.name == "t" && endParameter != null) {
                    val startValue = parameter.values.firstOrNull() ?: 0.0
                    val endValue = endParameter.values.firstOrNull() ?: 0.0
                    val interpolatedValue = startValue + fraction * (endValue - startValue)
                    parameter.copy(values = listOf(interpolatedValue))
                } else {
                    parameter
                }
            }

            val interpolatedValidTime = "${start.validTime.substring(0, 11)}${"%02d:00:00Z".format(hour)}"
            interpolatedPoints.add(start.copy(validTime = interpolatedValidTime, parameters = interpolatedParameters))
        }
        return interpolatedPoints
    }

    private fun TimeSeries.getHour(): Int {
        return validTime.substring(11, 13).toInt()
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
