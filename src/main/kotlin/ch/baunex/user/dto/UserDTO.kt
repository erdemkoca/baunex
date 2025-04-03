package ch.baunex.user.dto

import ch.baunex.user.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val email: String,
    val password: String? = null,
    val role: Role,
    val phone: String? = null,
    val street: String? = null,
    val city: String? = null
)


@Serializable
data class LoginDTO(val email: String, val password: String)
