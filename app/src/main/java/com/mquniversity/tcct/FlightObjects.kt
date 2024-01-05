package com.mquniversity.tcct

data class RequestBody(
    val flights: Array<Flight>
)

data class ResponseBody(
    val flightEmissions: Array<FlightWithEmissions>,
    val modelVersion: ModelVersion
)

data class FlightWithEmissions(
    val flight: Flight,
    val emissionsGramsPerPax: EmissionsGramsPerPax
)

data class Flight(
    val origin: String,
    val destination: String,
    val operatingCarrierCode: String,
    val flightNumber: Int,
    val departureDate: Date,
)

data class Date(
    val year: Int,
    val month: Int,
    val day: Int
)

data class EmissionsGramsPerPax(
    val first: Int,
    val business: Int,
    val premiumEconomy: Int,
    val economy: Int
)

data class ModelVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val dated: String
)
