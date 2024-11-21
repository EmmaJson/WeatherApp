package com.emmajson.weatherapp.repository

import CityDb
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.emmajson.weatherapp.ui.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val context: Context) {
    private val cityDb = CityDb(context)

    // Toggle the favorite status of a city
    suspend fun toggleFavorite(cityName: String) {
        withContext(Dispatchers.IO) {
            val db = cityDb.writableDatabase
            val cursor = db.rawQuery("SELECT isFavorite FROM cities WHERE name = ?", arrayOf(cityName))
            if (cursor.moveToFirst()) {
                val isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite")) == 1
                val newIsFavorite = if (isFavorite) 0 else 1
                val values = ContentValues().apply {
                    put("isFavorite", newIsFavorite)
                }
                db.update("CityTable", values, "name = ?", arrayOf(cityName))
            }
            cursor.close()
        }
    }

    // Add or update a city in the database
    suspend fun addOrUpdateCity(city: City, isFavorite: Boolean = false, isLatest: Boolean = false) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("name", city.name)
                put("lat", city.latitude)
                put("lon", city.longitude)
                put("isFavorite", if (isFavorite) 1 else 0)
                put("isLatest", if (isLatest) 1 else 0)
            }
            cityDb.writableDatabase.insertWithOnConflict(
                "CityTable",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    // Fetch all cities
    suspend fun getAllCities(): List<City> = withContext(Dispatchers.IO) {
        val cities = mutableListOf<City>()
        val cursor = cityDb.readableDatabase.rawQuery("SELECT * FROM cities", null)
        cursor.use {
            while (it.moveToNext()) {
                cities.add(
                    City(
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        latitude = it.getDouble(it.getColumnIndexOrThrow("lat")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("lon"))
                    )
                )
            }
        }
        cities
    }

    // Fetch favorite cities
    suspend fun getFavoriteCities(): List<City> = withContext(Dispatchers.IO) {
        val cities = mutableListOf<City>()
        val cursor = cityDb.readableDatabase.rawQuery("SELECT * FROM cities WHERE isFavorite = 1", null)
        cursor.use {
            while (it.moveToNext()) {
                cities.add(
                    City(
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        latitude = it.getDouble(it.getColumnIndexOrThrow("lat")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("lon"))
                    )
                )
            }
        }
        cities
    }

    // Fetch search history (last 10 searched cities)
    suspend fun getSearchHistory(): List<City> = withContext(Dispatchers.IO) {
        val cities = mutableListOf<City>()
        val cursor = cityDb.readableDatabase.rawQuery("SELECT * FROM cities ORDER BY isLatest DESC LIMIT 10", null)
        cursor.use {
            while (it.moveToNext()) {
                cities.add(
                    City(
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        latitude = it.getDouble(it.getColumnIndexOrThrow("lat")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("lon"))
                    )
                )
            }
        }
        cities
    }


    // Delete a city by name
    suspend fun deleteCity(cityName: String) {
        withContext(Dispatchers.IO) {
            cityDb.writableDatabase.delete("cities", "name = ?", arrayOf(cityName))
        }
    }
}
