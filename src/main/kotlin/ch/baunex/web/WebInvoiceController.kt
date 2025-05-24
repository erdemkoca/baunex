package ch.baunex.web

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.service.InvoiceService
import ch.baunex.invoice.facade.InvoiceFacade
import ch.baunex.project.facade.ProjectFacade
import io.quarkus.qute.TemplateInstance
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import java.time.LocalDate

@Path("/invoices")
@ApplicationScoped
class WebInvoiceController {

    @Inject
    lateinit var invoiceService: InvoiceService

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val invoices = invoiceFacade.getAll()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoices"
        val template = WebController.Templates.invoiceList(invoices, projects, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val invoice = invoiceFacade.getById(id)
        val currentDate = LocalDate.now()
        val activeMenu = "invoices"
        val template = WebController.Templates.invoiceDetail(invoice, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/{id}/mark-as-paid")
    @Produces(MediaType.APPLICATION_JSON)
    fun markAsPaid(@PathParam("id") id: Long) {
        invoiceFacade.markAsPaid(id)
    }

    @POST
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    fun cancel(@PathParam("id") id: Long) {
        invoiceFacade.cancel(id)
    }
} 