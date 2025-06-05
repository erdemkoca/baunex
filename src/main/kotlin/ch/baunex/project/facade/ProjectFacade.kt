package ch.baunex.project.facade

import ch.baunex.notes.model.NoteCategory
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.dto.*
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.project.service.ProjectService
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService,
    private val projectMapper: ProjectMapper,
    private val employeeService: EmployeeService
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
        return projectService.getProjectWithEntries(id)
            ?.let { projectMapper.toDetailDTO(it) }
    }

    @Transactional
    fun deleteProject(id: Long) {
        projectService.deleteProject(id)
    }

    @Transactional
    fun addNoteToProject(
        projectId: Long,
        title: String?,
        category: NoteCategory,
        content: String,
        tags: List<String>,
        createdById: Long
    ) {
        val project = projectService.getProjectWithEntries(projectId)
            ?: throw IllegalArgumentException("Projekt nicht gefunden")
        val creator = employeeService.getEmployee(createdById)
            ?: throw IllegalArgumentException("Mitarbeiter nicht gefunden")

        val note = NoteModel().apply {
            this.title = title
            this.category = category
            this.content = content
            this.tags = tags
            this.createdAt = LocalDateTime.now()
            this.createdBy = creator
            this.project = project
        }
        project.notes.add(note)
    }

}
