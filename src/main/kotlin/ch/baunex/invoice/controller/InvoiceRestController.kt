package ch.baunex.invoice.controller

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.facade.InvoiceFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger

@Path("/api/invoice")
@ApplicationScoped
class InvoiceRestController {

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    private val logger = Logger.getLogger(InvoiceRestController::class.java)

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: Long): InvoiceDTO =
        invoiceFacade.getById(id)

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(dto: InvoiceDraftDTO): InvoiceDTO {
        logger.info("Received invoice creation request: $dto")
        try {
            val result = invoiceFacade.createInvoice(dto)
            logger.info("Successfully created invoice with ID: ${result.id}")
            return result
        } catch (e: Exception) {
            logger.error("Error creating invoice", e)
            throw e
        }
    }

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
