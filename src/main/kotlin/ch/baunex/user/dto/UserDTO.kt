package ch.baunex.user.dto

import ch.baunex.user.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val email: String? = null,
    val password: String? = null,
    val role: Role,
    val phone: String? = null,
    val street: String? = null,
    val city: String? = null
)

@Serializable
//data class UserResponseDTO(
//    val id: Long?,
//    val email: String,
//    val phone: String?,
//    val street: String?,
//    val role: Role
//)
data class UserResponseDTO(
    val id: Long?,
    val email: String,
    val role: Role, // ✅ Fix: Use Role type directly
    val phone: String?, // ✅ Include phone
    val street: String? // ✅ Include street
)



@Serializable
data class LoginDTO(val email: String, val password: String)

@Serializable
data class TokenResponse(
    val token: String,
    val id: Long,
    val role: Role
)
