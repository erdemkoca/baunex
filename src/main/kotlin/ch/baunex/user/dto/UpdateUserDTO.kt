package ch.baunex.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserDTO(
    val email: String?,
    val phone: String?,
    val password: String?
)