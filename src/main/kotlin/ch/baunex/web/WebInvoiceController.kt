package ch.baunex.web

import ch.baunex.invoice.facade.InvoiceFacade
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.time.LocalDate

@Path("/invoices")
class WebInvoiceController {

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): TemplateInstance {
        val invoices = invoiceFacade.getAll()
        val currentDate = LocalDate.now()
        val activeMenu = "invoices"
        return WebController.Templates.invoiceList(invoices, currentDate, activeMenu)
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): TemplateInstance {
        val invoice = invoiceFacade.getById(id)
        val currentDate = LocalDate.now()
        val activeMenu = "invoices"
        return WebController.Templates.invoiceDetail(invoice, currentDate, activeMenu)
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