package controllers

import controllers.CountryInfoConfig._
import controllers.ZActionBuilder._
import eu.timepit.refined.refineV
import model._
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import service.CountryService.CountryService
import service.ExchangeRateService.ExchangeRateService
import service.WeatherService.WeatherService
import service._
import sttp.client3.asynchttpclient.zio.SttpClient
import zio._

import javax.inject._

class CountryInfoController @Inject() (
    configuration: Configuration,
    controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  val cfg: CountryInfoConfig = configuration.get[CountryInfoConfig]("service")

  def status(country: String): Action[AnyContent] = {
    val response: ZIO[
      WeatherService with SttpClient with ExchangeRateService with CountryService,
      Failure,
      CountryResponse
    ] = for {
      code <- refineV[Iso2CodeRefine](country)
        .fold(
          e => ZIO.fail(Failure.InvalidCountryCode(e)),
          a => ZIO.succeed(CountryCodeIso2(a))
        )
      info <- CountryService.getInfo(code.value.value).mapError(Failure.CountryServiceFailure)
      rate <- ExchangeRateService
        .getRate(info.currency, "USD")
        .mapError(Failure.ExchangeRateServiceFailure)
      weather <- WeatherService
        .getWeatherReport(info.capital, code.value.value)
        .mapError(Failure.WeatherServiceFailure)
    } yield CountryResponse(info.name, info.capital, weather.main.temp, info.currency, rate)

    val result: UIO[Result] = response
      .fold(
        {
          case Failure.InvalidCountryCode(e) => Results.BadRequest(s"Invalid argument: $e")
          case Failure.CountryServiceFailure(e) =>
            e match {
              case CountryService.Failure.NotFound =>
                Results.BadRequest(s"Country $country not found")
              case CountryService.Failure.Internal(_) => Results.InternalServerError
            }
          case Failure.ExchangeRateServiceFailure(_) => Results.InternalServerError
          case Failure.WeatherServiceFailure(_)      => Results.InternalServerError
        },
        a => Results.Ok(Json.toJson(a))
      )
      .provideLayer(Dependencies(cfg))
      .catchAll(_ => ZIO.succeed(Results.InternalServerError))

    Action.zio(_ => result)
  }
}
