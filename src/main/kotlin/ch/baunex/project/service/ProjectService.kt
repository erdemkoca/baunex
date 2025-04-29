package ch.baunex.project.service

import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.dto.ProjectUpdateDTO
import ch.baunex.project.mapper.applyTo
import ch.baunex.project.mapper.toModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.service.CustomerService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val customerService: CustomerService
) {

    @Transactional
    fun createProject(dto: ProjectCreateDTO): ProjectModel {
        val customer = customerService.findCustomerById(dto.customerId)
            ?: throw IllegalArgumentException("Kein Kunde mit ID ${dto.customerId}")
        val project = dto.toModel(customer)
        projectRepository.persist(project)
        return project
    }

    @Transactional
    fun updateProject(id: Long, dto: ProjectUpdateDTO): ProjectModel? {
        val existing = projectRepository.findById(id) ?: return null
        // Customer wechseln, falls gesetzt
        dto.customerId?.let { newCid ->
            val cust = customerService.findCustomerById(newCid)
                ?: throw IllegalArgumentException("Kein Kunde mit ID $newCid")
            existing.customer = cust
        }
        // Rest-Felder mappen
        dto.applyTo(existing)
        return existing
    }

    fun getAllProjects(): List<ProjectModel> = projectRepository.listAll()
    fun getProjectWithEntries(id: Long): ProjectModel? = projectRepository.findByIdWithTimeEntries(id)
    @Transactional
    fun deleteProject(id: Long): Boolean = projectRepository.deleteById(id)
}
