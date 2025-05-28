package ch.baunex.documentGenerator.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "document_entries")
class DocumentEntryModel  : PanacheEntity() {
    @ManyToOne
    @JoinColumn(name = "document_id")
    lateinit var document: DocumentModel

    var description: String? = null
    var quantity: Double? = null
    var price: Double? = null
}