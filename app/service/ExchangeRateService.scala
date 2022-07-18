package service

import io.circe.generic.auto._
import sttp.client3.asynchttpclient.zio.SttpClient
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import zio.Has
import zio.ZIO
import zio.ZLayer

import java.time.Instant

object ExchangeRateService {

  type ExchangeRateService = Has[Service]

  trait Service {
    def getRate(currency: String, base: String): ZIO[SttpClient, Failure, Double]
  }

  sealed trait Failure extends ThrowableFailure
  object Failure {
    case object NotFound                    extends Failure
    final case class Internal(e: Throwable) extends Failure
  }

  final case class Config(url: String, apiKey: String)

  def make(config: Config): ZIO[SttpClient, Failure, Service] = {
    ZIO.succeed(new Service {

      final case class RatesResponse(
          disclaimer: String,
          license: String,
          timestamp: Long,
          base: String,
          rates: Map[String, Double])

      override def getRate(currency: String, base: String): ZIO[SttpClient, Failure, Double] = {
        val request = basicRequest
          .get(uri"${config.url}?app_id=${config.apiKey}&base=$base")
          .response(asJson[RatesResponse])

        for {
          response <- send(request).mapError(Failure.Internal)
          rates    <- ZIO.fromEither(response.body).mapError(Failure.Internal)
          rate     <- ZIO.fromOption(rates.rates.get(currency)).orElseFail(Failure.NotFound)
        } yield rate
      }
    })
  }

  def live(config: Config): ZLayer[SttpClient, Failure, ExchangeRateService] =
    make(config).toLayer

  def getRate(
      currency: String,
      base: String
    ): ZIO[ExchangeRateService with SttpClient, Failure, Double] =
    ZIO.accessM(_.get.getRate(currency, base))
}
