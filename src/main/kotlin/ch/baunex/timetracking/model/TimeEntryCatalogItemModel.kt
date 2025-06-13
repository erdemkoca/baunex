package ch.baunex.timetracking.model

import ch.baunex.catalog.model.CatalogItemModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "time_entry_catalog_items")
class TimeEntryCatalogItemModel : PanacheEntity() {
    @ManyToOne
    @JoinColumn(name = "time_entry_id")
    lateinit var timeEntry: TimeEntryModel

    @ManyToOne
    @JoinColumn(name = "catalog_item_id")
    lateinit var catalogItem: CatalogItemModel

    @Column(nullable = false)
    var quantity: Int = 1  // Quantity used during this time entry

    @Column(nullable = false)
    var unitPrice: Double = 0.0  // Price at the time of entry

    val totalPrice: Double
        get() = quantity * unitPrice
}