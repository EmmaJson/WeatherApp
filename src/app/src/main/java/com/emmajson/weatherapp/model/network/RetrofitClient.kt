package com.emmajson.weatherapp.model.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/"

    // Configure OkHttpClient with timeout settings
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS) // Timeout for establishing a connection
        .readTimeout(5, TimeUnit.SECONDS)    // Timeout for reading data
        .writeTimeout(5, TimeUnit.SECONDS)   // Timeout for writing data
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Add OkHttpClient to Retrofit
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherService: WeatherService = retrofit.create(WeatherService::class.java)
}
