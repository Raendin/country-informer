package service

sealed trait Failure extends Product with Serializable
object Failure {
  final case class InvalidCountryCode(e: String)                              extends Failure
  final case class CountryServiceFailure(e: CountryService.Failure)           extends Failure
  final case class ExchangeRateServiceFailure(e: ExchangeRateService.Failure) extends Failure
  final case class WeatherServiceFailure(e: WeatherService.Failure)           extends Failure
}
