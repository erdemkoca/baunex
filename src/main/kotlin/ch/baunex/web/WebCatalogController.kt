package ch.baunex.web

import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.dto.ProjectCatalogItemDTO
import ch.baunex.catalog.facade.CatalogFacade
import ch.baunex.catalog.facade.ProjectCatalogItemFacade
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.web.WebController.Templates
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI

@Path("/projects/{projectId}/catalog")
class WebCatalogController {

    @Inject
    lateinit var projectCatalogItemFacade: ProjectCatalogItemFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(@PathParam("projectId") projectId: Long): Response {
        val project = projectFacade.getProjectWithDetails(projectId) ?: return Response.status(404).build()
        val catalogItems = catalogFacade.getAllItems()
        val template = Templates.projectDetail(project, "projects", java.time.LocalDate.now(), catalogItems)
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
