package ch.baunex.catalog.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "catalog_items")
class CatalogItemModel : PanacheEntity() {

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    var unitPrice: Double = 0.0

    var description: String? = null
}
