package ch.baunex.controlreport.dto

import ch.baunex.user.model.CustomerType
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val type: CustomerType,
    val firstName: String,
    val lastName: String,
    val street: String?,
    val postalCode: String?,
    val city: String?
)