package ch.baunex.documentGenerator.facade

import ch.baunex.documentGenerator.dto.DocumentDTO
import ch.baunex.documentGenerator.mapper.DocumentMapper
import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.service.DocumentService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class DocumentFacade {

    @Inject
    lateinit var documentService: DocumentService

    @Inject
    lateinit var documentMapper: DocumentMapper

    fun createDocument(dto: DocumentDTO): DocumentModel {
        val model = documentMapper.toDocumentModel(dto)
        return documentService.saveDocument(model)
    }

    fun getDocumentById(id: Long): DocumentModel {
        return documentService.getDocumentById(id)
    }

    fun updateDocument(id: Long, dto: DocumentDTO): DocumentModel {
        val existing = documentService.getDocumentById(id)
        val updated = documentMapper.toDocumentModel(dto)
        updated.id = existing.id // Preserve ID
        return documentService.updateDocument(existing, updated)
    }

    fun deleteDocument(id: Long) {
        documentService.deleteDocument(id)
    }

}
