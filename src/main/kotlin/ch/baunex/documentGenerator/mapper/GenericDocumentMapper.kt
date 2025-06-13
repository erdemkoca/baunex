package ch.baunex.documentGenerator.mapper

import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.dto.DocumentSectionDTO
import ch.baunex.documentGenerator.dto.GenericDocumentDTO
import ch.baunex.documentGenerator.dto.TableDTO
import ch.baunex.documentGenerator.model.DocumentEntryModel
import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.model.DocumentType
import ch.baunex.documentGenerator.model.InvoiceDocumentModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GenericDocumentMapper {

    /**
     * Generic → Model: erzeugt je nach dto.type die richtige konkrete Subklasse.
     */
    fun toModel(dto: GenericDocumentDTO): DocumentModel {
        val doc: DocumentModel = when (dto.type) {
            DocumentType.INVOICE        -> InvoiceDocumentModel()
            // TODO weitere Typen hier ergänzen
            else                        -> throw IllegalArgumentException("Unsupported type ${dto.type}")
        }
        doc.type            = dto.type
        doc.headerMarkdown  = dto.headerHtml
        doc.footerMarkdown  = dto.footerHtml

        // Sektionen → flache Tabelle von Einträgen
        doc.entries = dto.sections
            .flatMap { it.table?.rows ?: emptyList() }
            .map { row ->
                DocumentEntryModel().apply {
                    document    = doc
                    description = row.getOrNull(0).orEmpty()
                    quantity    = row.getOrNull(1)?.toDoubleOrNull()
                    price       = row.getOrNull(2)?.toDoubleOrNull()
                    total       = row.getOrNull(3)?.toDoubleOrNull()
                }
            }
            .toMutableList()

        return doc
    }

    /**
     * Model → GenericResponse: bildet alle gemeinsamen Felder ab.
     */
    fun toResponseDTO(doc: DocumentModel): DocumentResponseDTO {
        // Wir packen alle entries in genau eine Tabelle als Beispiel:
        val table = TableDTO(
            headers = listOf("Beschreibung","Menge","Preis","Total"),
            rows    = doc.entries.map { e ->
                listOf(
                    e.description.orEmpty(),
                    e.quantity?.toString().orEmpty(),
                    e.price?.toString().orEmpty(),
                    e.total?.toString().orEmpty()
                )
            }
        )
        val section = DocumentSectionDTO(
            key         = "entries",
            title       = null,
            contentHtml = null,
            table       = table
        )

        return DocumentResponseDTO(
            id         = doc.id,
            type       = doc.type,
            createdAt  = doc.createdAt,
            headerHtml = doc.headerMarkdown,
            footerHtml = doc.footerMarkdown,
            sections   = listOf(section),
            metadata   = emptyMap()
        )
    }
}
