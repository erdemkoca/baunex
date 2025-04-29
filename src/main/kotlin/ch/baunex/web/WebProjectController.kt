// File: ch/baunex/web/WebProjectController.kt
package ch.baunex.web

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.dto.ProjectDetailDTO
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.dto.ProjectUpdateDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.service.ProjectService
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

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
        // Für das „Neue Projekt“-Formular baust Du am besten ein CreateDTO oder DetailDTO
        val emptyDetail = ProjectDetailDTO(
            id             = 0,
            name           = "",
            customerId     = 0,
            customerName   = "",
            budget         = 0,
            contact        = null,
            startDate      = null,
            endDate        = null,
            description    = null,
            status         = ch.baunex.project.model.ProjectStatus.PLANNED,
            street         = null,
            city           = null,
            timeEntries    = emptyList(),
            catalogItems   = emptyList(),
            contacts   = emptyList(),
        )
        val template = WebController.Templates
            .projectDetail(emptyDetail, "projects", getCurrentDate(),
                catalogItems = emptyList(), billing = BillingDTO(0, emptyList(), emptyList(), 0.0, 0.0, 0.0), contacts = emptyList(), customers = emptyList()
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
        val template = WebController.Templates
            .projectDetail(detail, "projects", getCurrentDate(), catalogItems, billing, contacts, customers)
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Transactional
    fun saveProject(
        @FormParam("id") id: Long?,
        @FormParam("name") name: String,
        @FormParam("customerId") customerId: Long,
        @FormParam("budget") budget: Int,
        @FormParam("contact") contact: String?,
        @FormParam("startDate") startDate: LocalDate?,
        @FormParam("endDate") endDate: LocalDate?,
        @FormParam("description") description: String?,
        @FormParam("status") status: ch.baunex.project.model.ProjectStatus?,
        @FormParam("street") street: String?,
        @FormParam("city") city: String?
    ): Response {
        if (id == null || id == 0L) {
            val createDto = ProjectCreateDTO(
                name        = name,
                customerId  = customerId,
                budget      = budget,
                contact     = contact,
                startDate   = startDate ?: getCurrentDate(),
                endDate     = endDate   ?: getCurrentDate(),
                description = description,
                status      = status    ?: ch.baunex.project.model.ProjectStatus.PLANNED,
                street      = street,
                city        = city
            )
            projectFacade.createProject(createDto)
        } else {
            val updateDto = ProjectUpdateDTO(
                name        = name,
                customerId  = customerId,
                budget      = budget,
                contact     = contact,
                startDate   = startDate,
                endDate     = endDate,
                description = description,
                status      = status,
                street      = street,
                city        = city
            )
            projectFacade.updateProject(id, updateDto)
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
