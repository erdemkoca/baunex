package ch.baunex.catalog.repository

import ch.baunex.catalog.model.CatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CatalogRepository : PanacheRepository<CatalogItemModel> {
    
    fun listAllCatalogItems(): List<CatalogItemModel> =
        find("FROM CatalogItemModel c").list()
}
