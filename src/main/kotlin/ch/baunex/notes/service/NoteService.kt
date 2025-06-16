package ch.baunex.notes.service

import ch.baunex.controlreport.service.DefectPositionService
import ch.baunex.documentGenerator.repository.DocumentRepository
import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.NoteCreateDto
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
    fun createNote(createDto: NoteCreateDto, creatorUserId: Long): NoteDto {
        val note = NoteModel().apply {
            this.project = createDto.projectId?.let { projectRepo.findById(it) }
                ?: throw IllegalArgumentException("Project ${createDto.projectId} not found")
            this.timeEntry = createDto.timeEntryId?.let { timeEntryRepo.findById(it) }
                ?: throw IllegalArgumentException("TimeEntry ${createDto.timeEntryId} not found")
            this.document = createDto.documentId?.let { documentRepo.findById(it) }
                ?: throw IllegalArgumentException("Document ${createDto.documentId} not found")
            this.createdBy = employeeRepo.findById(creatorUserId)
                ?: throw IllegalArgumentException("User $creatorUserId not found")
            this.createdAt = LocalDate.now()
            this.updatedAt = LocalDate.now()
            this.title = createDto.title
            this.content = createDto.content
            this.category = createDto.category
            this.tags = createDto.tags
        }
        noteRepo.persist(note)

        if (createDto.category == NoteCategory.MÄNGEL) {
            defectPositionService.createFromNote(note)
        }

        return note.toDto()
    }

    @Transactional
    fun updateNote(noteId: Long, updateDto: NoteCreateDto, updaterUserId: Long): NoteDto {
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

    @Transactional
    fun addAttachment(
        noteId: Long,
        fileStream: InputStream,
        originalFilename: String
    ): MediaAttachmentDto {
        val note = noteRepo.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId not found")
        return noteAttachmentService.uploadForNote(
            note,
            fileStream,
            originalFilename
        )
    }

    fun listAttachments(noteId: Long): List<MediaAttachmentDto> {
        val note = noteRepo.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId not found")
        return noteAttachmentService.listForNote(note)
    }
}
