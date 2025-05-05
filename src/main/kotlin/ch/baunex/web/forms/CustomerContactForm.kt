package ch.baunex.web.forms

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import jakarta.ws.rs.FormParam

class CustomerContactForm {
    @FormParam("id")
    var id: Long? = null

    @FormParam("personId")
    var personId: Long? = null

    @FormParam("role")
    var role: String? = null

    @FormParam("isPrimary")
    var isPrimary: Boolean? = false

    fun toCreateDTO(): CustomerContactCreateDTO =
        CustomerContactCreateDTO(
            personId   = personId!!,
            role       = role,
            isPrimary  = isPrimary ?: false
        )

    fun toUpdateDTO(): CustomerContactUpdateDTO =
        CustomerContactUpdateDTO(
            role       = role,
            isPrimary  = isPrimary ?: false
        )
}