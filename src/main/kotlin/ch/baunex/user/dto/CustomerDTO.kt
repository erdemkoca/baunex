package ch.baunex.user.dto

import java.time.LocalDateTime
import java.math.BigDecimal

data class CustomerDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String?,
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
    val updatedAt: LocalDateTime
)