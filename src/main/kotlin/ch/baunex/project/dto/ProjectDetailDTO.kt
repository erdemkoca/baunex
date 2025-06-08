package ch.baunex.project.dto

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.notes.dto.NoteDto
import ch.baunex.project.model.ProjectStatus
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectDetailDTO(
    val id: Long,
    val projectNumberFormatted: String,
    val name: String,
    val customerId: Long,
    val customerName: String,
    val budget: Int,
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val endDate: LocalDate?,
    val description: String?,
    val status: ProjectStatus,
    val street: String?,
    val city: String?,
    val customer: CustomerDTO,
    val timeEntries: List<TimeEntryResponseDTO>,
    val catalogItems: List<ProjectCatalogItemDTO>,
    val contacts: List<CustomerContactDTO>,
    val notes: List<NoteDto> = emptyList()
)
