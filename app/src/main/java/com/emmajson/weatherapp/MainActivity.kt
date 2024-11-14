package com.emmajson.weatherapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.emmajson.weatherapp.data.WeatherRetriever
import com.emmajson.weatherapp.model.WeatherModel
import com.emmajson.weatherapp.ui.WeatherViewModel
import com.emmajson.weatherapp.ui.WeatherViewModelFactory
import com.emmajson.weatherapp.ui.screens.WeatherScreen
import com.emmajson.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //val viewModel: WeatherViewModel by viewModels()

        val application = applicationContext as Application
        val viewModelFactory = WeatherViewModelFactory(application)
        val viewModel: WeatherViewModel by viewModels { viewModelFactory }

        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    //val weatherModel = WeatherModel(application = application)
                    //weatherModel.threadTest()
                    //weatherModel.isNetworkAvailable()
                    // Start the WeatherScreen with the ViewModel
                    WeatherScreen(viewModel = viewModel)
                } }
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