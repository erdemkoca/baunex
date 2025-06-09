package ch.baunex.documentGenerator.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*

@Entity
@Table(name = "document_entries")
class DocumentEntryModel : PanacheEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    lateinit var document: DocumentModel

    var type: String? = null

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    var quantity: Double? = null
    var price: Double? = null
    var total: Double? = null
}