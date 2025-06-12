package ch.baunex.documentGenerator.controller

import ch.baunex.documentGenerator.dto.invoice.InvoiceDocumentResponseDTO
import ch.baunex.documentGenerator.facade.InvoiceDocumentFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.io.ByteArrayInputStream
import org.jboss.logging.Logger

@Path("/api/documents/invoices")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
class InvoiceDocumentController @Inject constructor(
    private val facade: InvoiceDocumentFacade
) {
    private val logger = Logger.getLogger(InvoiceDocumentController::class.java)


    /** Create a new PDF‐document entry for an existing invoice ID */
    @POST
    @Path("/{invoiceId}")
    fun create(@PathParam("invoiceId") invoiceId: Long): InvoiceDocumentResponseDTO =
        facade.createFromInvoice(invoiceId)

    /** Stream back the PDF for that invoice‐document */
    @GET
    @Path("/{invoiceDocId}/pdf")
    @Produces("application/pdf")
    fun downloadPdf(@PathParam("invoiceDocId") invoiceDocId: Long): Response {
        logger.info("PDF-Download angefragt für InvoiceDocument mit ID = $invoiceDocId")

        // Optional: existiert das DocumentModel überhaupt?
        val exists = try {
            facade.generatePdf(invoiceDocId) // oder falls Du nur testen willst: facade.getById(...)
            true
        } catch (e: Exception) {
            logger.warn("Kein InvoiceDocument gefunden oder Fehler beim Generieren: ${e.message}")
            false
        }
        if (!exists) {
            // Gib 404 zurück
            return Response.status(Response.Status.NOT_FOUND)
                .entity("InvoiceDocument $invoiceDocId nicht gefunden")
                .build()
        }
        val bytes = facade.generatePdf(invoiceDocId)
        val filename = "invoice_$invoiceDocId.pdf"
        return Response.ok(ByteArrayInputStream(bytes))
            .header("Content-Disposition", "attachment; filename=\"$filename\"")
            .build()
    }

    /** Optionally expose a JSON list of all invoice‐documents */
    @GET
    fun list(): List<InvoiceDocumentResponseDTO> =
        facade.listAllInvoices()
}
