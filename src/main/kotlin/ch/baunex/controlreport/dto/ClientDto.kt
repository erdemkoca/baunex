package ch.baunex.controlreport.dto

import ch.baunex.controlreport.model.ClientType

data class ClientDto(
    val type: ClientType?,
    val name: String,
    val street: String,
    val postalCode: String,
    val city: String
)