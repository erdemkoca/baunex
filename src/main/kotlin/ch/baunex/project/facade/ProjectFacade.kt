package ch.baunex.project.facade

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.AttachmentForUI
import ch.baunex.notes.dto.NoteForUI
import ch.baunex.notes.mapper.toAttachmentForUI
import ch.baunex.notes.mapper.toDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.notes.model.NoteModel
import ch.baunex.project.dto.*
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.project.service.ProjectService
import ch.baunex.user.dto.EmployeeReferenceDTO
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class ProjectFacade @Inject constructor(
    private val projectService: ProjectService,
    private val projectMapper: ProjectMapper,
    private val employeeService: EmployeeService,
    private val employeeFacade: EmployeeFacade
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
        tags: List<String>
    ) {
        val project = projectService.getProjectWithEntries(projectId)
            ?: throw IllegalArgumentException("Projekt nicht gefunden")
        val adminId = employeeFacade.findByRole(Role.ADMIN).id
        val creator = employeeService.getEmployee(adminId) //TOOO Right now hardcoded as Admin (JWT maybe)
            ?: throw IllegalArgumentException("Mitarbeiter nicht gefunden")

        val note = NoteModel().apply {
            this.title = title
            this.category = category
            this.content = content
            this.tags = tags
            this.createdAt = LocalDate.now()
            this.createdBy = creator
            this.project = project
        }
        project.notes.add(note)
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

    @Transactional
    fun getProjectNotesView(id: Long): ProjectNotesViewDTO {
        val project = projectService.getProjectWithEntries(id)
            ?: throw IllegalArgumentException("Projekt mit ID $id nicht gefunden")

        val employees = employeeFacade.listAll()
            .map { EmployeeReferenceDTO(it.id, it.firstName, it.lastName) }
        val categories = NoteCategory.values().map { it.name }

        // 1) Projekt-Notizen
        val projNotes = project.notes.map { note ->
            NoteForUI(
                id          = note.id,
                title       = note.title,
                content     = note.content,
                category    = note.category,
                tags        = note.tags,
                createdById = note.createdBy.id,
                createdAt   = note.createdAt,
                attachments = note.attachments.map { model ->
                    val dto = model.toDto()
                    dto.toAttachmentForUI() },
                source      = "project"
            )
        }

        // 2) TimeEntry-Notizen
        val teNotes = project.timeEntries.flatMap { entry ->
            entry.notes.map { note ->
                NoteForUI(
                    id          = note.id,
                    title       = note.title,
                    content     = note.content,
                    category    = note.category,
                    tags        = note.tags,
                    createdById = note.createdBy.id,
                    createdAt   = note.createdAt,
                    attachments = note.attachments.map { model ->
                        val dto = model.toDto()
                        dto.toAttachmentForUI() },
                    source      = "timeEntry",
                    entryId     = entry.id,
                    entryDate   = entry.date,
                    entryTitle  = entry.title
                )
            }
        }

        val allNotes = (projNotes + teNotes)
            .sortedByDescending { it.createdAt }  // optional: nach Datum sortieren

        return ProjectNotesViewDTO(
            projectId   = project.id,
            projectName = project.name,
            categories  = categories,
            employees   = employees,
            notes       = allNotes
        )
    }
}