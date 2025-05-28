package ch.baunex.company.dto

import kotlinx.serialization.Serializable

@Serializable
data class CompanyDTO(
    val id: Long? = null,
    val name: String,
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val iban: String? = null,
    val bic: String? = null,
    val bankName: String? = null,
    val vatNumber: String? = null,
    val taxNumber: String? = null,
    val logo: String? = null,
    val defaultInvoiceFooter: String? = null,
    val defaultInvoiceTerms: String? = null,
    val defaultVatRate: Double = 8.1
) 