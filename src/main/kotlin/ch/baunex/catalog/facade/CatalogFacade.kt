package ch.baunex.catalog.facade

import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.model.toDTO
import ch.baunex.catalog.model.toModel
import ch.baunex.catalog.service.CatalogService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CatalogFacade @Inject constructor(
    private val service: CatalogService
) {
    fun getAllItems(): List<CatalogItemDTO> = service.getAll().map { it.toDTO() }

    fun getItemById(id: Long): CatalogItemDTO? = service.findById(id)?.toDTO()

    fun createItem(dto: CatalogItemDTO): CatalogItemDTO {
        val model = dto.toModel()
        service.save(model)
        return model.toDTO()
    }

    fun updateItem(id: Long, dto: CatalogItemDTO) = service.update(id, dto.toModel())

    fun deleteItem(id: Long) = service.delete(id)
}
