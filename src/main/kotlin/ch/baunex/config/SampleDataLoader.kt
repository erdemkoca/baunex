package ch.baunex.config

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleDataLoader {

    @Inject
    lateinit var projectFacade: ProjectFacade


    @Transactional
    fun loadSampleData(@Observes event: StartupEvent) {
        // Only load sample data if no projects exist
        if (projectFacade.getAllProjects().isEmpty()) {
            // Create sample projects
            val projects = listOf(
                ProjectDTO(
                    name = "Office Renovation",
                    budget = 25000,
                    client = "ABC Corporation",
                    contact = "John Smith, john.smith@abc.com"
                ),
                ProjectDTO(
                    name = "Website Redesign",
                    budget = 15000,
                    client = "XYZ Industries",
                    contact = "Jane Doe, jane.doe@xyz.com"
                ),
                ProjectDTO(
                    name = "Marketing Campaign",
                    budget = 35000,
                    client = "Global Enterprises",
                    contact = "Michael Johnson, mjohnson@global.com"
                ),
                ProjectDTO(
                    name = "Software Implementation",
                    budget = 50000,
                    client = "Tech Solutions Inc.",
                    contact = "Sarah Williams, swilliams@techsolutions.com"
                ),
                ProjectDTO(
                    name = "Product Launch Event",
                    budget = 20000,
                    client = "Innovative Products",
                    contact = "Robert Brown, rbrown@innovative.com"
                )
            )

            // Save sample projects
            projects.forEach { projectFacade.createProject(it) }


        }
    }
} 