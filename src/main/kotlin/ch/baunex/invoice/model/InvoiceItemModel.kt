package ch.baunex.invoice.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "invoice_items")
class InvoiceItemModel : PanacheEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @JsonIgnore
    lateinit var invoice: InvoiceModel

    var description: String? = ""

    // "VA" (Verrechnete Arbeit) oder "IC" (Material/Katalog)
    var type: String = ""

    var quantity: Double = 0.0
    var price: Double = 0.0
    var total: Double = 0.0

    // Referenzen auf Originaldaten, falls benötigt
    var timeEntryId: Long? = null
    var projectCatalogItemId: Long? = null
}
