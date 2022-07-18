# country-informer

Demo webservice with Play Framework. 

Distributive is provided as ZIP-file country-informer-1.0.zip

## Install

    unzip country-informer-1.0.zip

## Run the server

    ./country-informer-1.0/bin/country-informer

# Http Server

The server returns information about country by its ISO code

## Get information about country

### Request

`GET /status?country`

    curl http://localhost:8080/status?country=RU

### Response

```json
{
    "country": "Russia",
    "capital": "Moscow",
    "temperature": 18.14,
    "currency": "RUB",
    "currencyRate": 57.099994
}
```