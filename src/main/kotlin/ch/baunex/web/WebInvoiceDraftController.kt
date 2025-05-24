package ch.baunex.web

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.service.InvoiceDraftService
import ch.baunex.invoice.facade.InvoiceDraftFacade
import io.quarkus.qute.TemplateInstance
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import java.time.LocalDate

@Path("/invoice-drafts")
@ApplicationScoped
class WebInvoiceDraftController {

    @Inject
    lateinit var invoiceDraftService: InvoiceDraftService

    @Inject
    lateinit var invoiceDraftFacade: InvoiceDraftFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val drafts = invoiceDraftFacade.getAll()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts = listOf(draft), currentDate = currentDate, activeMenu = activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newForm(): Response {
        val customers = invoiceDraftFacade.getAllCustomers()
        val projects = invoiceDraftFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftForm(null, customers, projects, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@RestPath id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val customers = invoiceDraftFacade.getAllCustomers()
        val projects = invoiceDraftFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftForm(draft, customers, projects, currentDate, activeMenu)
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