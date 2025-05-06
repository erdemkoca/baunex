package ch.baunex.user.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CustomerContactDTO(
    val id: Long,
    val personId: Long,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,
    val phone: String?,
    val role: String?,
    val isPrimary: Boolean,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)
