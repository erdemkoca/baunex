package ch.baunex.catalog.facade

import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.mapper.toProjectCatalogItemDTO
import ch.baunex.catalog.mapper.toProjectCatalogItemModel
import ch.baunex.catalog.service.ProjectCatalogItemService
import ch.baunex.project.service.ProjectService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ProjectCatalogItemFacade @Inject constructor(
    private val service: ProjectCatalogItemService,
    private val projectService: ProjectService
) {
    fun getItemsForProject(projectId: Long): List<ProjectCatalogItemDTO> =
        service.getByProjectId(projectId)
            .map { it.toProjectCatalogItemDTO() }

    fun addItemToProject(projectId: Long, dto: ProjectCatalogItemDTO) {
        val project = projectService.getProjectWithEntries(projectId)
            ?: throw IllegalArgumentException("Project mit ID $projectId nicht gefunden")
        service.save(dto.toProjectCatalogItemModel(project))
    }

    fun updateItem(id: Long, dto: ProjectCatalogItemDTO) {
        val projId = dto.projectId
        val project = projectService.getProjectWithEntries(projId)
            ?: throw IllegalArgumentException("Project mit ID $projId nicht gefunden")
        service.update(id, dto.toProjectCatalogItemModel(project))
    }

    fun deleteItem(id: Long) {
        service.delete(id)
    }
}
