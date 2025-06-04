package ch.baunex.notes.repository

import ch.baunex.notes.model.MediaAttachmentModel
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MediaAttachmentRepository : PanacheRepository<MediaAttachmentModel> {
    fun findByNoteId(noteId: Long): List<MediaAttachmentModel> =
        list("note.id", noteId)
}
