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

    fun listAttachments(noteId: Long): List<MediaAttachmentDto> {
        val note = noteRepository.findById(noteId)
            ?: throw IllegalArgumentException("Note \$noteId nicht gefunden")
        return noteAttachmentService.listForNote(note)
    }

    @Transactional
    fun uploadAttachment(
        noteId: Long,
        fileStream: InputStream,
        originalFilename: String
    ): MediaAttachmentDto {
        val note = noteRepository.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId not found")
        return noteAttachmentService.uploadForNote(
            note,
            fileStream,
            originalFilename
        )
    }

    @Transactional
    fun linkAttachments(noteId: Long, attachmentIds: List<Long>) {
        val note = noteRepository.findById(noteId)
            ?: throw IllegalArgumentException("Note $noteId nicht gefunden")
        attachmentIds.forEach { attachmentId ->
            noteAttachmentService.linkAttachmentToNote(note, attachmentId)
        }
    }

    @Transactional
    fun deleteAttachment(attachmentId: Long): Boolean =
        noteAttachmentService.deleteAttachment(attachmentId)
}
