package ch.baunex.controlreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContractorDto(
    val type: String?,
    val company: String,
    val street: String,
    val postalCode: String,
    val city: String
)