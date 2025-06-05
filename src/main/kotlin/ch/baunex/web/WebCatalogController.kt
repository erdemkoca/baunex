package ch.baunex.web

//import ch.baunex.billing.facade.BillingFacade
import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.catalog.facade.ProjectCatalogItemFacade
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.web.WebController.Templates
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/projects/{projectId}/catalog")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
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
    @Path("")  // e.g. /projects/1/catalog
    fun viewProject(@PathParam("projectId") projectId: Long): Response {
        val projectDetail = projectFacade
            .getProjectWithDetails(projectId)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        val catalogItems = catalogFacade.getAllItems()
        val billing      = billingFacade.getBillingForProject(projectId)
        val contacts: List<CustomerContactDTO> = projectDetail.contacts
        val customers: List<CustomerDTO>         = customerFacade.listAll()

        val tpl = Templates.projectDetail(
            project      = projectDetail,
            activeMenu   = "projects",
            currentDate  = LocalDate.now(),
            catalogItems = catalogItems,
            billing      = billing,
            contacts     = contacts,
            customers    = customers,
            categories = NoteCategory.entries
        )
        return Response.ok(tpl.render()).build()
    }

    @POST
    @Path("/save")
    @Transactional
    fun save(
        @PathParam("projectId") projectId: Long,
        @FormParam("id")           id: Long?,
        @FormParam("itemName")     itemName: String,
        @FormParam("quantity")     quantity: Int,
        @FormParam("unitPrice")    unitPrice: Double,
        @FormParam("catalogItemId") catalogItemId: Long?
    ): Response {
        // decide whether to attach existing catalog entry or create new
        val actualCatalogItemId = if (catalogItemId == null) {
            val newCatalogItem = CatalogItemDTO(id = null, name = itemName, unitPrice = unitPrice)
            catalogFacade.createItem(newCatalogItem).id!!
        } else {
            catalogItemId
        }

        val dto = ProjectCatalogItemDTO(
            id            = id,
            projectId     = projectId,
            itemName      = itemName,
            quantity      = quantity,
            unitPrice     = unitPrice,
            totalPrice    = quantity * unitPrice,
            catalogItemId = actualCatalogItemId
        )

        if (id == null) {
            projectCatalogItemFacade.addItemToProject(projectId, dto)
        } else {
            projectCatalogItemFacade.updateItem(id, dto)
        }
        // back to Billing tab
        return Response.seeOther(URI("/projects/$projectId#billing")).build()
    }

    @POST
    @Path("/{itemId}/update")
    @Transactional
    fun updateItem(
        @PathParam("projectId") projectId: Long,
        @PathParam("itemId")    itemId: Long,
        @FormParam("itemName")  itemName: String,
        @FormParam("quantity")  quantity: Int,
        @FormParam("unitPrice") unitPrice: Double
    ): Response {
        // reuse your facade's update logic
        val dto = ProjectCatalogItemDTO(
            id            = itemId,
            projectId     = projectId,
            itemName      = itemName,
            quantity      = quantity,
            unitPrice     = unitPrice,
            totalPrice    = quantity * unitPrice,
            catalogItemId = null // not changing the underlying catalog template here
        )
        projectCatalogItemFacade.updateItem(itemId, dto)
        return Response.seeOther(URI("/projects/$projectId#billing")).build()
    }

    @GET
    @Path("/{itemId}/delete")
    @Transactional
    fun delete(
        @PathParam("projectId") projectId: Long,
        @PathParam("itemId")    itemId: Long
    ): Response {
        projectCatalogItemFacade.deleteItem(itemId)
        return Response.seeOther(URI("/projects/$projectId#billing")).build()
    }
}
