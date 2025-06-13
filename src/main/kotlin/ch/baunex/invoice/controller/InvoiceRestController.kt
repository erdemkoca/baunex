package ch.baunex.invoice.controller

import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceNewDraftDTO
import ch.baunex.invoice.facade.InvoiceFacade
import ch.baunex.invoice.model.InvoiceStatus
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
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun newInvoice(dto: InvoiceNewDraftDTO): InvoiceDTO {
        logger.info("Received new invoice request: $dto")
        try {
            val result = invoiceFacade.createNewInvoice(dto)
            logger.info("Successfully created new invoice with ID: ${result.id}")
            return result
        } catch (e: Exception) {
            logger.error("Error creating new invoice", e)
            throw e
        }
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createInvoice(dto: InvoiceDTO): InvoiceDTO {
        logger.info("Received invoice creation request: $dto")
        try {
            val result = if (dto.invoiceStatus == InvoiceStatus.DRAFT) {
                invoiceFacade.createDraftInvoice(dto)
            } else {
                invoiceFacade.createIssuedInvoice(dto)
            }
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
