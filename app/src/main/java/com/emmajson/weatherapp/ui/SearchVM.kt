package com.emmajson.weatherapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.repository.CityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : ViewModel() {
    private val cityRepository = CityRepository(application)

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _favoriteCities = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCities: StateFlow<Set<String>> = _favoriteCities

    private val _searchHistory = MutableStateFlow<List<City>>(emptyList())
    val searchHistory: StateFlow<List<City>> = _searchHistory

    private val _allCities = MutableStateFlow<List<City>>(emptyList())

    init {
        loadCities()
    }

    private fun loadCities() {
        viewModelScope.launch {
            _allCities.value = cityRepository.getAllCities()
            _favoriteCities.value = cityRepository.getFavoriteCities().map { it.name }.toSet()
        }
    }

    // Combine search text, favorites, and all cities to dynamically filter results
    val displayCities = combine(searchText, _allCities, favoriteCities) { text, allCities, favorites ->
        val filteredCities = allCities.filter { it.name.contains(text, ignoreCase = true) }
        val favoriteList = filteredCities.filter { favorites.contains(it.name) }
        val historyList = filteredCities.filterNot { favorites.contains(it.name) }
        favoriteList + historyList
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    suspend fun toggleFavorite(cityName: String) {
        cityRepository.toggleFavorite(cityName)
        loadCities()
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            _searchHistory.value = cityRepository.getSearchHistory()
        }
    }
    fun addToSearchHistory(city: City) {
        viewModelScope.launch {
            cityRepository.addOrUpdateCity(city, isLatest = true)
            loadCities()
        }
    }
}

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double
)
class SearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}