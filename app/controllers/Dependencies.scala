package controllers

import service.CountryService.CountryService
import service.ExchangeRateService.ExchangeRateService
import service.WeatherService.WeatherService
import service._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.RLayer

object Dependencies {
  type Dependencies = SttpClient with CountryService with ExchangeRateService with WeatherService

  def apply(config: CountryInfoConfig): RLayer[Any, Dependencies] = {
    val sttp = AsyncHttpClientZioBackend.layer()

    val cs = sttp >>> CountryService.live(config.countrySvcCfg)
    val es = sttp >>> ExchangeRateService.live(config.exchangeRateServiceCfg)
    val ws = sttp >>> WeatherService.live(config.weatherServiceCfg)

    cs ++ es ++ ws ++ sttp
  }
}
