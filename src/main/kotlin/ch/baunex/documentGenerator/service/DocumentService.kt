package ch.baunex.documentGenerator.service

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.mapper.DocumentMapper
import ch.baunex.documentGenerator.repository.DocumentRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class DocumentService {

    @Inject
    lateinit var documentMapper: DocumentMapper

    @Inject
    lateinit var documentRepository: DocumentRepository

    fun saveDocument(doc: DocumentModel): DocumentModel {
        doc.persist()
        doc.entries.forEach { it.persist() }
        return doc
    }

    fun getDocumentById(id: Long): DocumentModel =
        documentRepository  .findById(id) ?: throw NotFoundException("Document $id not found")

    @Transactional
    fun updateDocument(existing: DocumentModel, updated: DocumentModel): DocumentModel {
        existing.customerName = updated.customerName
        existing.markdownHeader = updated.markdownHeader
        existing.markdownFooter = updated.markdownFooter
        existing.type = updated.type

        // Update entries by replacing
        existing.entries.clear()
        existing.entries.addAll(updated.entries.map {
            it.document = existing
            it
        })

        return existing
    }

    @Transactional
    fun deleteDocument(id: Long) {
        val doc = getDocumentById(id)
        doc.delete()
    }

}
