package ch.baunex.config

import ch.baunex.project.ProjectHandler
import ch.baunex.project.dto.ProjectRequest
import ch.baunex.worker.WorkerHandler
import ch.baunex.worker.dto.WorkerRequest
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleDataLoader {

    @Inject
    lateinit var projectHandler: ProjectHandler

    @Inject
    lateinit var workerHandler: WorkerHandler

    @Transactional
    fun loadSampleData(@Observes event: StartupEvent) {
        // Only load sample data if no projects exist
        if (projectHandler.getAllProjects().isEmpty()) {
            // Create sample projects
            val projects = listOf(
                ProjectRequest(
                    name = "Office Renovation",
                    budget = 25000,
                    client = "ABC Corporation",
                    contact = "John Smith, john.smith@abc.com"
                ),
                ProjectRequest(
                    name = "Website Redesign",
                    budget = 15000,
                    client = "XYZ Industries",
                    contact = "Jane Doe, jane.doe@xyz.com"
                ),
                ProjectRequest(
                    name = "Marketing Campaign",
                    budget = 35000,
                    client = "Global Enterprises",
                    contact = "Michael Johnson, mjohnson@global.com"
                ),
                ProjectRequest(
                    name = "Software Implementation",
                    budget = 50000,
                    client = "Tech Solutions Inc.",
                    contact = "Sarah Williams, swilliams@techsolutions.com"
                ),
                ProjectRequest(
                    name = "Product Launch Event",
                    budget = 20000,
                    client = "Innovative Products",
                    contact = "Robert Brown, rbrown@innovative.com"
                )
            )

            // Save sample projects
            projects.forEach { projectHandler.saveProject(it) }

            // Create sample workers
            val workers = listOf(
                WorkerRequest(
                    firstName = "David",
                    lastName = "Miller",
                    email = "david.miller@baunex.ch",
                    phone = "+41 76 123 4567",
                    position = "Project Manager",
                    hourlyRate = 85.0
                ),
                WorkerRequest(
                    firstName = "Emma",
                    lastName = "Wilson",
                    email = "emma.wilson@baunex.ch",
                    phone = "+41 76 234 5678",
                    position = "Software Developer",
                    hourlyRate = 75.0
                ),
                WorkerRequest(
                    firstName = "Thomas",
                    lastName = "Anderson",
                    email = "thomas.anderson@baunex.ch",
                    phone = "+41 76 345 6789",
                    position = "UI/UX Designer",
                    hourlyRate = 70.0
                ),
                WorkerRequest(
                    firstName = "Olivia",
                    lastName = "Martinez",
                    email = "olivia.martinez@baunex.ch",
                    phone = "+41 76 456 7890",
                    position = "Marketing Specialist",
                    hourlyRate = 65.0
                ),
                WorkerRequest(
                    firstName = "James",
                    lastName = "Taylor",
                    email = "james.taylor@baunex.ch",
                    phone = "+41 76 567 8901",
                    position = "Business Analyst",
                    hourlyRate = 80.0
                )
            )

            // Save sample workers
            workers.forEach { workerHandler.saveWorker(it) }
        }
    }
} 