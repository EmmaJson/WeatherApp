package com.emmajson.weatherapp

import com.emmajson.weatherapp.vm.SearchViewModel
import com.emmajson.weatherapp.vm.SearchViewModelFactory
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
import com.emmajson.weatherapp.vm.SettingsViewModel
import com.emmajson.weatherapp.vm.SettingsViewModelFactory
import com.emmajson.weatherapp.ui.theme.WeatherAppTheme
import com.emmajson.weatherapp.vm.WeatherViewModel
import com.emmajson.weatherapp.vm.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: WeatherViewModel by viewModels {
            WeatherViewModelFactory(application)
        }
        val searchViewModel: SearchViewModel by viewModels {
            SearchViewModelFactory(application)
        }
        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModelFactory(application)
        }

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
