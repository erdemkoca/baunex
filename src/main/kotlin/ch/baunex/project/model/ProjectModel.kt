package ch.baunex.project.model

import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.CustomerModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "projects")
class ProjectModel : PanacheEntity() {

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    lateinit var customer: CustomerModel

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    var budget: Int = 0

    @Column(name = "start_date")
    var startDate: LocalDate? = null

    @Column(name = "end_date")
    var endDate: LocalDate? = null

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ProjectStatus = ProjectStatus.PLANNED

    var street: String? = null
    var city: String? = null

    // --- RELATIONS ---

    // Time entries
    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var timeEntries: MutableList<TimeEntryModel> = mutableListOf()

    // Catalog items used in this project (services or products)
    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var usedItems: MutableList<ProjectCatalogItemModel> = mutableListOf()

    @Column(name = "project_number", unique = true, nullable = false)
    var projectNumber: Int = 0

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<NoteModel> = mutableListOf()

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var buildingType: ProjectType = ProjectType.DIVERSE
}
