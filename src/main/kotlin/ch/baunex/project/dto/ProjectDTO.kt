package ch.baunex.project.dto

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.project.model.ProjectStatus
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
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
        val city: String? = null,
        val timeEntries: List<TimeEntryResponseDTO> = emptyList(),
        val catalogItems: List<ProjectCatalogItemDTO> = emptyList(),
        val customerId: Long,
        val customerName: String,
        val projectNumberFormatted: String,
        val projectNumber: Int

)