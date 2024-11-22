package com.emmajson.weatherapp.ui.screens

import City
import com.emmajson.weatherapp.vm.SearchViewModel
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel,
    onCitySelected: (String) -> Unit
) {
    val searchText by searchViewModel.searchText.observeAsState("")
    val favoriteCities by searchViewModel.favoriteCities.observeAsState(emptyList())
    val searchHistory by searchViewModel.searchHistory.observeAsState(emptyList())
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
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Clear Cache Button
            Button(
                onClick = {
                    coroutineScope.launch { searchViewModel.clearCache() }
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
                    .height(56.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onCitySelected(searchText.trim())
                        searchViewModel.addCityToSearchHistory(City(searchText, 0f, 0f))
                        searchViewModel.onSearchTextChange("")
                        navController.popBackStack()
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                if (favoriteCities.isNotEmpty()) {
                    Text("Favorites", style = MaterialTheme.typography.bodyLarge)
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                IconButton(onClick = {
                                    coroutineScope.launch { searchViewModel.toggleFavorite(city) }
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
                    Text("Search History", style = MaterialTheme.typography.bodyLarge)
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                IconButton(onClick = {
                                    coroutineScope.launch { searchViewModel.toggleFavorite(city) }
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
