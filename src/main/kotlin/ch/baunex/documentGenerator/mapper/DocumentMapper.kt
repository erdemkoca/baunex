package ch.baunex.documentGenerator.mapper

import ch.baunex.documentGenerator.dto.DocumentDTO
import ch.baunex.documentGenerator.model.DocumentEntryModel
import ch.baunex.documentGenerator.model.DocumentModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentMapper {

    fun toDocumentModel(dto: DocumentDTO): DocumentModel {
        val doc = DocumentModel().apply {
            type = dto.type
            customerName = dto.customerName
            markdownHeader = dto.markdownHeader
            markdownFooter = dto.markdownFooter
        }

        doc.entries = dto.entries.map {
            DocumentEntryModel().apply {
                document = doc
                description = it.description
                quantity = it.quantity
                price = it.price
            }
        }.toMutableList()


        return doc
    }
}
