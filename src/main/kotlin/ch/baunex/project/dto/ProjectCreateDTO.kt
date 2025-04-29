package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectCreateDTO(
    val name: String,
    val customerId: Long,
    val budget: Int,
    @Contextual val startDate: LocalDate,
    @Contextual val endDate: LocalDate,
    val description: String? = null,
    val status: ProjectStatus = ProjectStatus.PLANNED,
    val street: String? = null,
    val city: String? = null,
    val contact: String? = null
)
