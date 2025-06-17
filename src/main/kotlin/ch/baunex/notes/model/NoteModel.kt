package ch.baunex.notes.model

import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "note")
class NoteModel : PanacheEntity() {

    // Pflicht-Relation: Jede Note gehört (mindestens) zu einem Projekt
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    lateinit var project: ProjectModel

    // Optional: nur wenn die Note an einen TimeEntry hängt
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "time_entry_id", nullable = true)
    var timeEntry: TimeEntryModel? = null

    // Optional: nur wenn die Note innerhalb eines ControlReports erstellt wurde
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "control_report_id", nullable = true)
    var controlReport: ControlReportModel? = null

    // Verbindung zur automatisch erzeugten DefectPosition (falls category == MÄNGEL)
    @OneToOne(mappedBy = "note", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var defectPosition: DefectPositionModel? = null

    // Optional: Notiz zu einer Rechnung
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "invoice_id", nullable = true)
    var invoice: InvoiceModel? = null

    // Optional: Notiz zu einem generierten Dokument
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "document_id", nullable = true)
    var document: DocumentModel? = null

    // Wer hat die Note geschrieben? Pflichtfeld
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    lateinit var createdBy: EmployeeModel

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDate? = null

    @Column(name = "updated_at")
    var updatedAt: LocalDate? = null

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

    @PrePersist fun onCreate() {
        val now = LocalDate.now()
        createdAt = now
        updatedAt = now
    }
    @PreUpdate fun onUpdate() {
        updatedAt = LocalDate.now()
    }
}
