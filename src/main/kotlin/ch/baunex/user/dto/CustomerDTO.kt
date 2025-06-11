package ch.baunex.user.dto

import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CustomerDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,
    val phone: String?,
    val customerNumber: Int,
    val formattedCustomerNumber: String,
    val companyName: String?,
    val paymentTerms: String?,
    val creditLimit: Double?,
    val industry: String?,
    val discountRate: Double?,
    val preferredLanguage: String?,
    val marketingConsent: Boolean,
    val taxId: String?,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime,
    val contacts: List<CustomerContactDTO> = emptyList(),
    val address: String? = null
)