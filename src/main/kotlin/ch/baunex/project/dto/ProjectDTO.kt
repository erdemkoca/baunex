package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectDTO(
        val id: Long? = null,
        val name: String,
        val client: String,
        val budget: Int,
        val contact: String? = null,
        @Contextual val startDate: LocalDate? = null,
        @Contextual val endDate: LocalDate? = null,
        val description: String? = null,
        val status: ProjectStatus = ProjectStatus.PLANNED,
        val street: String? = null,
        val city: String? = null
)