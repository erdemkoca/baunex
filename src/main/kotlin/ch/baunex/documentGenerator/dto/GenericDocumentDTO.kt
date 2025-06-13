package ch.baunex.documentGenerator.dto

import ch.baunex.documentGenerator.model.DocumentType

data class GenericDocumentDTO(
    val id: Long? = null,
    val type: DocumentType,
    val headerHtml: String?,                 // bereits gerendertes HTML für den Header
    val footerHtml: String?,                 // ebenso für den Footer
    val sections: List<DocumentSectionDTO>,  // beliebige Inhaltsblöcke
    val metadata: Map<String, String> = emptyMap()
    // in metadata können wir z.B. Rechnungsnummer, Datum etc. ablegen
)