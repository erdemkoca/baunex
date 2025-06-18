package ch.baunex.controlreport.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.EmployeeModel
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "control_reports")
class ControlReportModel : PanacheEntity() {

    var reportNumber: String? = null

    @Serializable(with = LocalDateSerializer::class)
    var controlDate: LocalDate? = null

    var pageCount: Int = 1
    var currentPage: Int = 1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    // Client information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: CustomerModel? = null

    // Contractor information
    @Enumerated(EnumType.STRING)
    var contractorType: ContractorType = ContractorType.CONTROL_ORGAN
    var contractorCompany: String? = null
    var contractorStreet: String? = null
    var contractorPostalCode: String? = null
    var contractorCity: String? = null

    // Installation location
    var installationStreet: String? = null
    var installationPostalCode: String? = null
    var installationCity: String? = null
    var parcelNumber: String? = null

    // Control data
    var controlScope: String? = null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id")
    var employee: EmployeeModel? = null

    var hasDefects: Boolean = false
    var deadlineNote: String? = null
    var generalNotes: String? = null

    @OneToMany(mappedBy = "controlReport", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var defectPositions: MutableList<DefectPositionModel> = mutableListOf()

    @OneToMany(mappedBy = "controlReport", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<NoteModel> = mutableListOf()

    // Completion information
    var defectResolverNote: String? = null
    var completionDate: LocalDateTime? = null

    // Metadata
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
