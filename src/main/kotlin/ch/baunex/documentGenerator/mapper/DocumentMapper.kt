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
import ch.baunex.company.model.CompanyModel
import ch.baunex.company.repository.CompanyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.format.DateTimeFormatter

@ApplicationScoped
class DocumentMapper {

    @Inject
    lateinit var companyRepository: CompanyRepository

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
        // Hole die Firmeninformationen
        val company = companyRepository.findFirst()

        val doc = DocumentModel().apply {
            type = DocumentType.INVOICE
            customerName = customer?.companyName ?: ""
            customerAddress = buildString {
                append(customer?.person?.details?.street ?: "")
                append(", ")
                append(customer?.person?.details?.zipCode ?: "")
                append(" ")
                append(customer?.person?.details?.city ?: "")
            }
            customerZip = customer?.person?.details?.zipCode
            customerCity = customer?.person?.details?.city
            invoiceNumber = invoice.invoiceNumber
            invoiceDate = invoice.invoiceDate
            dueDate = invoice.dueDate
            invoiceStatus = invoice.invoiceStatus
            notes = invoice.notes.joinToString("\n") { it.content }
            vatRate = company?.defaultVatRate ?: 8.1
            projectId = invoice.projectId
            projectName = project?.name ?: ""

            // Set company information
            company?.let {
                companyName = it.name
                companyAddress = it.street
                companyZip = it.zipCode
                companyCity = it.city
                companyPhone = it.phone
                companyEmail = it.email
                terms = it.defaultInvoiceTerms
                footer = buildString {
                    append("Bankverbindung\n")
                    append("${it.bankName ?: ""}  \n")
                    append("IBAN: ${it.iban ?: ""}  \n")
                    append("BIC: ${it.bic ?: ""}  \n")
                    if (!it.vatNumber.isNullOrBlank()) append("MWST-Nr: ${it.vatNumber}  \n")
                    if (!it.taxNumber.isNullOrBlank()) append("Steuer-Nr: ${it.taxNumber}  \n")
                }
            }

            // Set document information
            documentNumber = invoice.invoiceNumber
            documentDate = invoice.invoiceDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        // Füge die Rechnungspositionen hinzu
        doc.entries = invoice.items.map { item ->
            DocumentEntryModel().apply {
                document = doc
                type = item.type
                description = item.description
                quantity = item.quantity
                price = item.price
                total = item.quantity * item.price
            }
        }.toMutableList()

        // Setze die Summen im Dokument
        doc.totalNetto = invoice.totalNetto
        doc.vatAmount = invoice.vatAmount
        doc.totalBrutto = invoice.totalBrutto

        return doc
    }
}
