package ch.baunex.user.dto

import ch.baunex.user.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val id: Long?,
    val email: String,
    val role: Role,
    val phone: String?,
    val street: String?
)

@Serializable
data class UserResponseDTOList(
    var allClients: List<UserResponseDTO>
)