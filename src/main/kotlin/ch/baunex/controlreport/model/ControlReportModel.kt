package ch.baunex.controlreport.model

import ch.baunex.notes.model.NoteModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.serialization.LocalDateSerializer
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "control_reports")
class ControlReportModel : PanacheEntity() {

    var reportNumber: Int? = null

    @Serializable(with = LocalDateSerializer::class)
    var controlDate: LocalDate? = null

    var pageCount: Int = 1
    var currentPage: Int = 1

    // Verknüpfte Entitäten
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    lateinit var project: ProjectModel

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id", nullable = false)
//    lateinit var customer: CustomerModel

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id")
    var employee: EmployeeModel? = null

    // Client information (wird von Customer kopiert)
//    var clientType: String? = null
//    var clientName: String? = null
//    var clientStreet: String? = null
//    var clientPostalCode: String? = null
//    var clientCity: String? = null

    // Auftragnehmer (z.B. Kontrollorgan)
    var contractorType: ContractorType? = ContractorType.CONTROL_ORGAN
    var contractorCompany: String? = null
    var contractorStreet: String? = null
    var contractorPostalCode: String? = null
    var contractorCity: String? = null

    // Installationsort (wird von Project kopiert)
//    var installationStreet: String? = null
//    var installationPostalCode: String? = null
//    var installationCity: String? = null
//    var parcelNumber: String? = null
//    var buildingType: String? = null

    // Kontrollinformationen
    var controlScope: String? = null
    var hasDefects: Boolean = false
    var deadlineNote: String? = null
    var generalNotes: String? = null

    @OneToMany(mappedBy = "controlReport", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var defectPositions: MutableList<DefectPositionModel> = mutableListOf()

    @OneToMany(mappedBy = "controlReport", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<NoteModel> = mutableListOf()

    // Abschlussinformationen
    var defectResolverNote: String? = null
    var completionDate: LocalDateTime? = null

    // Metadaten
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    fun onCreate() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
