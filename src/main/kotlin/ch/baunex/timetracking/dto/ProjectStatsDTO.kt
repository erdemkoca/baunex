package ch.baunex.timetracking.dto

import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ProjectStatsDTO(
    val project: ProjectListDTO,
    val totalHours: Double,
    val employeeCount: Int,
    @Serializable(with = LocalDateSerializer::class) val lastActivity: LocalDate?
) 