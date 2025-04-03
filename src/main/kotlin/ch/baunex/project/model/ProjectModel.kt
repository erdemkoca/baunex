package ch.baunex.project.model

import ch.baunex.project.dto.ProjectDTO
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

}

fun ProjectModel.toDTO(): ProjectDTO = ProjectDTO(
    id = this.id,
    name = this.name,
    budget = this.budget,
    client = this.client,
    contact = this.contact
)
