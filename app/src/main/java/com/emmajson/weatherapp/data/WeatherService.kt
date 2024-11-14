package com.emmajson.weatherapp.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherService {
    @GET("weather/forecast")
    suspend fun getWeatherForecast(@Query("lonLat") lonLat: String): WeatherRetriever.WeatherResponse
}


object RetrofitClient {
    private const val BASE_URL = "https://maceo.sth.kth.se/"

    val weatherService: WeatherService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)
}


