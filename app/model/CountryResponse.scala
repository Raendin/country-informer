package model

import play.api.libs.json._

final case class CountryResponse(
    country: String,
    capital: String,
    temperature: Double,
    currency: String,
    currencyRate: Double)

object CountryResponse {
  implicit val format: OFormat[CountryResponse] = Json.format[CountryResponse]
}
