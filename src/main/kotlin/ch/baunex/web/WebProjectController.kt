package ch.baunex.web

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.dto.ProjectDetailDTO
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.dto.ProjectUpdateDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.user.model.Role
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@Path("/projects")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
class WebProjectController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var billingFacade: BillingFacade

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    private fun getCurrentDate() = LocalDate.now()

    @GET
    fun list(): Response {
        val projects: List<ProjectListDTO> = projectFacade.getAllProjects()
        val template = WebController.Templates
            .projects(projects, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    fun newProject(): Response {
        // Build a dummy CustomerDTO to satisfy the non-null parameter in ProjectDetailDTO
        val now = LocalDateTime.now()
        val emptyCustomer = CustomerDTO(
            id               = 0L,
            firstName        = "",
            lastName         = "",
            email            = null,
            street           = null,
            city             = null,
            zipCode          = null,
            country          = null,
            phone            = null,
            customerNumber   = 0,
            formattedCustomerNumber = "",
            companyName      = null,
            paymentTerms     = null,
            creditLimit      = null,
            industry         = null,
            discountRate     = null,
            preferredLanguage= null,
            marketingConsent = false,
            taxId            = null,
            createdAt        = now,
            updatedAt        = now,
            contacts         = emptyList()
        )
        val emptyDetail = ProjectDetailDTO(
            id           = 0,
            name         = "",
            customerId   = 0,
            customerName = "",
            budget       = 0,
            customer     = emptyCustomer,
            startDate    = null,
            endDate      = null,
            description  = null,
            status       = ch.baunex.project.model.ProjectStatus.PLANNED,
            street       = null,
            city         = null,
            timeEntries  = emptyList(),
            catalogItems = emptyList(),
            contacts     = emptyList(),
            projectNumberFormatted = ""
        )
        // Preload all customers so the dropdown can be populated
        val customers = customerFacade.listAll()
        val template = WebController.Templates.projectDetail(
            emptyDetail,
            "projects",
            getCurrentDate(),
            catalogItems = emptyList(),
            billing      = BillingDTO(0, emptyList(), emptyList(), 0.0, 0.0, 0.0),
            contacts     = emptyList(),
            customers    = customers
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    fun view(@PathParam("id") id: Long): Response {
        val detail: ProjectDetailDTO = projectFacade
            .getProjectWithDetails(id)
            ?: return Response.status(404).build()
        val catalogItems = catalogFacade.getAllItems()
        val billing: BillingDTO = billingFacade.getBillingForProject(id)
        val contacts: List<CustomerContactDTO> = detail.contacts
        val customers: List<CustomerDTO> = customerFacade.listAll()
        val template = WebController.Templates.projectDetail(
            detail,
            "projects",
            getCurrentDate(),
            catalogItems,
            billing,
            contacts,
            customers
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Transactional
    fun saveProject(
        @FormParam("id")         id: Long?,
        @FormParam("name")       name: String,
        @FormParam("customerId") customerId: Long,
        @FormParam("budget")     budget: Int,
        @FormParam("startDate")  startDate: LocalDate?,
        @FormParam("endDate")    endDate: LocalDate?,
        @FormParam("description")description: String?,
        @FormParam("status")     status: ch.baunex.project.model.ProjectStatus?,
        @FormParam("street")     street: String?,
        @FormParam("city")       city: String?,
        //@FormParam("employeeId") employeeId: Long,
        @FormParam("projectNoteTitle") projectNoteTitle: String?,
        @FormParam("projectNoteContent") projectNoteContent: String?,
        @FormParam("projectNoteCategory") projectNoteCategory: String?
        ): Response {
        val employeeId = employeeFacade.findByRole(Role.ADMIN).id
        //TODO now employeeId is hardcoded, should be changed

        val projectNotes: List<NoteDto> = if (!projectNoteContent.isNullOrBlank()) {
            listOf(
                NoteDto(
                    id            = 0L,
                    projectId     = null,
                    timeEntryId   = null,
                    documentId    = null,
                    createdById   = employeeId,
                    createdByName = employeeFacade.findById(employeeId).firstName + employeeFacade.findById(employeeId).lastName,
                    createdAt     = LocalDateTime.now(),
                    updatedAt     = null,
                    title         = projectNoteTitle,
                    content       = projectNoteContent,
                    category      = NoteCategory.valueOf(projectNoteCategory ?: "INFO"),
                    tags          = emptyList(),
                    attachments   = emptyList()
                )
            )
        } else {
            emptyList()
        }

        if (id == null || id == 0L) {
            projectFacade.createProject(
                ProjectCreateDTO(
                    name        = name,
                    customerId  = customerId,
                    budget      = budget,
                    startDate   = startDate ?: getCurrentDate(),
                    endDate     = endDate   ?: getCurrentDate(),
                    description = description,
                    status      = status    ?: ch.baunex.project.model.ProjectStatus.PLANNED,
                    street      = street,
                    city        = city,
                    initialNotes = projectNotes
                )
            )
        } else {
            projectFacade.updateProject(
                id,
                ProjectUpdateDTO(
                    name        = name,
                    customerId  = customerId,
                    budget      = budget,
                    startDate   = startDate,
                    endDate     = endDate,
                    description = description,
                    status      = status,
                    street      = street,
                    city        = city,
                    updatedNotes = projectNotes
                )
            )
        }
        return Response.seeOther(URI("/projects")).build()
    }

    @GET
    @Path("/{id}/delete")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        projectFacade.deleteProject(id)
        return Response.seeOther(URI("/projects")).build()
    }
}
