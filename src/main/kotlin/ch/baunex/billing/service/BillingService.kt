package ch.baunex.billing.service

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.dto.CostBreakdownDTO
import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.model.ProjectCatalogItemModel
import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import ch.baunex.timetracking.mapper.TimeEntryMapper
import ch.baunex.timetracking.model.TimeEntryModel
import ch.baunex.timetracking.repository.TimeEntryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class BillingService @Inject constructor(
    private val timeEntryMapper: TimeEntryMapper
) {

    @Inject
    lateinit var projectCatalogItemRepository: ProjectCatalogItemRepository

    @Inject
    lateinit var timeEntryRepository: TimeEntryRepository

    fun calculateBilling(projectId: Long): BillingDTO {
        // 1) Roh-Daten mit generischem Typ:
        val materialModels: List<ProjectCatalogItemModel> =
            projectCatalogItemRepository.find("project.id", projectId).list()

        val timeEntryModels: List<TimeEntryModel> =
            timeEntryRepository.find("project.id", projectId).list()

        // 2) Map zu Response-DTOs
        val materialDTOs = materialModels.map { it.toProjectCatalogItemDTO() }
        val entries = timeEntryModels.map { entry -> timeEntryMapper.toTimeEntryResponseDTO(entry) }

        // 3) Alle Katalog-Zeilen aus den TimeEntries
        val timeEntryCatalogItems: List<ProjectCatalogItemDTO> = entries.flatMap { resp ->
            resp.catalogItems.map { item ->
                ProjectCatalogItemDTO(
                    id            = item.id,
                    projectId     = projectId,
                    itemName      = "${item.itemName} (${resp.date})",
                    quantity      = item.quantity,
                    unitPrice     = item.unitPrice,
                    totalPrice    = item.totalPrice,
                    catalogItemId = item.catalogItemId
                )
            }
        }

        // 4) Alles zusammenführen
        val allMaterials = materialDTOs + timeEntryCatalogItems

        // 5) Material-Summe
        val materialTotal: Double = allMaterials
            .mapNotNull { it.totalPrice }
            .sum()

        // 6) Kosten-Breakdown aufsummieren
        var totalServiceCost      = 0.0
        var totalSurcharges       = 0.0
        var totalAdditionalCosts  = 0.0
        var totalCatalogItemsCost = 0.0

        for (resp in entries) {
            resp.costBreakdown?.let { bd ->
                totalServiceCost      += bd.totalServiceCost
                totalSurcharges       += bd.totalSurcharges
                totalAdditionalCosts  += bd.totalAdditionalCosts
                totalCatalogItemsCost += bd.catalogItemsCost
            }
        }

        val totalCatalogAndMaterials = totalCatalogItemsCost + materialTotal

        val costBreakdown = CostBreakdownDTO(
            totalServiceCost             = totalServiceCost,
            totalSurcharges              = totalSurcharges,
            totalAdditionalCosts         = totalAdditionalCosts,
            totalCatalogItemsCost        = totalCatalogItemsCost,
            totalCatalogItemsAndMaterials = totalCatalogAndMaterials
        )

        // 7) Ergebnis zusammenbauen
        return BillingDTO(
            projectId     = projectId,
            materials     = allMaterials,
            timeEntries   = entries,
            materialTotal = materialTotal,
            timeTotal     = totalServiceCost,
            total         = totalServiceCost + totalCatalogAndMaterials,
            costBreakdown = costBreakdown
        )
    }
}
