package com.emmajson.weatherapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emmajson.weatherapp.ui.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel,
    onCitySelected: (String) -> Unit
) {
    val searchText by searchViewModel.searchText.collectAsState()
    val filteredCities by searchViewModel.cities.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val favoriteCities by searchViewModel.favoriteCities.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            TextField(
                value = searchText,
                onValueChange = { searchViewModel.onSearchTextChange(it) },
                label = { Text("Enter city name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onCitySelected(searchText) // Pass the text directly for free-text searches
                    navController.popBackStack() // Navigate back to WeatherScreen
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display recommendations if available
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredCities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onCitySelected(city.name) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = city.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = { searchViewModel.toggleFavorite(city.name) }) {
                                Icon(
                                    imageVector = if (favoriteCities.contains(city.name)) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = "Toggle Favorite"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
