package ch.baunex.web

import ch.baunex.document.generator.facade.DocumentGeneratorFacade
import ch.baunex.document.generator.model.DocumentTemplate
import ch.baunex.document.generator.model.DocumentType
import ch.baunex.document.generator.model.templates.InvoiceDraftTemplate
import ch.baunex.document.generator.model.templates.InvoiceItem
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime
import java.util.UUID

@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class WebDocumentController(
    private val documentGeneratorFacade: DocumentGeneratorFacade
) {
    @GET
    @Path("/{type}/preview")
    @Produces(MediaType.TEXT_HTML)
    fun previewDocument(
        @PathParam("type") type: DocumentType,
        @QueryParam("projectId") projectId: Long
    ): Response {
        // TODO: Get project data and create template
        val template = createTemplate(type, projectId)
        val html = documentGeneratorFacade.buildHtml(template)
        return Response.ok(html).build()
    }

    @GET
    @Path("/{type}/generate")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    fun generateDocument(
        @PathParam("type") type: DocumentType,
        @QueryParam("projectId") projectId: Long
    ): Response {
        val template = createTemplate(type, projectId)
        val pdf = documentGeneratorFacade.generateDocument(template)
        
        return Response.ok(pdf)
            .header("Content-Disposition", "attachment; filename=\"${type.name.lowercase()}_${projectId}.pdf\"")
            .build()
    }

    private fun createTemplate(type: DocumentType, projectId: Long): DocumentTemplate {
        // TODO: Implement proper template creation based on project data
        return when (type) {
            DocumentType.INVOICE_DRAFT -> InvoiceDraftTemplate.create(
                companyName = "Baunex AG",
                customerName = "Test Customer",
                invoiceNumber = "INV-${UUID.randomUUID().toString().take(8)}",
                date = LocalDateTime.now(),
                dueDate = LocalDateTime.now().plusDays(30),
                items = listOf(
                    InvoiceItem(
                        description = "Test Item",
                        quantity = 1.0,
                        unitPrice = 100.0,
                        total = 100.0
                    )
                ),
                subtotal = 100.0,
                vatRate = 7.7,
                vatAmount = 7.7,
                total = 107.7
            )
            else -> throw IllegalArgumentException("Document type $type not implemented yet")
        }
    }
} 