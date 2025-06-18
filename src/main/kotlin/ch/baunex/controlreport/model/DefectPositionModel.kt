package ch.baunex.controlreport.model

import ch.baunex.notes.model.NoteModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "defect_positions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_dp_report_position",
            columnNames = ["control_report_id", "position_number"]
        )
    ]
)
class DefectPositionModel : PanacheEntity() {

    @OneToOne(
        fetch = FetchType.LAZY,
        optional = false,
        cascade = [CascadeType.ALL],     // or REMOVE/orphanRemoval if you want
        orphanRemoval = true
    )
    @JoinColumn(name = "note_id", unique = true)
    lateinit var note: NoteModel

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_report_id")
    var controlReport: ControlReportModel? = null

    /** The sequential position number within its report */
    @Column(name = "position_number", nullable = false)
    var positionNumber: Int = 0

    @ElementCollection
    @CollectionTable(
        name = "defect_position_norm_references",
        joinColumns = [JoinColumn(name = "defect_position_id")]
    )
    @Column(name = "norm_reference")
    var normReferences: MutableList<String> = mutableListOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at")
    lateinit var updatedAt: LocalDateTime
}
