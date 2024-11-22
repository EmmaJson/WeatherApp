package com.emmajson.weatherapp

import SearchViewModel
import SearchViewModelFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.emmajson.weatherapp.model.navigation.Navigation
import com.emmajson.weatherapp.ui.SettingsViewModel
import com.emmajson.weatherapp.ui.SettingsViewModelFactory
import com.emmajson.weatherapp.ui.theme.WeatherAppTheme
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import com.emmajson.weatherapp.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the ViewModel instance
        val viewModel: WeatherViewModel by viewModels {
            WeatherViewModelFactory(application)
        }

        // Obtain the SearchViewModel instance
        val searchViewModel: SearchViewModel by viewModels {
            SearchViewModelFactory(application)
        }

        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModelFactory(application)
        }

        // Set up UI with Compose, observing ViewModel state
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(vm = viewModel, svm=searchViewModel, settingsVm = settingsViewModel)
                }
            }
        }
    }
}
