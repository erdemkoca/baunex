package ch.baunex.web

import ch.baunex.document.generator.facade.DocumentGeneratorFacade
import ch.baunex.document.generator.model.DocumentType
import ch.baunex.document.generator.model.templates.InvoiceDraftTemplate
import ch.baunex.invoice.service.InvoiceService
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime
import java.util.UUID

@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class DocumentController(
    private val documentGeneratorFacade: DocumentGeneratorFacade,
    private val invoiceService: InvoiceService
) {
    @Inject
    lateinit var documentPreview: Template

    @GET
    @Path("/{type}/preview")
    @Produces(MediaType.TEXT_HTML)
    fun previewDocument(
        @PathParam("type") type: DocumentType,
        @QueryParam("projectId") projectId: Long
    ): TemplateInstance {
        val template = createTemplate(type, projectId)
        val html = documentGeneratorFacade.buildHtml(template)
        return documentPreview.data("content", html)
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
        return when (type) {
            DocumentType.INVOICE_DRAFT -> {
                val invoice = invoiceService.getInvoiceByProjectId(projectId)
                InvoiceDraftTemplate.create(
                    companyName = "Baunex AG",
                    customerName = invoice.customer.name,
                    invoiceNumber = invoice.invoiceNumber,
                    date = invoice.date,
                    dueDate = invoice.dueDate,
                    items = invoice.items.map { item ->
                        InvoiceItem(
                            description = item.description,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            total = item.total
                        )
                    },
                    subtotal = invoice.subtotal,
                    vatRate = invoice.vatRate,
                    vatAmount = invoice.vatAmount,
                    total = invoice.total
                )
            }
            else -> throw IllegalArgumentException("Document type $type not implemented yet")
        }
    }
} 