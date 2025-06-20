package ch.baunex.controlreport.dto

import ch.baunex.controlreport.model.ContractorType
import kotlinx.serialization.Serializable

@Serializable
data class ContractorDto(
    val type: ContractorType?,
    val company: String,
    val street: String,
    val postalCode: String,
    val city: String
)