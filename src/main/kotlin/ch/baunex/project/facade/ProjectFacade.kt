package ch.baunex.project.facade

import ch.baunex.project.dto.*
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.project.service.ProjectService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService,
    private val projectMapper: ProjectMapper
) {
    @Transactional
    fun createProject(createDto: ProjectCreateDTO): ProjectDetailDTO {
        val created = projectService.createProject(createDto)
        return projectMapper.toDetailDTO(created)
    }

    @Transactional
    fun updateProject(id: Long, updateDto: ProjectUpdateDTO): Boolean {
        return projectService.updateProject(id, updateDto)?.let { true } ?: false
    }

    fun getAllProjects(): List<ProjectListDTO> {
        return projectService.getAllProjects()
            .map { projectMapper.toListDTO(it) }
    }

    fun getProjectWithDetails(id: Long): ProjectDetailDTO? {
        println("Getting project with ID: $id")
        return projectService.getProjectWithEntries(id)
            ?.let { 
                println("Found project: ${it.name}")
                projectMapper.toDetailDTO(it) 
            }
            ?.also { println("Project not found with ID: $id") }
    }

    @Transactional
    fun deleteProject(id: Long) {
        projectService.deleteProject(id)
    }

    @Transactional
    fun createOrUpdate(id: Long?, createDto: ProjectCreateDTO): Long {
        return if (id != null && id > 0) {
            // mappe vom Create‐ zum Update‐DTO
            val upd = ProjectUpdateDTO(
                name        = createDto.name,
                customerId  = createDto.customerId,
                budget      = createDto.budget,
                startDate   = createDto.startDate,
                endDate     = createDto.endDate,
                description = createDto.description,
                status      = createDto.status,
                street      = createDto.street,
                city        = createDto.city,
                updatedNotes= createDto.initialNotes
            )
            updateProject(id, upd)
            id
        } else {
            val detail = createProject(createDto)
            detail.id
        }
    }
}