package com.emmajson.weatherapp.vm

import City
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData

class SearchViewModel(application: Application) : ViewModel() {
    val cityRepository = CityRepository(application)

    private val _searchText = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _favoriteCities = MutableStateFlow<List<City>>(emptyList())
    private val _searchHistory = MutableStateFlow<List<City>>(emptyList())

    val searchText = _searchText.asLiveData()
    val favoriteCities = _favoriteCities.asLiveData()
    val searchHistory = _searchHistory.asLiveData()
    val isSearching = _isSearching.asLiveData()

    init {
        loadDataFromRepository()
    }

    private fun loadDataFromRepository() {
        viewModelScope.launch {
            println("SearchViewModel.loadDataFromRepository: Loading data")

            // Load favorite cities
            val favorites = cityRepository.getFavoriteCities()
            _favoriteCities.value = favorites

            // Load search history
            val history = cityRepository.getSearchHistory()
            _searchHistory.value = history

            println("SearchViewModel.loadDataFromRepository: Favorites loaded: $favorites")
        }
    }

    fun addCityToSearchHistory(city: City) {
        viewModelScope.launch {
            // Create a mutable set from the current search history to remove duplicates
            val updatedHistory = _searchHistory.value.toMutableSet()

            // Add the new city to the set
            updatedHistory.add(city)

            // Update the StateFlow with the new list
            _searchHistory.value = updatedHistory.toList()
            // Log the updated history
            Log.d("history", "Added ${city.name} to history. Updated history: ${_searchHistory.value}")
        }
    }

    fun updateSearchHistory() {
        viewModelScope.launch {
            val history = cityRepository.getSearchHistory()
            println("SearchViewModel.updateSearchHistory: Updated searchHistory: $history")
            _searchHistory.value = history.toList() // Ensure a new list is emitted
        }
    }

    // Update the search text
    fun onSearchTextChange(newText: String) {
        _searchText.value = newText.trim()
    }

    // Toggle favorite status
    fun toggleFavorite(city: City) {
        viewModelScope.launch {
            println("SearchViewModel.toggleFavorite: Toggling favorite for city ${city.name}")

            // Toggle favorite status in the repository
            cityRepository.toggleFavorite(city.name)

            // Reload the favorites list from the database
            val updatedFavorites = cityRepository.getFavoriteCities()

            // Force emission by assigning a new list
            _favoriteCities.value = updatedFavorites.toList()
            println("SearchViewModel.toggleFavorite: Updated favoriteCities: $updatedFavorites")
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            println("SearchViewModel.clearCache: Clearing non-favorite cities")

            // Delete non-favorite cities
            cityRepository.deleteNonFavoriteCities()

            // Reload favorites
            val refreshedFavorites = cityRepository.getFavoriteCities()
            println("SearchViewModel.clearCache: Refreshed favorites: $refreshedFavorites")

            // Emit a new list
            _favoriteCities.value = refreshedFavorites.toList()
            println("SearchViewModel.clearCache: Emitted new favoriteCities list")

            updateSearchHistory()
        }
    }
}

class SearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
