package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import kotlinx.serialization.Serializable

@Serializable
data class ProjectListDTO(
    val id: Long,
    val name: String,
    val customerId: Long,
    val customerName: String,
    val status: String,
    val budget: Int? = null
)
