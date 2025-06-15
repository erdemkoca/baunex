package ch.baunex.controlreport.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.user.model.CustomerModel
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "control_reports")
class ControlReportModel : PanacheEntity() {

    var reportNumber: String? = null

    @Serializable(with = LocalDateSerializer::class)
    var controlDate: LocalDateTime? = null

    var pageCount: Int = 1
    var currentPage: Int = 1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    // Client information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: CustomerModel? = null
    var clientType: ClientType? = ClientType.OWNER

    // Contractor information
    @Enumerated(EnumType.STRING)
    var contractorType: ContractorType? = null
    var contractorCompany: String? = null
    var contractorStreet: String? = null
    var contractorHouseNumber: String? = null
    var contractorPostalCode: String? = null
    var contractorCity: String? = null

    // Installation location
    var installationStreet: String? = null
    var installationHouseNumber: String? = null
    var installationPostalCode: String? = null
    var installationCity: String? = null
    var parcelNumber: String? = null

    // Control data
    var controlScope: String? = null
    var controllerName: String? = null
    var controllerPhone: String? = null
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
    var companyStamp: String? = null
    var completionSignature: String? = null

    // Metadata
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
