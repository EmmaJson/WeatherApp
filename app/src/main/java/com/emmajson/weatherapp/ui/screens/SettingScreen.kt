package com.emmajson.weatherapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emmajson.weatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel, navController: NavController) {
    val refreshRate by viewModel.refreshRate.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    val forecastDays by viewModel.forecastDays.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(text = "Enable Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
            }

            Text(text = "Refresh Rate (in minutes):", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = refreshRate.toFloat(),
                onValueChange = { viewModel.updateRefreshRate(it.toInt()) },
                valueRange = 1f..15f,
                steps = 14
            )
            Text(text = "${refreshRate}", style = MaterialTheme.typography.bodyMedium)

            // Forecast Days Setting
            Text("Select Forecast Days", style = MaterialTheme.typography.bodyLarge)

            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { expanded = true }) {
                    Text("Forecast Days: $forecastDays")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(3, 5, 7, 10).forEach { days ->
                        DropdownMenuItem(
                            text = { Text("$days Days") },
                            onClick = {
                                expanded = false
                                viewModel.updateForecastDays(days)
                            }
                        )
                    }
                }
            }
        }
    }
}
