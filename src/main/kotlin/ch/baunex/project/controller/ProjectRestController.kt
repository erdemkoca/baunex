package ch.baunex.project.controller

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.dto.ProjectDetailDTO
import ch.baunex.project.facade.ProjectFacade
import kotlinx.serialization.encodeToString
import ch.baunex.serialization.SerializationUtils.json
import org.jboss.resteasy.reactive.multipart.FileUpload
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.user.facade.EmployeeFacade
import ch.baunex.project.dto.ProjectNotesViewDTO
import ch.baunex.web.WebController.Templates
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.GenericEntity
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime

@Path("/projects")
@ApplicationScoped
class ProjectRestController {

    @Inject lateinit var projectFacade: ProjectFacade
    @Inject lateinit var catalogFacade: CatalogFacade
    @Inject lateinit var billingFacade: BillingFacade
    @Inject lateinit var customerFacade: CustomerFacade
    @Inject lateinit var employeeFacade: EmployeeFacade
    @Inject lateinit var noteAttachmentFacade: NoteAttachmentFacade

    private fun nowDate() = LocalDate.now()
    private fun nowDateTime() = LocalDateTime.now()

    /*** HTML ***/

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val dtos = projectFacade.getAllProjects()
        val tpl = Templates.projects(
            projectsJson = json.encodeToString(dtos),
            currentDate  = nowDate(),
            activeMenu   = "projects"
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newForm(): Response {
        val customers    = customerFacade.listAll()
        val catalogItems = catalogFacade.getAllItems()
        val now          = nowDateTime()
        val emptyCust = CustomerDTO(
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
            id                     = 0,
            name                   = "",
            customerId             = 0,
            customerName           = "",
            budget                 = 0,
            customer               = emptyCust,
            startDate              = null,
            endDate                = null,
            description            = null,
            status                 = ch.baunex.project.model.ProjectStatus.PLANNED,
            street                 = null,
            city                   = null,
            timeEntries            = emptyList(),
            catalogItems           = emptyList(),
            contacts               = emptyList(),
            projectNumberFormatted = ""
        )
        val tpl = Templates.projectDetail(
            projectJson      = json.encodeToString(emptyDetail),
            customersJson    = json.encodeToString(customers),
            employeesJson    = json.encodeToString(employeeFacade.listAll()),
            catalogItemsJson = json.encodeToString(catalogItems),
            billingJson      = json.encodeToString(BillingDTO(0, emptyList(), emptyList(), 0.0,0.0,0.0)),
            categoriesJson   = json.encodeToString(NoteCategory.values().map { it.name }),
            currentDate      = nowDate(),
            activeMenu       = "projects",
            contactsJson     = "[]",
            projectId    = 0,
            activeSubMenu = "detail"
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/{id}") //Übersicht
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@PathParam("id") id: Long): Response {
        val detail       = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val tpl = Templates.projectDetail(
            projectJson      = json.encodeToString(detail),
            customersJson    = json.encodeToString(customerFacade.listAll()),
            employeesJson    = json.encodeToString(employeeFacade.listAll()),
            catalogItemsJson = json.encodeToString(catalogFacade.getAllItems()),
            billingJson      = json.encodeToString(billingFacade.getBillingForProject(id)),
            categoriesJson   = json.encodeToString(NoteCategory.values().map { it.name }),
            currentDate      = nowDate(),
            activeMenu       = "projects",
            contactsJson     = json.encodeToString(detail.contacts),
            projectId        = detail.id,
            activeSubMenu    = "detail"
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/{id}/contacts")
    @Produces(MediaType.TEXT_HTML)
    fun viewContacts(@PathParam("id") id: Long): Response {
        val detail     = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val tpl = Templates.projectContacts(
            projectJson      = json.encodeToString(detail),
            customersJson    = json.encodeToString(customerFacade.listAll()),
            employeesJson    = json.encodeToString(employeeFacade.listAll()),
            catalogItemsJson = json.encodeToString(catalogFacade.getAllItems()),
            billingJson      = json.encodeToString(billingFacade.getBillingForProject(id)),
            categoriesJson   = json.encodeToString(NoteCategory.values().map { it.name }),
            currentDate      = nowDate(),
            activeMenu       = "projects",
            contactsJson     = json.encodeToString(detail.contacts),
            projectId        = detail.id,
            activeSubMenu    = "contacts"
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/{id}/catalog")
    @Produces(MediaType.TEXT_HTML)
    fun viewCatalog(@PathParam("id") id: Long): Response {
        val detail     = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val tpl = Templates.projectCatalog(
            projectJson      = json.encodeToString(detail),
            customersJson    = json.encodeToString(customerFacade.listAll()),
            employeesJson    = json.encodeToString(employeeFacade.listAll()),
            catalogItemsJson = json.encodeToString(catalogFacade.getAllItems()),
            billingJson      = json.encodeToString(billingFacade.getBillingForProject(id)),
            categoriesJson   = json.encodeToString(NoteCategory.values().map { it.name }),
            currentDate      = nowDate(),
            activeMenu       = "projects",
            contactsJson     = json.encodeToString(detail.contacts),
            projectId        = detail.id,
            activeSubMenu    = "catalog"
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/{id}/billing")
    @Produces(MediaType.TEXT_HTML)
    fun viewBilling(@PathParam("id") id: Long): Response {
        val detail     = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val tpl = Templates.projectBilling(
            projectJson      = json.encodeToString(detail),
            customersJson    = json.encodeToString(customerFacade.listAll()),
            employeesJson    = json.encodeToString(employeeFacade.listAll()),
            catalogItemsJson = json.encodeToString(catalogFacade.getAllItems()),
            billingJson      = json.encodeToString(billingFacade.getBillingForProject(id)),
            categoriesJson   = json.encodeToString(NoteCategory.values().map { it.name }),
            currentDate      = nowDate(),
            activeMenu       = "projects",
            contactsJson     = json.encodeToString(detail.contacts),
            projectId        = detail.id,
            activeSubMenu    = "billing"
        )
        return Response.ok(tpl.render()).build()
    }

    /*** JSON ***/

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun saveJson(
        @QueryParam("id") id: Long?,
        createDto: ProjectCreateDTO
    ): Response {
        val newId = projectFacade.createOrUpdate(id, createDto)
        return Response.ok(mapOf("id" to newId)).build()
    }

    @POST
    @Path("/{id}/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun saveJsonWithPath(
        @PathParam("id") id: Long,
        createDto: ProjectCreateDTO
    ): Response = saveJson(id, createDto)

    @POST
    @Path("/{id}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteJson(@PathParam("id") id: Long): Response {
        projectFacade.deleteProject(id)
        return Response.ok().build()
    }
}
