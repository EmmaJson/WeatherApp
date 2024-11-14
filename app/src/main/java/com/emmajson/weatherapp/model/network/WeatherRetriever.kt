package com.emmajson.weatherapp.network

import android.util.Log
import com.emmajson.weatherapp.model.network.Parameter
import com.emmajson.weatherapp.model.network.TimeSeries
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WeatherRetriever {
    private val client = OkHttpClient()

    fun getWeather(lat: Double, lon: Double): List<TimeSeries> {
        //val url = "https://maceo.sth.kth.se/weather/forecast?lonLat=lon/$lon/lat/$lat"
        val url = "https://maceo.sth.kth.se/weather/forecast?lonLat=lon/14.333/lat/60.383"
        Log.d("WeatherRetriever", "Fetching weather data from URL: $url")
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d("WeatherRetriever", "API Response: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    if (jsonObject.has("timeSeries")) {
                        parseWeatherJson(responseBody)
                    } else {
                        Log.e("WeatherRetriever", "No 'timeSeries' field found")
                        emptyList()
                    }
                } else {
                    Log.e("WeatherRetriever", "API Error: ${response.code}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherRetriever", "Error fetching weather data: ${e.message}")
            emptyList()
        }
    }

    private fun parseWeatherJson(json: String): List<TimeSeries> {
        val jsonObject = JSONObject(json)

        if (!jsonObject.has("timeSeries")) {
            Log.e("WeatherRetriever", "No 'timeSeries' field found in response")
            return emptyList()
        }

        val timeSeriesArray = jsonObject.getJSONArray("timeSeries")
        val timeSeriesList = mutableListOf<TimeSeries>()

        for (i in 0 until timeSeriesArray.length()) {
            val entry = timeSeriesArray.getJSONObject(i)
            val validTime = entry.getString("validTime")
            val parametersArray = entry.getJSONArray("parameters")
            val parametersList = mutableListOf<Parameter>()

            for (j in 0 until parametersArray.length()) {
                val param = parametersArray.getJSONObject(j)
                val name = param.getString("name")
                val levelType = param.getString("levelType")
                val level = param.getInt("level")
                val unit = param.getString("unit")
                val valuesArray = param.getJSONArray("values")
                val valuesList = List(valuesArray.length()) { k -> valuesArray.getDouble(k) }

                parametersList.add(Parameter(name, levelType, level, unit, valuesList))
            }

            timeSeriesList.add(TimeSeries(validTime, parametersList))
        }

        Log.d("WeatherRetriever", "Parsed ${timeSeriesList.size} TimeSeries entries")
        return timeSeriesList
    }
}