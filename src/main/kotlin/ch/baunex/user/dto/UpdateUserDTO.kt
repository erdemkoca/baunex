package ch.baunex.user.dto

import ch.baunex.user.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserDTO(
    val email: String? = null,
    val phone: String? = null,
    val role: Role? = null,
    val password: String? = null,
    val street: String? = null
)

class RoleUpdateDTO {
    lateinit var role: Role
}