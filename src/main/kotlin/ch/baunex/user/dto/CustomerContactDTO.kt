package ch.baunex.user.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CustomerContactDTO(
    val id: Long,
    val personId: Long,
    val personName: String,     // oder ein kleines PersonDTO
    val role: String?,
    val isPrimary: Boolean,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)

