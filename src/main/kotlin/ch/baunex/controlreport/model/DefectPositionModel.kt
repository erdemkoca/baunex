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

    var positionNumber: Int = 0

    @ElementCollection
    @CollectionTable(name = "defect_position_norm_references", joinColumns = [JoinColumn(name = "defect_position_id")])
    @Column(name = "norm_reference")
    var normReferences: MutableList<String> = mutableListOf()

    // Resolution information
    var resolvedAt: LocalDateTime? = null
    var resolutionStamp: String? = null
    var resolutionSignature: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_report_id")
    var controlReport: ControlReportModel? = null
}