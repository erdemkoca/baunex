package ch.baunex.controlreport.model

import ch.baunex.notes.model.NoteModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "defect_positions")
class DefectPositionModel : PanacheEntity() {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", unique = true)
    lateinit var note: NoteModel

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_report_id")
    var controlReport: ControlReportModel? = null

    /** The sequential position number within its report */
    var positionNumber: Int = 0

    /** Free‐form description (mirrors note.content) */
    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @ElementCollection
    @CollectionTable(
        name = "defect_position_norm_references",
        joinColumns = [JoinColumn(name = "defect_position_id")]
    )
    @Column(name = "norm_reference")
    var normReferences: MutableList<String> = mutableListOf()

    /** When this DefectPosition was created */
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    /** When it was last updated (optional) */
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

//    companion object {
//        /** Finder: all defect-positions for a given report */
//        fun findByControlReportId(reportId: Long) =
//            list("controlReport.id", reportId)
//    }
}
