package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import kotlinx.serialization.Serializable

@Serializable
data class ProjectListDTO(
    val id: Long,
    val name: String,
    val customerName: String,
    val budget: Int,
    val status: ProjectStatus
)
