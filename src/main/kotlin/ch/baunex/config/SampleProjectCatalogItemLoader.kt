package ch.baunex.config.sample

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.catalog.facade.ProjectCatalogItemFacade
import ch.baunex.project.facade.ProjectFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleProjectCatalogItemLoader {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var projectCatalogItemFacade: ProjectCatalogItemFacade

    @Transactional
    fun load() {
        val projects = projectFacade.getAllProjects()
        val catalogItems = catalogFacade.getAllItems()

        if (projects.isEmpty() || catalogItems.isEmpty()) return

        val efhProject = projects.find { it.name.contains("EFH") }
        val garageProject = projects.find { it.name.contains("Ladestation") }

        val steckdose = catalogItems.find { it.name.contains("Steckdose") }
        val schalter = catalogItems.find { it.name.contains("Lichtschalter") }
        val kabel = catalogItems.find { it.name.contains("Installationskabel") }
        val ledPanel = catalogItems.find { it.name.contains("LED Panel") }
        val automat = catalogItems.find { it.name.contains("Sicherungsautomat") }

        if (efhProject != null && steckdose != null && schalter != null && kabel != null) {
            val efhProjectId = efhProject.id
            listOf(
                ProjectCatalogItemDTO(
                    projectId = efhProjectId,
                    itemName = steckdose.name,
                    quantity = 12,
                    unitPrice = steckdose.unitPrice,
                    totalPrice = 12 * steckdose.unitPrice,
                    catalogItemId = steckdose.id
                ),
                ProjectCatalogItemDTO(
                    projectId = efhProjectId,
                    itemName = schalter.name,
                    quantity = 10,
                    unitPrice = schalter.unitPrice,
                    totalPrice = 10 * schalter.unitPrice,
                    catalogItemId = schalter.id
                ),
                ProjectCatalogItemDTO(
                    projectId = efhProjectId,
                    itemName = kabel.name,
                    quantity = 100,
                    unitPrice = kabel.unitPrice,
                    totalPrice = 100 * kabel.unitPrice,
                    catalogItemId = kabel.id
                )
            ).forEach { projectCatalogItemFacade.addItemToProject(efhProjectId, it) }
        }

        if (garageProject != null && automat != null && ledPanel != null) {
            val garageProjectId = garageProject.id
            listOf(
                ProjectCatalogItemDTO(
                    projectId = garageProjectId,
                    itemName = automat.name,
                    quantity = 3,
                    unitPrice = automat.unitPrice,
                    totalPrice = 3 * automat.unitPrice,
                    catalogItemId = automat.id
                ),
                ProjectCatalogItemDTO(
                    projectId = garageProjectId,
                    itemName = ledPanel.name,
                    quantity = 6,
                    unitPrice = ledPanel.unitPrice,
                    totalPrice = 6 * ledPanel.unitPrice,
                    catalogItemId = ledPanel.id
                )
            ).forEach { projectCatalogItemFacade.addItemToProject(garageProjectId, it) }
        }
    }
}
