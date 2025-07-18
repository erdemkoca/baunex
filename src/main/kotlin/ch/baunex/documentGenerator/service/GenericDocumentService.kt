package ch.baunex.documentGenerator.service

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.repository.DocumentRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class GenericDocumentService(
    private val documentRepository: DocumentRepository
) {
    fun findById(id: Long): DocumentModel =
        documentRepository.findByIdWithoutEntries(id)
            ?: throw NotFoundException("Document $id not found")

    fun listAll(): List<DocumentModel> =
        documentRepository.listAll()

    @Transactional
    fun save(doc: DocumentModel): DocumentModel {
        doc.persist()
        doc.entries.forEach { it.document = doc; it.persist() }
        return doc
    }

    @Transactional
    fun update(existing: DocumentModel, updated: DocumentModel): DocumentModel {
        // Kopiere nur gemeinsame Felder
        existing.headerMarkdown = updated.headerMarkdown
        existing.footerMarkdown = updated.footerMarkdown
        // Einträge austauschen
        existing.entries.apply {
            clear()
            addAll(updated.entries.map { it.apply { document = existing } })
        }
        return existing
    }

    @Transactional
    fun delete(id: Long) {
        findById(id).delete()
    }
}
