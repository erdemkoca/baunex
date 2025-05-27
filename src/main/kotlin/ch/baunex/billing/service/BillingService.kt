package ch.baunex.billing.service

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.dto.CostBreakdownDTO
import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.service.TimeEntryCostService
import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class BillingService @Inject constructor(
    private val timeEntryCostService: TimeEntryCostService,
    private val timeEntryMapper: TimeEntryMapper
) {
    @Inject
    lateinit var projectCatalogItemRepository: ProjectCatalogItemRepository

    @Inject
    lateinit var timeEntryRepository: TimeEntryRepository

    fun calculateBilling(projectId: Long): BillingDTO {
        // Fetch models
        val materialModels: List<ProjectCatalogItemModel> =
            projectCatalogItemRepository.find("project.id", projectId).list()

        val timeEntryModels: List<TimeEntryModel> =
            timeEntryRepository.find("project.id", projectId).list()

        // Map to DTOs
        val materialDTOs = materialModels.map { item -> item.toProjectCatalogItemDTO() }
        val timeEntryDTOs = timeEntryModels.map { entry -> timeEntryMapper.toTimeEntryResponseDTO(entry) }

        // Calculate totals
        val materialTotal = materialDTOs.sumOf { it.totalPrice }
        
        // Calculate cost breakdown
        var totalServiceCost = 0.0
        var totalSurcharges = 0.0
        var totalAdditionalCosts = 0.0
        var totalCatalogItemsCost = 0.0

        timeEntryDTOs.forEach { entry ->
            entry.costBreakdown?.let { breakdown ->
                totalServiceCost += breakdown.totalServiceCost
                totalSurcharges += breakdown.totalSurcharges
                totalAdditionalCosts += breakdown.totalAdditionalCosts
                totalCatalogItemsCost += breakdown.catalogItemsCost
            }
        }

        val totalCatalogItemsAndMaterials = totalCatalogItemsCost + materialTotal

        val costBreakdown = CostBreakdownDTO(
            totalServiceCost = totalServiceCost,
            totalSurcharges = totalSurcharges,
            totalAdditionalCosts = totalAdditionalCosts,
            totalCatalogItemsCost = totalCatalogItemsCost,
            totalSurchargesSum = totalSurcharges,
            totalAdditionalCostsSum = totalAdditionalCosts,
            totalCatalogItemsAndMaterials = totalCatalogItemsAndMaterials
        )

        return BillingDTO(
            projectId = projectId,
            materials = materialDTOs,
            timeEntries = timeEntryDTOs,
            materialTotal = materialTotal,
            timeTotal = totalServiceCost,
            total = totalCatalogItemsAndMaterials + totalServiceCost,
            costBreakdown = costBreakdown
        )
    }
}
