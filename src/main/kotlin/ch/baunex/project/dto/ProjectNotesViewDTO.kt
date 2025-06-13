package ch.baunex.project.dto

import ch.baunex.notes.dto.NoteForUI
import ch.baunex.user.dto.EmployeeReferenceDTO
import kotlinx.serialization.Serializable

@Serializable
data class ProjectNotesViewDTO(
    val projectId: Long,
    val projectName: String,
    val categories: List<String>,
    val employees: List<EmployeeReferenceDTO>,
    val notes: List<NoteForUI>,
)
