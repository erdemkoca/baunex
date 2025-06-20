package ch.baunex.notes.service

import ch.baunex.controlreport.service.DefectPositionService
import ch.baunex.documentGenerator.repository.DocumentRepository
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.repository.NoteRepository
import ch.baunex.notes.repository.MediaAttachmentRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.io.InputStream
import java.time.LocalDate

@ApplicationScoped
class NoteService(
    private val noteRepo: NoteRepository,
    private val mediaAttachmentRepo: MediaAttachmentRepository,
    private val projectRepo: ProjectRepository,
    private val timeEntryRepo: TimeEntryRepository,
    private val documentRepo: DocumentRepository,
    private val employeeRepo: EmployeeRepository,
    private val noteAttachmentService: NoteAttachmentService,
    private val defectPositionService: DefectPositionService
) {

    fun listNotesByProject(projectId: Long): List<NoteDto> =
        noteRepo.findByProjectId(projectId).map { it.toDto() }

    fun listNotesByTimeEntry(timeEntryId: Long): List<NoteDto> =
        noteRepo.findByTimeEntryId(timeEntryId).map { it.toDto() }

    fun listNotesByDocument(documentId: Long): List<NoteDto> =
        noteRepo.findByDocumentId(documentId).map { it.toDto() }

    fun getNoteById(noteId: Long): NoteDto =
        noteRepo.findById(noteId)
            ?.toDto()
            ?: throw IllegalArgumentException("Note with id $noteId not found")

    /**
     * Create a new note.  If category == MÄNGEL, also create a DefectPosition.
     */
    @Transactional
    fun createNote(createDto: NoteDto, creatorUserId: Long): NoteDto {
        // 1) NoteModel-Instanz anlegen und erforderliche Fremd­schlüssel auflösen
        val note = NoteModel().apply {
            // a) Projekt–Verknüpfung (Pflichtfeld für Projekt-Notizen)
            this.project = createDto.projectId
                ?.let { projectRepo.findById(it) }
                ?: throw IllegalArgumentException("Project ${createDto.projectId} not found")

            // b) TimeEntry–Verknüpfung (optional – nur, wenn eine TimeEntry-Note)
            this.timeEntry = createDto.timeEntryId
                ?.let { timeEntryRepo.findById(it) }
                ?: null  // kein Fehler, wenn timeEntryId == null

            // c) Document–Verknüpfung (optional – z.B. Notiz zu einem PDF-Dokument)
            this.document = createDto.documentId
                ?.let { documentRepo.findById(it) }
                ?: null

            // d) Wer hat die Notiz erstellt?
            this.createdBy = employeeRepo.findById(creatorUserId)
                ?: throw IllegalArgumentException("User $creatorUserId not found")

            // e) Timestamps
            this.createdAt = LocalDate.now()
            this.updatedAt = LocalDate.now()

            // f) Restliche Felder aus dem DTO
            this.title    = createDto.title
            this.content  = createDto.content
            this.category = createDto.category
            this.tags     = createDto.tags
        }

        // 2) In die Datenbank speichern
        noteRepo.persist(note)

        // 3) Spezialfall „MÄNGEL“-Notiz → DefectPosition anlegen
        if (createDto.category == NoteCategory.MÄNGEL) {
            val defectPosition = defectPositionService.createFromNote(note)
            note.defectPosition = defectPosition
        }

        // 4) DTO für die API-Antwort zurückgeben
        return note.toDto()
    }


    @Transactional
    fun updateNote(noteId: Long, updateDto: NoteDto, updaterUserId: Long): NoteDto {
        val existing = noteRepo.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId not found")

        if (existing.createdBy.id != updaterUserId) {
            throw IllegalAccessException("User $updaterUserId cannot edit note $noteId")
        }

        existing.apply {
            title = updateDto.title
            content = updateDto.content
            category = updateDto.category
            tags = updateDto.tags
            updatedAt = LocalDate.now()
        }

        return existing.toDto()
    }

    @Transactional
    fun deleteNote(noteId: Long, deleterUserId: Long) {
        val existing = noteRepo.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId not found")

        if (existing.createdBy.id != deleterUserId) {
            throw IllegalAccessException("User $deleterUserId cannot delete note $noteId")
        }
        noteRepo.delete(existing)
    }
}
