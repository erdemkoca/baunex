package ch.baunex.documentGenerator.mapper

import ch.baunex.documentGenerator.dto.DocumentDTO
import ch.baunex.documentGenerator.dto.DocumentEntryDTO
import ch.baunex.documentGenerator.dto.DocumentEntryResponseDTO
import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.model.DocumentEntryModel
import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.model.DocumentType
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.CustomerModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentMapper {

    /**
     * Wandelt ein DocumentDTO in das persistierbare DocumentModel um.
     */
    fun toModel(dto: DocumentDTO): DocumentModel {
        val doc = DocumentModel().apply {
            type = dto.type
            customerName = dto.customerName
            markdownHeader = dto.markdownHeader
            markdownFooter = dto.markdownFooter
        }
        doc.entries = dto.entries.map { entryDto ->
            DocumentEntryModel().apply {
                document = doc
                description = entryDto.description
                quantity = entryDto.quantity
                price = entryDto.price
            }
        }.toMutableList()
        return doc
    }

    /**
     * Wandelt ein DocumentModel in das REST-Response-DTO um.
     */
    fun toResponseDTO(model: DocumentModel): DocumentResponseDTO {
        return DocumentResponseDTO(
            id = model.id,
            type = model.type.name,
            customerName = model.customerName,
            markdownHeader = model.markdownHeader,
            markdownFooter = model.markdownFooter,
            createdAt = model.createdAt,
            entries = model.entries.map { entry ->
                DocumentEntryResponseDTO(
                    id = entry.id,
                    description = entry.description,
                    quantity = entry.quantity,
                    price = entry.price
                )
            }
        )
    }

    fun toDocument(invoice: InvoiceModel, customer: CustomerModel?, project: ProjectModel?): DocumentModel {
        val doc = DocumentModel().apply {
            type = DocumentType.INVOICE
            customerName = customer?.companyName ?: ""
            customerAddress = customer?.person?.details?.street ?: ""
            invoiceNumber = invoice.invoiceNumber
            invoiceDate   = invoice.invoiceDate
            dueDate       = invoice.dueDate
            invoiceStatus = invoice.invoiceStatus
            notes         = invoice.notes.joinToString("\n") { it.content }
            vatRate       = if (invoice.totalBrutto != 0.0)
                invoice.vatAmount / (invoice.totalBrutto - invoice.vatAmount) * 100
            else 0.0
            projectId     = invoice.projectId
            projectName   = project?.name ?: ""
            // createdAt bleibt auf Default (now)
        }

        doc.entries = invoice.items.map { item ->
            DocumentEntryModel().apply {
                document    = doc
                type        = item.type
                description = item.description
                quantity    = item.quantity
                price       = item.price
            }
        }.toMutableList()

        return doc
    }
}
