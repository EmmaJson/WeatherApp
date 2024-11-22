package com.emmajson.weatherapp.model.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {
    @GET("lon/{lon}/lat/{lat}/data.json")
    fun getWeather(
        @Path("lon") lon: Double,
        @Path("lat") lat: Double
    ): Call<WeatherResponse>
}
