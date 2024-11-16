package com.emmajson.weatherapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.emmajson.weatherapp.model.navigation.Navigation
import com.emmajson.weatherapp.ui.screens.WeatherScreen
import com.emmajson.weatherapp.ui.theme.WeatherAppTheme
import com.emmajson.weatherapp.viewmodel.WeatherViewModel
import com.emmajson.weatherapp.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        //val viewModel: WeatherViewModel by viewModels()

        val viewModel: WeatherViewModel by viewModels {
            WeatherViewModelFactory(application)
        }

        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    //val weatherModel = WeatherModel(application = application)
                    //weatherModel.threadTest()
                    //weatherModel.isNetworkAvailable()
                    // Start the WeatherScreen with the ViewModel
                    Navigation(vm = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherAppTheme {
        Greeting("Android")
    }
}