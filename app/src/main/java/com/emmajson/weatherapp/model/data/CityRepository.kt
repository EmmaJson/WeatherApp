package com.emmajson.weatherapp.repository

import City
import CityDb
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val context: Context) {
    private val cityDb = CityDb(context)

    fun toggleFavorite(cityName: String) {
        println("CityRepository.toggleFavorite: Toggling favorite for city $cityName")
        cityDb.toggleFavoriteCity(cityName)
    }

    suspend fun getFavoriteCities(): List<City> = withContext(Dispatchers.IO) {
        println("CityRepository.getFavoriteCities: Fetching favorite cities")
        cityDb.getFavoriteCities().also {
            println("CityRepository.getFavoriteCities: Found ${it.size} favorites")
        }
    }

    // Fetch search history from the database
    suspend fun getSearchHistory(): List<City> = withContext(Dispatchers.IO) {
        println("CityRepository.getAll: Fetching all cities")
        cityDb.getAll().also {
            println("CityRepository.get all: Found ${it.size} ")
        }
    }

    fun deleteNonFavoriteCities() {
        println("CityRepository.deleteNonFavoriteCities: Deleting all non-favorites...")
        cityDb.deleteAllExceptFavorites()
        val remainingCities = cityDb.getFavoriteCities() // Add a method to fetch all cities
        println("CityRepository.deleteNonFavoriteCities: Remaining cities: $remainingCities")
    }
}
