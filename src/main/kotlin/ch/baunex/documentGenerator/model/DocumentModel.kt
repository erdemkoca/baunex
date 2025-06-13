package ch.baunex.documentGenerator.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)    // or SINGLE_TABLE if you prefer
@Table(name = "documents")
abstract class DocumentModel : PanacheEntity() {
    @Enumerated(EnumType.STRING)
    lateinit var type: DocumentType

    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(columnDefinition = "TEXT")
    var headerMarkdown: String? = null

    @Column(columnDefinition = "TEXT")
    var footerMarkdown: String? = null

    // entries still generic
    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entries: MutableList<DocumentEntryModel> = mutableListOf()
}
