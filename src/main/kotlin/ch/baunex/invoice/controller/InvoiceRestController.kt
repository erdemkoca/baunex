package ch.baunex.invoice.controller

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.facade.InvoiceFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("/api/invoice")
@ApplicationScoped
class InvoiceRestController {

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: Long): InvoiceDTO =
        invoiceFacade.getById(id)

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(dto: InvoiceDraftDTO): InvoiceDTO =
        invoiceFacade.createInvoice(dto)

    @POST @Path("/{id}/mark-as-paid")
    @Produces(MediaType.APPLICATION_JSON)
    fun markAsPaid(@PathParam("id") id: Long) {
        invoiceFacade.markAsPaid(id)
    }

    @POST @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    fun cancel(@PathParam("id") id: Long) {
        invoiceFacade.cancel(id)
    }

    // …and any list or draft endpoints you need
}
