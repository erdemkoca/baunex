package ch.baunex.web

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.InvoiceEntryDTO
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.service.InvoiceDraftService
import ch.baunex.invoice.facade.InvoiceDraftFacade
import ch.baunex.project.facade.ProjectFacade
import io.quarkus.qute.TemplateInstance
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import java.time.LocalDate
import java.net.URI

@Path("/invoice-drafts")
@ApplicationScoped
class WebInvoiceDraftController {

    @Inject
    lateinit var invoiceDraftService: InvoiceDraftService

    @Inject
    lateinit var invoiceDraftFacade: InvoiceDraftFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val drafts = invoiceDraftFacade.getAll()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts, projects, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts = listOf(draft),projects, currentDate = currentDate, activeMenu = activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newForm(@QueryParam("projectId") projectId: Long?): Response {
        val projects = projectFacade.getAllProjects()
        val customers = invoiceDraftFacade.getAllCustomers()
        val currentDate = LocalDate.now()
        val dueDate = currentDate.plusDays(30)
        
        // If projectId is provided, get project details
        val project = projectId?.let { projectFacade.getProjectWithDetails(it) }
        
        val template = WebController.Templates.invoiceDraftForm(
            draft = null,
            customers = customers,
            projects = projects,
            currentDate = currentDate,
            dueDate = dueDate,
            activeMenu = "invoice-drafts",
            selectedProject = project
        )
        
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun create(
        @FormParam("projectId") projectId: Long,
        @FormParam("serviceStartDate") serviceStartDate: String?,
        @FormParam("serviceEndDate") serviceEndDate: String?,
        @FormParam("notes") notes: String?
    ): Response {
        val project = projectFacade.getProjectWithDetails(projectId)
            ?: return Response.status(404).build()

        val draft = InvoiceDraftDTO(
            projectId = projectId,
            customerId = project.customerId,
            customerName = project.customerName,
            customerAddress = project.customer.street + ", " + project.customer.city,
            serviceStartDate = serviceStartDate?.let { LocalDate.parse(it) },
            serviceEndDate = serviceEndDate?.let { LocalDate.parse(it) },
            notes = notes,
            status = "DRAFT",
            entries = project.timeEntries.map { entry ->
                InvoiceEntryDTO(
                    description = entry.notes,
                    type = "VA",
                    quantity = entry.hoursWorked,
                    price = entry.hourlyRate,
                    total = entry.hoursWorked * entry.hourlyRate
                )
            } + project.catalogItems.map { item ->
                InvoiceEntryDTO(
                    description = item.itemName,
                    type = "IC",
                    quantity = item.quantity.toDouble(),
                    price = item.unitPrice,
                    total = item.totalPrice
                )
            }
        )

        val createdDraft = invoiceDraftFacade.create(draft)
        return Response.seeOther(URI.create("/invoice-drafts/${createdDraft.id}")).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@RestPath id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val customers = invoiceDraftFacade.getAllCustomers()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val dueDate = draft.dueDate ?: currentDate.plusDays(30)
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftForm(
            draft = draft,
            customers = customers,
            projects = projects,
            currentDate = currentDate,
            dueDate = dueDate,
            activeMenu = activeMenu
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/{id}/create-invoice")
    @Produces(MediaType.TEXT_HTML)
    fun createInvoice(@RestPath id: Long): Response {
        try {
            val draft = invoiceDraftService.getById(id)
            if (draft.status != "DRAFT") {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nur Rechnungsentwürfe können in Rechnungen umgewandelt werden.")
                    .build()
            }

            val invoice = invoiceDraftService.createInvoiceFromDraft(id)
            return Response.seeOther(java.net.URI.create("/invoices/${invoice.id}")).build()
        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Fehler beim Erstellen der Rechnung: ${e.message}")
                .build()
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun create(@FormParam("invoiceNumber") invoiceNumber: String,
              @FormParam("invoiceDate") invoiceDate: String,
              @FormParam("dueDate") dueDate: String,
              @FormParam("customerId") customerId: Long,
              @FormParam("projectId") projectId: Long,
              @FormParam("notes") notes: String?): Response {
        val draft = InvoiceDraftDTO(
            invoiceNumber = invoiceNumber,
            invoiceDate = java.time.LocalDate.parse(invoiceDate),
            dueDate = java.time.LocalDate.parse(dueDate),
            customerId = customerId,
            projectId = projectId,
            notes = notes,
            status = "DRAFT"
        )

        val created = invoiceDraftFacade.create(draft)
        return Response.seeOther(java.net.URI.create("/invoice-drafts/${created.id}")).build()
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun update(@RestPath id: Long,
              @FormParam("invoiceNumber") invoiceNumber: String,
              @FormParam("invoiceDate") invoiceDate: String,
              @FormParam("dueDate") dueDate: String,
              @FormParam("customerId") customerId: Long,
              @FormParam("projectId") projectId: Long,
              @FormParam("notes") notes: String?): Response {
        val draft = InvoiceDraftDTO(
            id = id,
            invoiceNumber = invoiceNumber,
            invoiceDate = java.time.LocalDate.parse(invoiceDate),
            dueDate = java.time.LocalDate.parse(dueDate),
            customerId = customerId,
            projectId = projectId,
            notes = notes,
            status = "DRAFT"
        )

        invoiceDraftFacade.update(id, draft)
        return Response.seeOther(java.net.URI.create("/invoice-drafts/${id}")).build()
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delete(@PathParam("id") id: Long) {
        invoiceDraftFacade.delete(id)
    }
} 