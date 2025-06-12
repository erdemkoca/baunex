package ch.baunex.documentGenerator.dto

data class DocumentSectionDTO(
    val key: String,
    val title: String?,
    val contentHtml: String? = null,
    val table: TableDTO? = null
)