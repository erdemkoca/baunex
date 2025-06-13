package ch.baunex.documentGenerator.facade

import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.dto.GenericDocumentDTO
import ch.baunex.documentGenerator.service.GenericDocumentService
import ch.baunex.documentGenerator.mapper.GenericDocumentMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class GenericDocumentFacade @Inject constructor(
    private val service: GenericDocumentService,
    private val mapper: GenericDocumentMapper
) {
    fun getById(id: Long): DocumentResponseDTO =
        service.findById(id).let(mapper::toResponseDTO)

    fun listAll(): List<DocumentResponseDTO> =
        service.listAll().map(mapper::toResponseDTO)

    fun create(dto: GenericDocumentDTO): DocumentResponseDTO =
        mapper.toModel(dto).let(service::save).let(mapper::toResponseDTO)

    fun update(id: Long, dto: GenericDocumentDTO): DocumentResponseDTO {
        val existing = service.findById(id)
        val updated  = mapper.toModel(dto).apply { this.id = id }
        return service.update(existing, updated).let(mapper::toResponseDTO)
    }

    fun delete(id: Long) {
        service.delete(id)
    }
}
