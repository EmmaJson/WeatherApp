package com.emmajson.weatherapp.repository

import City
import CityDb
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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

    // Add a city if it does not already exist, or update it if it does
    suspend fun addCityIfNotExists(city: City, isFavorite: Boolean = false) {
        withContext(Dispatchers.IO) {
            val db = cityDb.writableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM cities WHERE name = ?", arrayOf(city.name))

            if (cursor.count == 0) {
                // City does not exist, add it to the database
                val values = ContentValues().apply {
                    put("name", city.name)
                    put("isFavorite", if (isFavorite) 1 else 0)
                }
                db.insertWithOnConflict(
                    "cities",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } else {
                // City exists, update the information if needed
                val values = ContentValues().apply {
                    put("name", city.name)
                    put("isFavorite", if (isFavorite) 1 else 0)
                }
                db.update("cities", values, "name = ?", arrayOf(city.name))
            }
            cursor.close()
        }
    }

    // Fetch a city by name
    suspend fun getCityByName(cityName: String): City? {
        return withContext(Dispatchers.IO) {
            val db = cityDb.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM cities WHERE name = ?", arrayOf(cityName))
            var city: City? = null
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val latitude = cursor.getFloat(cursor.getColumnIndexOrThrow("lat"))
                val longitude = cursor.getFloat(cursor.getColumnIndexOrThrow("lon"))
                city = City(name)
            }
            cursor.close()
            city
        }
    }

    // Fetch search history from the database
    suspend fun getSearchHistory(): List<City> = withContext(Dispatchers.IO) {
        val cities = mutableListOf<City>()
        val cursor = cityDb.readableDatabase.rawQuery("SELECT * FROM cities", null)
        cursor.use {
            val nameIndex = it.getColumnIndexOrThrow("name")
            while (it.moveToNext()) {
                cities.add(City(name = it.getString(nameIndex)))
            }
        }
        cities
    }

    fun deleteNonFavoriteCities() {
        println("CityRepository.deleteNonFavoriteCities: Deleting all non-favorites...")
        cityDb.deleteAllExceptFavorites()
        val remainingCities = cityDb.getFavoriteCities() // Add a method to fetch all cities
        println("CityRepository.deleteNonFavoriteCities: Remaining cities: $remainingCities")
    }
}
