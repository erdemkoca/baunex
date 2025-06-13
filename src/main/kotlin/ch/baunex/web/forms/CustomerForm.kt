package ch.baunex.web.forms

import ch.baunex.user.dto.CustomerCreateDTO
import jakarta.ws.rs.FormParam

class CustomerForm {

    @FormParam("id")
    var id: Long? = null

    @FormParam("firstName")
    var firstName: String? = null

    @FormParam("lastName")
    var lastName: String? = null

    @FormParam("email")
    var email: String? = null

    @FormParam("street")
    var street: String? = null

    @FormParam("city")
    var city: String? = null

    @FormParam("zipCode")
    var zipCode: String? = null

    @FormParam("country")
    var country: String? = null

    @FormParam("phone")
    var phone: String? = null

    @FormParam("customerNumber")
    var customerNumber: String? = null

    @FormParam("companyName")
    var companyName: String? = null

    @FormParam("paymentTerms")
    var paymentTerms: String? = null

    @FormParam("creditLimit")
    var creditLimit: Double? = null

    @FormParam("industry")
    var industry: String? = null

    @FormParam("discountRate")
    var discountRate: Double? = null

    @FormParam("preferredLanguage")
    var preferredLanguage: String? = null

    @FormParam("marketingConsent")
    var marketingConsent: Boolean? = false

    @FormParam("taxId")
    var taxId: String? = null

    fun toCreateDTO(): CustomerCreateDTO {
        return CustomerCreateDTO(
            firstName        = firstName!!,
            lastName         = lastName!!,
            email            = email,
            street           = street,
            city             = city,
            zipCode          = zipCode,
            country          = country,
            phone            = phone,
            customerNumber   = customerNumber!!,
            companyName      = companyName,
            paymentTerms     = paymentTerms,
            creditLimit      = creditLimit,
            industry         = industry,
            discountRate     = discountRate,
            preferredLanguage= preferredLanguage,
            marketingConsent = marketingConsent ?: false,
            taxId            = taxId
        )
    }
}
