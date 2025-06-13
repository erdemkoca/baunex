//// ch.baunex.documentGenerator.facade.DocumentFacade.kt
//package ch.baunex.documentGenerator.facade
//
//import ch.baunex.documentGenerator.service.DocumentService
//import jakarta.enterprise.context.ApplicationScoped
//import ch.baunex.documentGenerator.dto.DocumentResponseDTO
//import ch.baunex.documentGenerator.dto.invoice.InvoiceDocumentRequestDTO
//import ch.baunex.documentGenerator.mapper.InvoiceDocumentMapper
//import jakarta.inject.Inject
//
//@ApplicationScoped
//class DocumentFacade {
//
//    @Inject
//    lateinit var service: DocumentService
//
//    @Inject
//    lateinit var mapper: InvoiceDocumentMapper
//
//    fun getById(id: Long): DocumentResponseDTO {
//        val model = service.getDocumentById(id)
//        return mapper.toResponseDTO(model)
//    }
//
//    fun createDocument(dto: InvoiceDocumentRequestDTO): DocumentResponseDTO {
//        val model = mapper.toModel(dto)
//        val saved = service.saveDocument(model)
//        return mapper.toResponseDTO(saved)
//    }
//
//    fun updateDocument(id: Long, dto: InvoiceDocumentRequestDTO): DocumentResponseDTO {
//        val existing = service.getDocumentById(id)
//        val updatedModel = mapper.toModel(dto).apply { this.id = id }  // ensure ID
//        val saved = service.updateDocument(existing, updatedModel)
//        return mapper.toResponseDTO(saved)
//    }
//
//    fun deleteDocument(id: Long) {
//        service.deleteDocument(id)
//    }
//
//    fun generatePdf(id: Long): ByteArray {
//        // service.getDocumentById returns the model
//        val model = service.getDocumentById(id)
//        return service.generatePdfBytes(model)
//    }
//
//    fun getAll(): List<DocumentResponseDTO> =
//        service.getAllDocuments().map { mapper.toResponseDTO(it) }
//
//    fun getDocument(id: Long): DocumentResponseDTO =
//        getById(id)
//}
