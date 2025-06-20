package ch.baunex.notes.service

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.MediaAttachmentModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.repository.MediaAttachmentRepository
import ch.baunex.upload.service.UploadService
import ch.baunex.upload.spi.StorageService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.io.InputStream

@ApplicationScoped
class NoteAttachmentService {

    @Inject
    lateinit var uploadService: UploadService

    @Inject
    lateinit var noteAttachmentRepository: MediaAttachmentRepository

    @Inject
    lateinit var mediaAttachmentRepository: MediaAttachmentRepository

    @Inject
    lateinit var storageService: StorageService

    fun listForNote(note: NoteModel): List<MediaAttachmentDto> {
        val noteId = note.id ?: throw IllegalArgumentException("Note ID darf nicht null sein")
        return mediaAttachmentRepository
            .findByNoteId(noteId)
            .map { it.toDto() }
    }

    @Transactional
    fun uploadForNote(note: NoteModel, stream: InputStream, filename: String): MediaAttachmentDto {
        // 1) hand off to storage layer
        val url = storageService.save(stream, filename)

        // 2) persist DB entity
        val att = MediaAttachmentModel().apply {
            this.note    = note
            this.url     = url
            this.caption = filename
        }
        mediaAttachmentRepository.persist(att)
        return att.toDto()
    }

    @Transactional
    fun linkAttachmentToNote(note: NoteModel, attachmentId: Long) {
        val attachment = mediaAttachmentRepository.findById(attachmentId)
            ?: throw IllegalArgumentException("Attachment $attachmentId nicht gefunden")
        // Remove von vorherigem Parent, falls nötig:
        attachment.note = note
        // Falls du Panache benutzt, genügt:
        attachment.persist()
    }

    @Transactional
    fun deleteAttachment(attachmentId: Long): Boolean {
        val att = mediaAttachmentRepository.findById(attachmentId) ?: return false
        storageService.delete(att.url)
        att.delete()
        return true
    }
}
