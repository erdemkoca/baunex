package ch.baunex.project.dto

import ch.baunex.notes.dto.NoteDto
import ch.baunex.project.model.ProjectStatus
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectCreateDTO(
    val name: String,
    val customerId: Long,
    val budget: Int,
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val endDate: LocalDate,
    val description: String? = null,
    val status: ProjectStatus = ProjectStatus.PLANNED,
    val street: String? = null,
    val city: String? = null,
    var initialNotes: List<NoteDto>
)
