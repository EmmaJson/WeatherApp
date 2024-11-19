package com.emmajson.weatherapp.model.geoAPI

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoAPI {
    @GET("search")
    fun getCoordinates(@Query("q") place: String): Call<List<GeocodeResponse>>
}

// Data Model for SMHI Response
data class GeocodeResponse(
    val lat: String,
    val lon: String,
    val display_name: String
)

// Retrofit Client for SMHI API
object RetrofitClient {
    private const val GEOCODE_BASE_URL = "https://geocode.maps.co/"

    val geocodeRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GEOCODE_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geoAPI: GeoAPI = geocodeRetrofit.create(GeoAPI::class.java)
}


// Function to Fetch Coordinates Using GeoAPI
fun fetchCoordinates(city: String, onSuccess: (Double, Double) -> Unit, onError: (String) -> Unit) {
    val geoApi = RetrofitClient.geoAPI
    val call = geoApi.getCoordinates(city)

    call.enqueue(object : Callback<List<GeocodeResponse>> {
        override fun onResponse(call: Call<List<GeocodeResponse>>, response: Response<List<GeocodeResponse>>) {
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                val firstResult = response.body()!![0]

                // Convert and round coordinates to 3 decimal places
                val lat = String.format("%.3f", firstResult.lat.toDouble()).toDouble()
                val lon = String.format("%.3f", firstResult.lon.toDouble()).toDouble()

                onSuccess(lat, lon)
            } else {
                onError("No results found for $city")
            }
        }

        override fun onFailure(call: Call<List<GeocodeResponse>>, t: Throwable) {
            onError("Error fetching coordinates: ${t.message}")
        }
    })
}
