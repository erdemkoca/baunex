package ch.baunex.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserDTO(
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val street: String? = null
)