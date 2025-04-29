package ch.baunex.web

import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.catalog.facade.ProjectCatalogItemFacade
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.web.WebController.Templates
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/projects/{projectId}/catalog")
class WebCatalogController {

    @Inject
    lateinit var projectCatalogItemFacade: ProjectCatalogItemFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Inject
    lateinit var billingFacade: BillingFacade

    @Inject
    lateinit var customerFacade: CustomerFacade

    @GET
    @Path("/{projectId}")
    @Produces(MediaType.TEXT_HTML)
    fun viewProject(@PathParam("projectId") projectId: Long): Response {
        val projectDetail = projectFacade
            .getProjectWithDetails(projectId)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val catalogItems = catalogFacade.getAllItems()
        val billing = billingFacade.getBillingForProject(projectId)
        val contacts: List<CustomerContactDTO> = projectDetail.contacts
        val customers: List<CustomerDTO> = customerFacade.listAll()
        val template = Templates.projectDetail(
            project     = projectDetail,
            activeMenu  = "projects",
            currentDate = LocalDate.now(),
            catalogItems= catalogItems,
            billing     = billing,
            contacts    = contacts,
            customers   = customers
        )

        return Response.ok(template.render()).build()
    }


    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun save(
        @PathParam("projectId") projectId: Long,
        @FormParam("id") id: Long?,
        @FormParam("itemName") itemName: String,
        @FormParam("quantity") quantity: Int,
        @FormParam("unitPrice") unitPrice: Double,
        @FormParam("catalogItemId") catalogItemId: Long?
    ): Response {

        // If no catalogItemId is provided, this is a new custom item
        val actualCatalogItemId = if (catalogItemId == null) {
            // Create a new catalog item in the catalog table
            val newCatalogItem = CatalogItemDTO(
                id = null,
                name = itemName,
                unitPrice = unitPrice
            )
            catalogFacade.createItem(newCatalogItem).id // <-- return newly created ID
        } else {
            catalogItemId
        }

        val dto = ProjectCatalogItemDTO(
            id = id,
            projectId = projectId,
            itemName = itemName,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = quantity * unitPrice,
            catalogItemId = actualCatalogItemId
        )

        if (id == null) {
            projectCatalogItemFacade.addItemToProject(projectId, dto)
        } else {
            projectCatalogItemFacade.updateItem(id, dto)
        }

        return Response.seeOther(URI("/projects/$projectId")).build()
    }

    @GET
    @Path("/{itemId}/delete")
    fun delete(@PathParam("projectId") projectId: Long, @PathParam("itemId") itemId: Long): Response {
        projectCatalogItemFacade.deleteItem(itemId)
        return Response.seeOther(URI("/projects/$projectId")).build()
    }
}
