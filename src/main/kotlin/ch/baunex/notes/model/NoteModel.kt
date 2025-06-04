package ch.baunex.notes.model

import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "note")
class NoteModel : PanacheEntity() {

    // id kommt von PanacheEntity: `@Id @GeneratedValue var id: Long? = null`

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    var project: ProjectModel? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_entry_id", nullable = true)
    var timeEntry: TimeEntryModel? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    var invoice: InvoiceModel? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: DocumentModel? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id")
    lateinit var createdBy: EmployeeModel

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @Column(name = "title")
    var title: String? = null

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    var category: NoteCategory = NoteCategory.INFO

    @Column(name = "tags", columnDefinition = "TEXT[]")
    var tags: List<String> = emptyList()

    @OneToMany(mappedBy = "note", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var attachments: MutableList<MediaAttachmentModel> = mutableListOf()
}
