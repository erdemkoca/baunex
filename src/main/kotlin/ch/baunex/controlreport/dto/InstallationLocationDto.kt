package ch.baunex.controlreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class InstallationLocationDto(
    val street: String,
    val postalCode: String,
    val city: String,
    val buildingType: String?,
    val parcelNumber: String?
)