package ch.baunex.notes.facade

import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toNoteForUI
import ch.baunex.notes.model.NoteCategory
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.service.NoteService
import ch.baunex.project.dto.ProjectNotesViewDTO
import ch.baunex.project.service.ProjectService
import ch.baunex.user.dto.EmployeeReferenceDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class NoteFacade(
    private val noteService: NoteService,
    private val projectService: ProjectService,
    private val employeeFacade: EmployeeFacade,
    private val employeeService: EmployeeService
) {

    fun getNote(noteId: Long): NoteDto =
        noteService.getNoteById(noteId)

    @Transactional
    fun getProjectNotesView(id: Long): ProjectNotesViewDTO {
        val project = projectService.getProjectWithEntries(id)
            ?: throw IllegalArgumentException("Projekt mit ID $id nicht gefunden")

        val employees = employeeFacade.listAll()
            .map { EmployeeReferenceDTO(it.id, it.firstName, it.lastName) }
        val categories = NoteCategory.values().map { it.name }

        // 1) Projekt-Notizen
        val projNotes = project.notes.map { note ->
            note.toNoteForUI(source = "project")
        }

        // 2) TimeEntry-Notizen
        val teNotes = project.timeEntries.flatMap { entry ->
            entry.notes.map { note ->
                note.toNoteForUI(
                    source = "timeEntry",
                    entryId = entry.id,
                    entryDate = entry.date,
                    entryTitle = entry.title
                )
            }
        }

        val allNotes = (projNotes + teNotes)
            .sortedByDescending { it.createdAt }

        return ProjectNotesViewDTO(
            projectId = project.id,
            projectName = project.name,
            categories = categories,
            employees = employees,
            notes = allNotes
        )
    }

    @Transactional
    fun addNoteToProject(createDto: NoteCreateDto, userId: Long): NoteDto {
        return noteService.createNote(createDto, userId)
    }

    @Transactional
    fun updateNoteOfProject(
        noteId: Long,
        updateDto: NoteCreateDto,
        userId: Long
    ): NoteDto {
        // this will throw if the note isn't found or the user isn't allowed
        return noteService.updateNote(noteId, updateDto, userId)
    }
}
