package ch.baunex.user.dto

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
    val customerNumber: String,
    val companyName: String?,
    val paymentTerms: String?,
    val creditLimit: Double?,
    val industry: String?,
    val discountRate: Double?,
    val preferredLanguage: String?,
    val marketingConsent: Boolean,
    val taxId: String?,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val contacts: List<CustomerContactDTO> = emptyList(),
    val address: String? = null
)