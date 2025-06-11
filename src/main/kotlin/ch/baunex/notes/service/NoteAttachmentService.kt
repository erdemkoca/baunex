package ch.baunex.notes.service

import ch.baunex.notes.controller.NoteAttachmentController
import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.MediaAttachmentModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.notes.repository.MediaAttachmentRepository
import ch.baunex.upload.service.UploadService
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

    fun listForNote(note: NoteModel): List<MediaAttachmentDto> {
        val noteId = note.id ?: throw IllegalArgumentException("Note ID darf nicht null sein")
        return mediaAttachmentRepository
            .findByNoteId(noteId)
            .map { it.toDto() }
    }

    @Transactional
    fun uploadForNote(
        note: NoteModel,
        fileStream: InputStream,
        originalFilename: String
    ): MediaAttachmentDto {
        // 1) write file to disk (or s3, etc) and get URL
        val url = uploadService.saveFile(fileStream, originalFilename)

        // 2) create & persist DB entity
        val attachment = MediaAttachmentModel().apply {
            this.note = note
            this.url = url
            this.caption = originalFilename
            // optionally infer type from extension
        }
        // since NoteModel has cascade ALL, either:
        // note.attachments.add(attachment); note.persist()
        // or persist directly:
        attachment.persist()

        return attachment.toDto()
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
        val att = noteAttachmentRepository.findById(attachmentId) ?: return false
        // optionally: uploadService.deleteFile(att.url)
        att.delete()
        return true
    }
}
