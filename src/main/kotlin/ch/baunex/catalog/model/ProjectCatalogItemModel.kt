package ch.baunex.catalog.model

import ch.baunex.project.model.ProjectModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "project_catalog_items")
class ProjectCatalogItemModel : PanacheEntity() {

    @ManyToOne
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    @Column(nullable = false)
    lateinit var itemName: String

    var quantity: Int = 1

    var unitPrice: Double = 0.0

    val totalPrice: Double
        get() = quantity * unitPrice
}
