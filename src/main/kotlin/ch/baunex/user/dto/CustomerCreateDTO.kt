package ch.baunex.user.dto

import java.math.BigDecimal

data class CustomerCreateDTO(
    val firstName: String,
    val lastName: String,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,
    val phone: String?,
    val email: String?,
    val customerNumber: String,
    val companyName: String?,
    val paymentTerms: String?,
    val creditLimit: BigDecimal?,
    val industry: String?,
    val discountRate: Double?,
    val preferredLanguage: String?,
    val marketingConsent: Boolean = false,
    val taxId: String?
)
