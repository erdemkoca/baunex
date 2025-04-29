package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectUpdateDTO(
    val name: String? = null,
    val customerId: Long? = null,
    val budget: Int? = null,
    @Contextual val startDate: LocalDate? = null,
    @Contextual val endDate: LocalDate? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
    val street: String? = null,
    val city: String? = null,
    val contact: String? = null
)
