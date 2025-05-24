package ch.baunex.invoice.service

import ch.baunex.invoice.dto.ProjectDTO
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class ProjectService {

    @Transactional
    fun getAllProjects(): List<ProjectDTO> {
        // TODO: Implement actual database query
        return emptyList()
    }

    @Transactional
    fun getProjectDetails(projectId: Long): ProjectDTO {
        // TODO: Implement actual database query
        throw NotImplementedError("Not implemented yet")
    }

    @Transactional
    fun getProjectEntries(projectId: Long): ProjectEntriesDTO {
        // TODO: Implement actual database query
        throw NotImplementedError("Not implemented yet")
    }
}

data class ProjectEntriesDTO(
    val timeEntries: List<TimeEntryDTO> = emptyList(),
    val catalogItems: List<CatalogItemDTO> = emptyList()
)

data class TimeEntryDTO(
    val id: Long,
    val description: String,
    val hours: Double,
    val hourlyRate: Double
)

data class CatalogItemDTO(
    val id: Long,
    val description: String,
    val quantity: Double,
    val price: Double
) 