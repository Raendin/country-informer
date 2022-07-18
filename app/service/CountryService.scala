package service

import io.circe.generic.auto._
import model.CountryInfo
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe._
import zio._

object CountryService {

  type CountryService = Has[Service]

  trait Service {
    protected def names: Map[String, String]
    protected def capitals: Map[String, String]
    protected def currency: Map[String, String]

    def getInfo(code: String): ZIO[SttpClient, Failure, CountryInfo]
  }

  sealed trait Failure extends ThrowableFailure
  object Failure {
    case object NotFound                    extends Failure
    final case class Internal(e: Throwable) extends Failure
  }

  final case class Config(namesUrl: String, capitalUrl: String, currencyUrl: String)

  def make(config: Config): ZIO[SttpClient, Failure, Service] = {

    val requestCapitals = basicRequest
      .get(uri"${config.capitalUrl}")
      .response(asJson[Map[String, String]])

    val requestNames = basicRequest
      .get(uri"${config.namesUrl}")
      .response(asJson[Map[String, String]])

    val requestCurrency = basicRequest
      .get(uri"${config.currencyUrl}")
      .response(asJson[Map[String, String]])

    for {
      responseCapitals <- send(requestCapitals).mapError(Failure.Internal)
      caps             <- ZIO.fromEither(responseCapitals.body).mapError(Failure.Internal)
      responseNames    <- send(requestNames).mapError(Failure.Internal)
      name             <- ZIO.fromEither(responseNames.body).mapError(Failure.Internal)
      responseCapitals <- send(requestCurrency).mapError(Failure.Internal)
      curr             <- ZIO.fromEither(responseCapitals.body).mapError(Failure.Internal)
    } yield (new Service {
      override val capitals: Map[String, String] = caps
      override val names: Map[String, String]    = name
      override val currency: Map[String, String] = curr

      override def getInfo(code: String): ZIO[SttpClient, Failure, CountryInfo] =
        ZIO
          .fromOption(for {
            name <- names.get(code)
            cap  <- capitals.get(code)
            curr <- currency.get(code)
          } yield CountryInfo(name, cap, curr))
          .orElseFail(Failure.NotFound)
    })
  }

  def live(config: Config): ZLayer[SttpClient, Failure, CountryService] =
    make(config).toLayer

  def getInfo(code: String): ZIO[CountryService with SttpClient, Failure, CountryInfo] =
    ZIO.accessM(_.get.getInfo(code))
}
