package com.emmajson.weatherapp.model.network

data class Parameter(
    val name: String,
    val levelType: String,
    val level: Int,
    val unit: String,
    var values: List<Float>
)

data class TimeSeries(
    val validTime: String,
    val parameters: List<Parameter>
)

data class WeatherResponse(
    val approvedTime: String,
    val referenceTime: String,
    val timeSeries: List<TimeSeries>
)
