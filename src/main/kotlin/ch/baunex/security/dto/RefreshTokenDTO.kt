package ch.baunex.security.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenDTO(
    val refreshToken: String
)



@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

