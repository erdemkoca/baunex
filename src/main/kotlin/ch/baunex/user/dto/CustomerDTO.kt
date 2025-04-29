package ch.baunex.user.dto

import java.time.LocalDateTime
import java.math.BigDecimal

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
    val creditLimit: BigDecimal?,
    val industry: String?,
    val discountRate: Double?,
    val preferredLanguage: String?,
    val marketingConsent: Boolean,
    val taxId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val contacts: List<CustomerContactDTO> = emptyList()
)