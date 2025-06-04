package ch.baunex.notes.service

import ch.baunex.documentGenerator.repository.DocumentRepository
import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.MediaAttachmentModel
import ch.baunex.notes.model.MediaType
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.repository.MediaAttachmentRepository
import ch.baunex.notes.repository.NoteRepository
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.repository.EmployeeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@ApplicationScoped
class NoteService(
    private val noteRepo: NoteRepository,
    private val mediaAttachmentRepo: MediaAttachmentRepository,
    private val projectRepo: ProjectRepository,
    private val timeEntryRepo: TimeEntryRepository,
    private val documentRepo: DocumentRepository,
    private val employeeRepo: EmployeeRepository
) {

    fun listNotesByProject(projectId: Long): List<NoteDto> {
        return noteRepo.findByProjectId(projectId).map { it.toDto() }
    }

    fun listNotesByTimeEntry(timeEntryId: Long): List<NoteDto> {
        return noteRepo.findByTimeEntryId(timeEntryId).map { it.toDto() }
    }

    fun listNotesByDocument(documentId: Long): List<NoteDto> {
        return noteRepo.findByDocumentId(documentId).map { it.toDto() }
    }

    fun getNoteById(noteId: Long): NoteDto {
        val note = noteRepo.findById(noteId) ?: throw IllegalArgumentException("Note with id $noteId not found")
        return note.toDto()
    }

    @Transactional
    fun createNote(createDto: NoteCreateDto, creatorUserId: Long): NoteDto {
        // 1. FK-Entitäten auflösen
        val projectModel = createDto.projectId?.let { id ->
            projectRepo.findById(id) ?: throw IllegalArgumentException("Project $id not found")
        }
        val timeEntryModel = createDto.timeEntryId?.let { id ->
            timeEntryRepo.findById(id) ?: throw IllegalArgumentException("TimeEntry $id not found")
        }
        val documentModel = createDto.documentId?.let { id ->
            documentRepo.findById(id) ?: throw IllegalArgumentException("Document $id not found")
        }

        val userModel = employeeRepo.findById(creatorUserId)
            ?: throw IllegalArgumentException("User $creatorUserId not found")

        // 2. Neue NoteModel-Instanz erstellen und Felder setzen
        val noteEntity = NoteModel().apply {
            this.project = projectModel
            this.timeEntry = timeEntryModel
            this.document = documentModel
            this.createdBy = userModel
            this.createdAt = LocalDateTime.now()
            this.title = createDto.title
            this.content = createDto.content
            this.category = createDto.category
            this.tags = createDto.tags
        }

        // 3. Persistieren
        noteRepo.persist(noteEntity)

        return noteEntity.toDto()
    }

    @Transactional
    fun updateNote(noteId: Long, updateDto: NoteCreateDto, updaterUserId: Long): NoteDto {
        val existing = noteRepo.findById(noteId) ?: throw IllegalArgumentException("Note $noteId not found")

        // Nur Ersteller oder Admin/Projektleiter darf bearbeiten
        if (existing.createdBy.id != updaterUserId /*&& !isAdmin(updaterUserId)*/) {
            throw IllegalAccessException("User $updaterUserId cannot edit note $noteId")
        }

        existing.title = updateDto.title
        existing.content = updateDto.content
        existing.category = updateDto.category
        existing.tags = updateDto.tags
        existing.updatedAt = LocalDateTime.now()

        // Projekt/TimeEntry/Document-Verknüpfung kann optional geändert werden, wenn nötig:
        // existing.project = updateDto.projectId?.let { projectRepo.findById(it) }

        // Panache managed entity, kein explizites merge nötig
        return existing.toDto()
    }

    @Transactional
    fun deleteNote(noteId: Long, deleterUserId: Long) {
        val existing = noteRepo.findById(noteId) ?: throw IllegalArgumentException("Note $noteId not found")

        if (existing.createdBy.id != deleterUserId /*&& !isAdmin(deleterUserId)*/) {
            throw IllegalAccessException("User $deleterUserId cannot delete note $noteId")
        }
        noteRepo.delete(existing)
        // CascadeType.ALL sorgt dafür, dass attachments mitgelöscht werden
    }

    @Transactional
    fun addAttachment(noteId: Long, url: String, type: String, caption: String?): MediaAttachmentDto {
        val note = noteRepo.findById(noteId) ?: throw IllegalArgumentException("Note $noteId not found")

        val mediaType = try {
            MediaType.valueOf(type.uppercase())
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid media type: $type")
        }

        val attachment = MediaAttachmentModel().apply {
            this.note = note
            this.url = url
            this.type = mediaType
            this.caption = caption
        }
        mediaAttachmentRepo.persist(attachment)
        return attachment.toDto()
    }

    fun listAttachments(noteId: Long): List<MediaAttachmentDto> {
        return mediaAttachmentRepo.findByNoteId(noteId).map { it.toDto() }
    }
}
