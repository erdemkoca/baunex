package ch.baunex.controlreport.dto

data class InstallationLocationDto(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val buildingType: String?,
    val parcelNumber: String?
)