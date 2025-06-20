package ch.baunex.controlreport.service

import ch.baunex.company.model.CompanyModel
import ch.baunex.company.repository.CompanyRepository
import ch.baunex.controlreport.dto.ControlReportDto
import ch.baunex.controlreport.mapper.ControlReportMapper
import ch.baunex.controlreport.repository.ControlReportRepository
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.model.ProjectType
import ch.baunex.project.repository.ProjectRepository
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.model.PersonModel
import ch.baunex.user.repository.CustomerRepository
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@QuarkusTest
class ControlReportServiceTest {

    @Inject
    lateinit var controlReportService: ControlReportService

    @Inject
    lateinit var projectRepository: ProjectRepository

    @Inject
    lateinit var companyRepository: CompanyRepository

    @Inject
    lateinit var customerRepository: CustomerRepository

    @Inject
    lateinit var controlReportRepository: ControlReportRepository

    @Inject
    lateinit var mapper: ControlReportMapper

    lateinit var savedProject: ProjectModel

    @BeforeEach
    @Transactional
    fun setUp() {
        controlReportRepository.deleteAll()
        projectRepository.deleteAll()
        customerRepository.deleteAll()
        companyRepository.deleteAll()

        val company = CompanyModel().apply {
            name = "Testfirma AG"
            street = "Teststrasse 1"
            zipCode = "8000"
            city = "Zürich"
            country = "Schweiz"
        }
        companyRepository.persist(company)

        val customer = CustomerModel().apply {
            person = PersonModel().apply {
                firstName = "Max"
                lastName = "Muster"
                details = PersonDetails().apply {
                    street = "Kundenweg 5"
                    zipCode = "3000"
                    city = "Bern"
                }
            }
        }
        customerRepository.persist(customer)

        val project = ProjectModel().apply {
            name = "Testprojekt"
            this.customer = customer
            buildingType = ProjectType.COMMERCIAL_BUILDING
            parcelNumber = "1234"
        }
        projectRepository.persist(project)
        savedProject = project
    }

    @Test
    @Transactional
    fun `should initialize control report if none exists`() {
        val dto: ControlReportDto = controlReportService.getOrInitializeByProjectId(savedProject.id!!)

        assertNotNull(dto)
        assertEquals("Max", dto.client.firstName)
        assertEquals("Muster", dto.client.lastName)
        assertEquals("Testfirma AG", dto.contractor.company)
        assertEquals("Zürich", dto.contractor.city)
        assertEquals(LocalDate.now(), dto.controlData.controlDate)
    }

    @Test
    @Transactional
    fun `should reuse existing report if already initialized`() {
        val first = controlReportService.getOrInitializeByProjectId(savedProject.id!!)
        val second = controlReportService.getOrInitializeByProjectId(savedProject.id!!)

        assertEquals(first.id, second.id)
        assertEquals(first.reportNumber, second.reportNumber)

        val reports = controlReportRepository.findByProjectId(savedProject.id!!)
        assertEquals(1, reports.size)
    }

    @Test
    @Transactional
    fun `should throw if project not found`() {
        val unknownId = 9999L
        val ex = assertThrows(jakarta.ws.rs.NotFoundException::class.java) {
            controlReportService.getOrInitializeByProjectId(unknownId)
        }
        assertTrue(ex.message!!.contains("Project"))
    }
}
