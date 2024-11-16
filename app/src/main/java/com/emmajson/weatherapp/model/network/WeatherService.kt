package com.emmajson.weatherapp.model.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("/weather/forecast")
    fun getWeather(
        @Query("lonLat") lonLat: String
    ): Call<WeatherResponse>
}