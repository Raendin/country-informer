package model

final case class MainWeather(
    temp: Double,
    feels_like: Double,
    temp_min: Double,
    temp_max: Double,
    pressure: Int,
    humidity: Int)
