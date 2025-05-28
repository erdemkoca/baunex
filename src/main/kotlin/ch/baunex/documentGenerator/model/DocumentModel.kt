package ch.baunex.documentGenerator.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "documents")
class DocumentModel : PanacheEntity() {

    @Enumerated(EnumType.STRING)
    lateinit var type: DocumentType

    lateinit var customerName: String

    var markdownHeader: String? = null
    var markdownFooter: String? = null
    var createdAt: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entries: MutableList<DocumentEntryModel> = mutableListOf()
}
