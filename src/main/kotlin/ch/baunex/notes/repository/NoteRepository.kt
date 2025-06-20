package ch.baunex.notes.repository

import ch.baunex.notes.model.NoteModel
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NoteRepository : PanacheRepository<NoteModel> {

    fun findByProjectId(projectId: Long): List<NoteModel> =
        list("project.id", projectId)

    fun findByTimeEntryId(timeEntryId: Long): List<NoteModel> =
        list("timeEntry.id", timeEntryId)

    fun findByDocumentId(documentId: Long): List<NoteModel> =
        list("document.id", documentId)
}
