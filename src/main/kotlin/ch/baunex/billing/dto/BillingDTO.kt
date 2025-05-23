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
    val total: Double,
    val costBreakdown: CostBreakdownDTO = CostBreakdownDTO()
)

@Serializable
data class CostBreakdownDTO(
    val totalServiceCost: Double = 0.0,
    val totalSurcharges: Double = 0.0,
    val totalAdditionalCosts: Double = 0.0,
    val totalCatalogItemsCost: Double = 0.0,
    val totalSurchargesSum: Double = 0.0,
    val totalAdditionalCostsSum: Double = 0.0,
    val totalCatalogItemsAndMaterials: Double = 0.0
)
