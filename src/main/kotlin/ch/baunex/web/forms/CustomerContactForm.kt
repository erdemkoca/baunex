package ch.baunex.web.forms

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import jakarta.ws.rs.FormParam

data class CustomerContactForm(
    @field:FormParam("id")
    val id: Long?,

    @field:FormParam("personId")
    val personId: Long?,

    // Person fields
    @field:FormParam("firstName")
    val firstName: String?,

    @field:FormParam("lastName")
    val lastName: String?,

    @field:FormParam("email")
    val email: String?,

    @field:FormParam("street")
    val street: String?,

    @field:FormParam("city")
    val city: String?,

    @field:FormParam("zipCode")
    val zipCode: String?,

    @field:FormParam("country")
    val country: String?,

    @field:FormParam("phone")
    val phone: String?,

    // Contact‐specific
    @field:FormParam("role")
    val role: String?,

    @field:FormParam("isPrimary")
    val isPrimary: Boolean?
) {

    fun toCreateDTO() = CustomerContactCreateDTO(
        firstName = firstName!!,
        lastName  = lastName!!,
        email     = email,
        street    = street,
        city      = city,
        zipCode   = zipCode,
        country   = country,
        phone     = phone,
        role      = role,
        isPrimary = isPrimary ?: false
    )

    fun toUpdateDTO() = CustomerContactUpdateDTO(
        personId  = personId!!,
        firstName = firstName!!,
        lastName  = lastName!!,
        email     = email,
        street    = street,
        city      = city,
        zipCode   = zipCode,
        country   = country,
        phone     = phone,
        role      = role,
        isPrimary = isPrimary ?: false
    )
}