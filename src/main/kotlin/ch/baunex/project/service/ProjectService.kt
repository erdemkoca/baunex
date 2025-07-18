package ch.baunex.project.service

import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.dto.ProjectUpdateDTO
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.service.CustomerService
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val customerService: CustomerService,
    private val mapper: ProjectMapper,
    private val projectMapper: ProjectMapper
) {

    @Transactional
    fun createProject(dto: ProjectCreateDTO): ProjectModel {
        val customer = customerService.findCustomerModelById(dto.customerId)
            ?: throw IllegalArgumentException("Kein Kunde mit ID ${dto.customerId}")
        val project = projectMapper.createModel(dto, customer).apply {}
        project.projectNumber = generateNextProjectNumber()
        projectRepository.persist(project)
        return project
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectUpdateDTO): ProjectModel? {
        val existing = projectRepository.findById(id) ?: return null
        // Customer wechseln, falls gesetzt
        dto.customerId?.let { newCid ->
            val cust = customerService.findCustomerModelById(newCid)
                ?: throw IllegalArgumentException("Kein Kunde mit ID $newCid")
            existing.customer = cust
        }
        // Rest-Felder mappen
        projectMapper.updateModel(existing, dto)
        return existing
    }

    fun getAllProjects(): List<ProjectModel> = projectRepository.listAllProjects()
    fun getProjectWithEntries(id: Long): ProjectModel? = projectRepository.findByIdWithoutTimeEntries(id)
    
    fun getProjectWithoutEntries(id: Long): ProjectModel? = projectRepository.findByIdWithoutTimeEntries(id)
    @Transactional
    fun deleteProject(id: Long): Boolean = projectRepository.deleteById(id)

    fun getAll(): List<ProjectDTO> {
        return projectRepository.listAllProjects().map { mapper.toDTO(it) }
    }

    fun getById(id: Long): ProjectDTO? {
        return projectRepository.findById(id)?.let { mapper.toDTO(it) }
    }

    @Transactional
    fun create(project: ProjectDTO): ProjectDTO {
        val entity = mapper.toEntity(project)
        projectRepository.persist(entity)
        return mapper.toDTO(entity)
    }

    @Transactional
    fun update(id: Long, project: ProjectDTO): ProjectDTO? {
        val existing = projectRepository.findById(id) ?: return null
        val updated = mapper.toEntity(project)
        existing.name = updated.name
        existing.description = updated.description
        existing.status = updated.status
        projectRepository.persist(existing)
        return mapper.toDTO(existing)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        return projectRepository.deleteById(id)
    }

    fun generateNextProjectNumber(): Int {
        val maxNumber = projectRepository
            .listAllProjects()
            .map { it.projectNumber }
            .maxOrNull() ?: 1000
        return maxNumber + 1
    }
}
