package ch.baunex.user.dto

data class CustomerCreateDTO(
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
    val marketingConsent: Boolean = false,
    val taxId: String?
)
