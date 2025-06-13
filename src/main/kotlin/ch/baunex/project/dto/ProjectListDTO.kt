package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectListDTO(
    val id: Long,
    val name: String,
    val customerId: Long,
    val customerName: String,
    val status: String,
    val budget: Int? = null,
    @Contextual val startDate: LocalDate? = null,
    @Contextual val endDate: LocalDate? = null
)
