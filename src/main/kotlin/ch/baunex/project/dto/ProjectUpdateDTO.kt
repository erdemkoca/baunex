package ch.baunex.project.dto

import ch.baunex.notes.dto.NoteDto
import ch.baunex.project.model.ProjectStatus
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectUpdateDTO(
    val name: String? = null,
    val customerId: Long? = null,
    val budget: Int? = null,
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val endDate: LocalDate? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
    val street: String? = null,
    val city: String? = null,
    val updatedNotes: List<NoteDto>
)
