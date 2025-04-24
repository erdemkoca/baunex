package ch.baunex.billing.dto

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.timetracking.dto.TimeEntryResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class BillingDTO(
    val projectId: Long?,
    val materials: List<ProjectCatalogItemDTO>,
    val timeEntries: List<TimeEntryResponseDTO>,
    val materialTotal: Double,
    val timeTotal: Double,
    val total: Double
)
