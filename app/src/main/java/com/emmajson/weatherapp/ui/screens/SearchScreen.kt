package com.emmajson.weatherapp.ui.screens

import City
import SearchViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.input.ImeAction


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel,
    onCitySelected: (String) -> Unit
) {
    println("SearchScreen: Composing UI with new favoriteCities state")

    val searchText by searchViewModel.searchText.observeAsState("")
    val favoriteCities by searchViewModel.favoriteCities.observeAsState(emptyList())
    val searchHistory by searchViewModel.filteredSearchHistory.collectAsState(emptyList())
    val isSearching by searchViewModel.isSearching.observeAsState(false)

    val coroutineScope = rememberCoroutineScope()


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
            // Clear Cache Button
            Button(
                onClick = {
                    println("SearchScreen: Clear Cache button clicked")
                    coroutineScope.launch {
                        searchViewModel.clearCache()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Clear Cache")
            }

            Spacer(modifier = Modifier.height(16.dp))
// Search bar
            TextField(
                value = searchText,
                onValueChange = { searchViewModel.onSearchTextChange(it) },
                label = { Text("Enter city name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { event ->
                        if (event.key == Key.Enter) {
                            onCitySelected(searchText.trim()) // Trigger the search or selection
                            searchViewModel.addCityToSearchHistory(City(name=searchText))
                            navController.popBackStack() // Navigate back to the previous screen
                            true // Indicate the event was handled
                        } else {
                            false // Let the system handle other key events
                        }
                    }
                    .height(56.dp), // Fixed height for the TextField
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done // Set the action key to "Done" (or "Search")
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onCitySelected(searchText) // Trigger the search or selection
                        searchViewModel.addCityToSearchHistory(City(name=searchText))
                        navController.popBackStack() // Navigate back to the previous screen
                    }
                )
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Display recommendations if available
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {

                // Display Favorite Cities
                if (favoriteCities.isNotEmpty()) {
                    Text(
                        text = "Favorites")
                    LazyColumn {
                        items(favoriteCities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        onCitySelected(city.name)
                                        navController.popBackStack()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        searchViewModel.toggleFavorite(city)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (favoriteCities.contains(city)) {
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

                Spacer(modifier = Modifier.height(16.dp))

                if (searchHistory.isNotEmpty()) {
                    Text(
                        text = "Search History"
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchHistory) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        onCitySelected(city.name)
                                        navController.popBackStack()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = {
                                    println("SearchScreen: Heart icon clicked for ${city.name}")
                                    coroutineScope.launch {
                                        searchViewModel.toggleFavorite(city)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (favoriteCities.contains(city)) {
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
}
