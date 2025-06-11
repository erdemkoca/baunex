package ch.baunex.notes.facade

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.repository.NoteRepository
import ch.baunex.notes.service.NoteAttachmentService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.io.InputStream

@ApplicationScoped
class NoteAttachmentFacade {

    @Inject
    lateinit var noteAttachmentService: NoteAttachmentService

    @Inject
    lateinit var noteRepository: NoteRepository

    /**
     * Liste aller Attachments fuer eine Note
     */
    fun listAttachments(noteId: Long): List<MediaAttachmentDto> {
        val note = noteRepository.findById(noteId)
            ?: throw IllegalArgumentException("Note \$noteId nicht gefunden")
        return noteAttachmentService.listForNote(note)
    }

    /**
     * Upload eines Attachments fuer eine Note
     */
    @Transactional
    fun uploadAttachment(
        noteId: Long,
        fileStream: InputStream,
        originalFilename: String
    ): MediaAttachmentDto {
        val note = noteRepository.findById(noteId)
            ?: throw IllegalArgumentException("Note \$noteId nicht gefunden")
        return noteAttachmentService.uploadForNote(
            note,
            fileStream,
            originalFilename
        )
    }

    /**
     * Loeschen eines Attachments nach ID
     */
    @Transactional
    fun deleteAttachment(attachmentId: Long): Boolean =
        noteAttachmentService.deleteAttachment(attachmentId)
}
