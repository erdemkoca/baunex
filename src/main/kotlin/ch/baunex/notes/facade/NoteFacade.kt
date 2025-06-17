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

    /** Erzeugt eine neue Projekt-Note und ggf. eine DefectPosition */
//    fun createNoteForProject(projectId: Long, dto: NoteCreateDto): List<NoteDto> {
//        return noteService.createNoteForProject(projectId, dto)
//    }

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
    fun createNoteForProject(projectId: Long, createDto: NoteCreateDto): NoteDto {
        return noteService.createNote(createDto, projectId)
    }

    @Transactional
    fun addNoteToProject(dto: NoteCreateDto, creatorUserId: Long) {
        noteService.createNote(dto, creatorUserId)
    }

//    @Transactional
//    fun addNoteToProject(
//        projectId: Long,
//        title: String?,
//        category: NoteCategory,
//        content: String,
//        tags: List<String>
//    ) {
//        val project = projectService.getProjectWithEntries(projectId)
//            ?: throw IllegalArgumentException("Projekt nicht gefunden")
//        val adminId = employeeFacade.findByRole(Role.ADMIN).id
//        val creator = employeeService.getEmployee(adminId)
//            ?: throw IllegalArgumentException("Mitarbeiter nicht gefunden")
//
//        val note = NoteModel().apply {
//            this.title = title
//            this.category = category
//            this.content = content
//            this.tags = tags
//            this.createdAt = LocalDate.now()
//            this.createdBy = creator
//            this.project = project
//        }
//        project.notes.add(note)
//    }
}
