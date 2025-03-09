package ch.baunex.user.dto

import ch.baunex.user.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val email: String,
    val password: String,
    val role: Role
)

@Serializable
data class UserResponseDTO(
    val id: Long,
    val email: String,
    val role: Role
)
