package controllers

import com.typesafe.config.Config
import play.api.ConfigLoader
import service._

final case class CountryInfoConfig(
    countrySvcCfg: CountryService.Config,
    exchangeRateServiceCfg: ExchangeRateService.Config,
    weatherServiceCfg: WeatherService.Config)

object CountryInfoConfig {
  implicit val configLoader: ConfigLoader[CountryInfoConfig] =
    (rootConfig: Config, path: String) => {
      val config = rootConfig.getConfig(path)
      CountryInfoConfig(
        CountryService.Config(
          config.getString("country.names_url"),
          config.getString("country.capital_url"),
          config.getString("country.currency_url")
        ),
        ExchangeRateService.Config(
          config.getString("rate.url"),
          config.getString("rate.api_key")
        ),
        WeatherService.Config(
          config.getString("weather.url"),
          config.getString("weather.api_key")
        )
      )
    }
}
