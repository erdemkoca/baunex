package ch.baunex.project.dto

import ch.baunex.project.model.ProjectStatus
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectListDTO(
    val id: Long,
    val name: String,
    val projectNumberFormatted: String,
    val customerId: Long,
    val customerName: String,
    val status: String,
    val budget: Int? = null,
    val parcelNumber: String? = "",
    @Serializable(with = LocalDateSerializer::class) val startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val endDate: LocalDate? = null
)
