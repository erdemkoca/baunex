package ch.baunex.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeReferenceDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phone: String?
)
