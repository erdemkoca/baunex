package ch.baunex.user.dto

data class CustomerContactCreateDTO(
    val personId: Long,
    val role: String?,
    val isPrimary: Boolean = false
)