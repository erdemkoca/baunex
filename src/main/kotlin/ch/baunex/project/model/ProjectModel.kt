package ch.baunex.project.model

import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "projects")
class ProjectModel : PanacheEntity() {

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var client: String

    @Column(nullable = false)
    var budget: Int = 0

    var contact: String? = "No Info"

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

    // Assigned workers
    @ManyToMany
    @JoinTable(
        name = "project_workers",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var assignedWorkers: MutableList<UserModel> = mutableListOf()

    // Time entries
    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var timeEntries: MutableList<TimeEntryModel> = mutableListOf()

    // Catalog items used in this project (services or products)
    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var usedItems: MutableList<ProjectCatalogItemModel> = mutableListOf()
}
