package model

final case class WeatherReport(
    coord: Coord,
    weather: Seq[Weather],
    base: String,
    main: MainWeather,
    visibility: Int,
    wind: Wind,
    clouds: Clouds,
    dt: Long,
    sys: WeatherSys,
    timezone: Int,
    id: Int,
    name: String,
    cod: Int)
