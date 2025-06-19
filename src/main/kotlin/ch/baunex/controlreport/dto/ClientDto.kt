package ch.baunex.controlreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val type: String?,
    val name: String,
    val street: String,
    val postalCode: String,
    val city: String
)