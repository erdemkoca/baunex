package ch.baunex.controlreport.dto

data class ContractorDto(
    val type: String,
    val company: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String
)