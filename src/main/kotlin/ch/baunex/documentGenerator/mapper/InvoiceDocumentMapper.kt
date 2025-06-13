package ch.baunex.documentGenerator.mapper

import ch.baunex.company.repository.CompanyRepository
import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import ch.baunex.documentGenerator.model.DocumentType
import ch.baunex.documentGenerator.model.DocumentEntryModel
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.project.model.ProjectModel
import ch.baunex.user.model.CustomerModel
import ch.baunex.documentGenerator.dto.invoice.InvoiceDocumentResponseDTO
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceDocumentMapper @Inject constructor(
    private val generic: GenericDocumentMapper,
    private val companyRepo: CompanyRepository
) {

    /**
     * Baut aus InvoiceModel (+ Kunde/Projekt) ein InvoiceDocumentModel.
     */
    fun toModel(
        inv: InvoiceModel,
        cust: CustomerModel?,
        proj: ProjectModel?
    ): InvoiceDocumentModel {
        val doc = InvoiceDocumentModel().apply {
            type            = DocumentType.INVOICE
            customerName    = cust?.companyName.orEmpty()
            customerAddress = buildAddress(cust).orEmpty()
            invoiceNumber   = inv.invoiceNumber
            invoiceDate     = inv.invoiceDate
            dueDate         = inv.dueDate
            invoiceStatus   = inv.invoiceStatus
            vatRate         = companyRepo.findFirst()?.defaultVatRate

            // Projekt-Infos
            projectId       = proj?.id
            projectName     = proj?.name
        }

        // Firmen-Daten und AGB/Footers
        companyRepo.findFirst()?.let {
            doc.companyName    = it.name
            doc.companyAddress = it.street
            doc.companyZip     = it.zipCode
            doc.companyCity    = it.city
            doc.companyPhone   = it.phone
            doc.companyEmail   = it.email
            doc.companyLogo    = it.logo
            doc.terms          = it.defaultInvoiceTerms
            doc.footer         = buildString {
                appendLine("Bankverbindung")
                appendLine("${it.bankName.orEmpty()}")
                appendLine("IBAN: ${it.iban.orEmpty()}")
                appendLine("BIC: ${it.bic.orEmpty()}")
                if (!it.vatNumber.isNullOrBlank()) appendLine("MWST-Nr: ${it.vatNumber}")
                if (!it.taxNumber.isNullOrBlank()) appendLine("Steuer-Nr: ${it.taxNumber}")
            }
        }

        // Positionen
        doc.entries = inv.items.map { item ->
            DocumentEntryModel().apply {
                document    = doc
                type        = item.type
                description = item.description
                quantity    = item.quantity
                price       = item.price
                total       = (item.quantity ?: 0.0) * (item.price ?: 0.0)
            }
        }.toMutableList()

        // Summen
        doc.totalNetto  = inv.totalNetto
        doc.vatAmount   = inv.vatAmount
        doc.totalBrutto = inv.totalBrutto

        return doc
    }

    /**
     * Wandelt ein InvoiceDocumentModel in das API-Response‐DTO um.
     * Nutzt den GenericDocumentMapper, um die tabellarischen Sections zu erzeugen.
     */
    fun toResponseDTO(model: InvoiceDocumentModel): InvoiceDocumentResponseDTO {
        // Erst generische Sections/Metadata
        val genericResp = generic.toResponseDTO(model)

        return InvoiceDocumentResponseDTO(
            id              = model.id,
            invoiceNumber   = model.invoiceNumber,
            invoiceDate     = model.invoiceDate,
            dueDate         = model.dueDate,
            invoiceStatus   = model.invoiceStatus?.name,
            customerName    = model.customerName,
            customerAddress = model.customerAddress,
            projectName     = model.projectName,
            totalNet        = model.totalNetto,
            vatAmount       = model.vatAmount,
            totalGross      = model.totalBrutto,
            sections        = genericResp.sections,
            metadata        = genericResp.metadata + mapOf(
                "companyName" to (model.companyName.orEmpty()),
                "companyCity" to (model.companyCity.orEmpty())
            )
        )
    }

    /** Hilfsfunktion, um Adresse aus dem CustomerModel zusammenzubauen */
    private fun buildAddress(cust: CustomerModel?): String? =
        cust?.person?.details?.let {
            listOfNotNull(it.street, it.zipCode, it.city)
                .joinToString(", ")
        }
}
