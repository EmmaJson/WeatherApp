package com.emmajson.weatherapp.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emmajson.weatherapp.data.RetrofitClient
import com.emmajson.weatherapp.data.WeatherRepository
import com.emmajson.weatherapp.data.WeatherRetriever
import com.emmajson.weatherapp.data.WeatherService
import com.emmajson.weatherapp.model.WeatherModel
import kotlinx.coroutines.launch


// Define an interface for the ViewModel
interface WeatherVM {
    val weatherData: LiveData<List<WeatherRetriever.TimeSeries>>
}


class WeatherViewModel(application: Application) : ViewModel(), WeatherVM {
    private val weatherModel = WeatherModel(application)

    // Expose the weather data to the UI
    override val weatherData: LiveData<List<WeatherRetriever.TimeSeries>> = weatherModel.weatherData
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


class FakeWeatherViewModel : ViewModel(), WeatherVM {
    private val _weatherData = MutableLiveData<List<WeatherRetriever.TimeSeries>>().apply {
        value = generateMockData()
    }
    override val weatherData: LiveData<List<WeatherRetriever.TimeSeries>> = _weatherData

    // Generate mock data for the next 7 days
    private fun generateMockData(): List<WeatherRetriever.TimeSeries> {
        return (0 until 7).map { day ->
            WeatherRetriever.TimeSeries(
                validTime = "2021-11-${String.format("%02d", 3 + day)}T12:00:00Z",
                parameters = listOf(
                    WeatherRetriever.Parameter(
                        name = "t",
                        levelType = "hl",
                        level = 2,
                        unit = "Cel",
                        values = listOf(10.0 + day) // Increment temperature for each day
                    ),
                    WeatherRetriever.Parameter(
                        name = "Wsymb2",
                        levelType = "hl",
                        level = 0,
                        unit = "",
                        values = listOf((1..27).random().toDouble()) // Random weather symbol
                    ),
                    WeatherRetriever.Parameter(
                        name = "msl",
                        levelType = "hmsl",
                        level = 0,
                        unit = "hPa",
                        values = listOf(1000.0 + day)
                    ),
                    WeatherRetriever.Parameter(
                        name = "vis",
                        levelType = "hl",
                        level = 0,
                        unit = "km",
                        values = listOf(10.0)
                    )
                )
            )
        }
    }
}

