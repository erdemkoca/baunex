package ch.baunex.documentGenerator.controller

import ch.baunex.documentGenerator.dto.invoice.InvoiceDocumentResponseDTO
import ch.baunex.documentGenerator.facade.InvoiceDocumentFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.io.ByteArrayInputStream

@Path("/api/invoices")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
class InvoiceDocumentController @Inject constructor(
    private val facade: InvoiceDocumentFacade
) {

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
