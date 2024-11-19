package com.emmajson.weatherapp.model.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.emmajson.weatherapp.model.network.Parameter
import com.emmajson.weatherapp.model.network.TimeSeries

class WeatherDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "weather.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_WEATHER = "weather"
        private const val COLUMN_VALID_TIME = "validTime"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LEVEL_TYPE = "levelType"
        private const val COLUMN_LEVEL = "level"
        private const val COLUMN_UNIT = "unit"
        private const val COLUMN_VALUE = "value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("WeatherDatabaseHelper", "Creating database table")
        val createTableQuery = """
            CREATE TABLE $TABLE_WEATHER (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VALID_TIME TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_LEVEL_TYPE TEXT,
                $COLUMN_LEVEL INTEGER,
                $COLUMN_UNIT TEXT,
                $COLUMN_VALUE REAL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("WeatherDatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WEATHER")
        onCreate(db)
    }

    fun insertWeatherData(data: List<TimeSeries>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            Log.d("WeatherDatabaseHelper", "Inserting weather data into database")
            db.delete(TABLE_WEATHER, null, null)

            for (timeSeries in data) {
                for (param in timeSeries.parameters) {
                    for (value in param.values) {
                        val contentValues = ContentValues().apply {
                            put(COLUMN_VALID_TIME, timeSeries.validTime)
                            put(COLUMN_NAME, param.name)
                            put(COLUMN_LEVEL_TYPE, param.levelType)
                            put(COLUMN_LEVEL, param.level)
                            put(COLUMN_UNIT, param.unit)
                            put(COLUMN_VALUE, value)
                        }
                        val result = db.insert(TABLE_WEATHER, null, contentValues)
                        if (result == -1L) {
                            Log.e("WeatherDatabaseHelper", "Failed to insert row: $contentValues")
                        } else {
                            Log.d("WeatherDatabaseHelper", "Inserted row: $contentValues")
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("WeatherDatabaseHelper", "Error inserting data: ${e.message}")
        } finally {
            db.endTransaction()
        }
    }

    fun getAllWeatherData(): List<TimeSeries> {
        Log.d("WeatherDatabaseHelper", "Fetching all weather data from database")
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WEATHER,
            arrayOf(COLUMN_VALID_TIME, COLUMN_NAME, COLUMN_LEVEL_TYPE, COLUMN_LEVEL, COLUMN_UNIT, COLUMN_VALUE),
            null, null, null, null, null
        )

        val weatherData = mutableListOf<TimeSeries>()
        val timeSeriesMap = mutableMapOf<String, MutableList<Parameter>>()

        cursor.use {
            while (it.moveToNext()) {
                val validTime = it.getString(it.getColumnIndexOrThrow(COLUMN_VALID_TIME))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME))
                val levelType = it.getString(it.getColumnIndexOrThrow(COLUMN_LEVEL_TYPE))
                val level = it.getInt(it.getColumnIndexOrThrow(COLUMN_LEVEL))
                val unit = it.getString(it.getColumnIndexOrThrow(COLUMN_UNIT))
                val value = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALUE))

                Log.d("WeatherDatabaseHelper", "Fetched row: validTime=$validTime, name=$name")

                val parameterList = timeSeriesMap.getOrPut(validTime) { mutableListOf() }
                val existingParam = parameterList.find { param -> param.name == name }

                if (existingParam != null) {
                    val updatedValues = existingParam.values.toMutableList()
                    updatedValues.add(value)
                    parameterList[parameterList.indexOf(existingParam)] = Parameter(
                        name, levelType, level, unit, updatedValues
                    )
                } else {
                    parameterList.add(Parameter(name, levelType, level, unit, listOf(value)))
                }
            }
        }

        for ((validTime, parameters) in timeSeriesMap) {
            weatherData.add(TimeSeries(validTime, parameters))
        }

        Log.d("WeatherDatabaseHelper", "Total fetched weather data entries: ${weatherData.size}")
        return weatherData
    }
}
