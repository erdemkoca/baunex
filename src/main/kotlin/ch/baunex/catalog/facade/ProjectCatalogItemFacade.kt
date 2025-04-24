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
        service.getByProjectId(projectId).map { it.toProjectCatalogItemDTO() }

    fun addItemToProject(projectId: Long, dto: ProjectCatalogItemDTO) {
        val project = projectService.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")
        service.save(dto.toProjectCatalogItemModel(project))
    }

    fun updateItem(id: Long, dto: ProjectCatalogItemDTO) {
        val project = projectService.getProjectById(dto.projectId) ?: throw IllegalArgumentException("Project not found")
        service.update(id, dto.toProjectCatalogItemModel(project))
    }

    fun deleteItem(id: Long) {
        service.delete(id)
    }
}
