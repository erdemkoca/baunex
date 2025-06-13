package ch.baunex.catalog.facade

import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.mapper.toCatalogItemDTO
import ch.baunex.catalog.mapper.toCatalogItemModel
import ch.baunex.catalog.service.CatalogService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CatalogFacade @Inject constructor(
    private val service: CatalogService
) {
    fun getAllItems(): List<CatalogItemDTO> = service.getAll().map { it.toCatalogItemDTO() }

    fun getItemById(id: Long): CatalogItemDTO? = service.findById(id)?.toCatalogItemDTO()

    fun createItem(dto: CatalogItemDTO): CatalogItemDTO {
        val model = dto.toCatalogItemModel()
        service.save(model)
        return model.toCatalogItemDTO()
    }

    fun updateItem(id: Long, dto: CatalogItemDTO) = service.update(id, dto.toCatalogItemModel())

    fun deleteItem(id: Long) = service.delete(id)
}
