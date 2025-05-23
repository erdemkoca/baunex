package ch.baunex.billing.service

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.mapper.toTimeEntryResponseDTO
import ch.baunex.timetracking.service.TimeEntryCostService
import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import ch.baunex.timetracking.repository.TimeEntryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class BillingService @Inject constructor(
    private val timeEntryCostService: TimeEntryCostService
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
        val timeEntryDTOs = timeEntryModels.map { entry -> entry.toTimeEntryResponseDTO() }

        // Totals
        val materialTotal = materialDTOs.sumOf { it.totalPrice }
        val timeTotal = timeEntryDTOs.sumOf { (it.hourlyRate ?: 0.0) * it.hoursWorked }

        return BillingDTO(
            projectId = projectId,
            materials = materialDTOs,
            timeEntries = timeEntryDTOs,
            materialTotal = materialTotal,
            timeTotal = timeTotal,
            total = materialTotal + timeTotal
        )
    }
}
