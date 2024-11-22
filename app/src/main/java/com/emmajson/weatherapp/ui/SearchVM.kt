import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emmajson.weatherapp.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toSet
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

    // Computed flow to filter favorite cities based on search text
    val filteredFavoriteCities = _searchText.flatMapLatest { query ->
        flow {
            val filteredList = if (query.isEmpty()) {
                _favoriteCities.value
            } else {
                _favoriteCities.value.filter { it.name.contains(query, ignoreCase = true) }
            }
            emit(filteredList)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Computed flow to filter search history based on search text
    val filteredSearchHistory = _searchText.flatMapLatest { query ->
        flow {
            val filteredList = if (query.isEmpty()) {
                _searchHistory.value
            } else {
                _searchHistory.value.filter { it.name.contains(query, ignoreCase = true) }
            }
            emit(filteredList)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
            cityRepository.addCityIfNotExists(city)
            // Log the updated history
            Log.d("history", "Added ${city.name} to history. Updated history: ${_searchHistory.value}")
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

            // Delete non-favorite cities from the database
            cityRepository.deleteNonFavoriteCities()

            // Reload the favorites list
            val refreshedFavorites = cityRepository.getFavoriteCities()
            _favoriteCities.value = refreshedFavorites // Update StateFlow
            println("SearchViewModel.clearCache: Updated favoriteCities: $refreshedFavorites")
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
