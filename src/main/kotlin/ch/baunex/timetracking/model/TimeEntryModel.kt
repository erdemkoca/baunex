package ch.baunex.timetracking.model

import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.UserModel
import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "time_entries")
class TimeEntryModel : PanacheEntity() {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    lateinit var user: UserModel

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    lateinit var project: ProjectModel

    @Column(nullable = false)
    lateinit var date: String

    @Column(nullable = false)
    var hoursWorked: Double = 0.0

    @Column(columnDefinition = "TEXT")
    var note: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
