package com.emmajson.weatherapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response
// JSON
import com.google.gson.Gson
// Persistant
import android.app.Application
import kotlinx.coroutines.runBlocking
import retrofit2.http.Query


class WeatherRetriever (private val application: Application) {


    init{
        println("Init TestFile")
    }

    interface WeatherService {
        @GET("weather/forecast")
        fun getForecast(
            @Query("lonLat") lonLat: String
        ): Call<WeatherResponse>
    }

    object RetrofitClient {
        private const val BASE_URL = "https://maceo.sth.kth.se/"

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun fetchWeatherData() = runBlocking {
        val weatherService = RetrofitClient.retrofit.create(WeatherService::class.java)
        val lonLat = "lon/14.333/lat/60.383"
        val call = weatherService.getForecast(lonLat)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        println("Approved Time: ${weatherResponse.approvedTime}")
                        println("Reference Time: ${weatherResponse.referenceTime}")

                        weatherResponse.timeSeries.forEach { timeSeries ->
                            println("\nValid Time: ${timeSeries.validTime}")

                            val temperature = timeSeries.getParameter("t")?.values?.firstOrNull()
                            val windSpeed = timeSeries.getParameter("ws")?.values?.firstOrNull()
                            val windDirection = timeSeries.getParameter("wd")?.values?.firstOrNull()
                            val cloudCover = timeSeries.getParameter("tcc_mean")?.values?.firstOrNull()
                            val visibility = timeSeries.getParameter("vis")?.values?.firstOrNull()
                            val precipitation = timeSeries.getParameter("pmean")?.values?.firstOrNull()

                            println("Temperature: ${temperature ?: "N/A"} °C")
                            println("Wind Speed: ${windSpeed ?: "N/A"} m/s")
                            println("Wind Direction: ${windDirection ?: "N/A"}°")
                            println("Cloud Cover: ${cloudCover ?: "N/A"} octas")
                            println("Visibility: ${visibility ?: "N/A"} km")
                            println("Precipitation: ${precipitation ?: "N/A"} kg/m²/h")
                            println("------")
                        }
                    } else {
                        println("Response body is null.")
                    }
                } else {
                    println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                println("Failed to fetch data: ${t.message}")
            }
        })
    }

    data class WeatherResponse(val approvedTime: String, val referenceTime: String, val geometry: Geometry, val timeSeries: List<TimeSeries>)

    data class Geometry(val type: String, val coordinates: List<List<Double>>)

    data class TimeSeries(val validTime: String, val parameters: List<Parameter>) {
        fun getParameter(name: String): Parameter? {
            return parameters.find { it.name == name }
        }
    }
    data class Parameter(val name: String, val levelType: String, val level: Int, val unit: String, val values: List<Double>)

    fun parseWeatherJson(jsonData: String) {
        val gson = Gson()

        // Deserialize the JSON into a WeatherResponse object
        val weatherResponse = gson.fromJson(jsonData, WeatherResponse::class.java)

        // Access and print the parsed data
        println("Approved Time: ${weatherResponse.approvedTime}")
        println("Reference Time: ${weatherResponse.referenceTime}")

        // Print coordinates
        val coordinates = weatherResponse.geometry.coordinates
        println("Coordinates: ${coordinates[0][0]}, ${coordinates[0][1]}")

        // Loop through the time series data
        weatherResponse.timeSeries.forEach { timeSeries ->
            println("Valid Time: ${timeSeries.validTime}")
            timeSeries.parameters.forEach { param ->
                println("Parameter: ${param.name}, Level: ${param.level}, Unit: ${param.unit}, Values: ${param.values.joinToString()}")
            }
            println("------")
        }
    }
}
