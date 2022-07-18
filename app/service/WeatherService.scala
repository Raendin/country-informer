package service

import io.circe.generic.auto._
import model.WeatherReport
import sttp.client3.asynchttpclient.zio.SttpClient
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import zio.Has
import zio.ZIO
import zio.ZLayer

object WeatherService {

  type WeatherService = Has[Service]

  trait Service {
    def getWeatherReport(city: String, country: String): ZIO[SttpClient, Failure, WeatherReport]
  }

  sealed trait Failure extends ThrowableFailure
  object Failure {
    case object NotFound                    extends Failure
    final case class Internal(e: Throwable) extends Failure
  }

  final case class Config(url: String, apiKey: String)

  def make(config: Config): ZIO[SttpClient, Failure, Service] = {
    ZIO.succeed {
      new Service {
        override def getWeatherReport(
            city: String,
            country: String
          ): ZIO[SttpClient, Failure, WeatherReport] = {

          val request = basicRequest
            .get(uri"${config.url}?q=$city,$country&appid=${config.apiKey}&units=metric")
            .response(asJson[WeatherReport])

          for {
            response <- send(request).mapError(Failure.Internal)
            report   <- ZIO.fromEither(response.body).mapError(Failure.Internal)
          } yield report
        }
      }
    }
  }

  def live(config: Config): ZLayer[SttpClient, Failure, WeatherService] = make(config).toLayer

  def getWeatherReport(
      city: String,
      country: String
    ): ZIO[WeatherService with SttpClient, Failure, WeatherReport] =
    ZIO.accessM(_.get.getWeatherReport(city, country))
}
