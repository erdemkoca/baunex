package ch.baunex.document.generator.model

import java.time.LocalDateTime
import java.util.UUID

interface DocumentTemplate {
    val id: UUID
    val type: DocumentType
    val sections: List<DocumentSection>
    val metadata: DocumentMetadata
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

data class DocumentMetadata(
    val title: String,
    val description: String? = null,
    val author: String,
    val company: String,
    val version: String = "1.0"
)

sealed class SectionType {
    object Markdown : SectionType()
    object Table : SectionType()
    object Header : SectionType()
    object Footer : SectionType()
    object Image : SectionType()
}

sealed class SectionContent {
    data class MarkdownContent(
        val markdown: String,
        val style: MarkdownStyle = MarkdownStyle()
    ) : SectionContent()

    data class TableContent(
        val headers: List<String>,
        val rows: List<Map<String, Any>>,
        val schema: TableSchema,
        val style: TableStyle = TableStyle()
    ) : SectionContent()

    data class ImageContent(
        val url: String,
        val alt: String? = null,
        val width: Int? = null,
        val height: Int? = null
    ) : SectionContent()
}

data class MarkdownStyle(
    val fontSize: Int = 12,
    val fontFamily: String = "Arial",
    val alignment: TextAlignment = TextAlignment.LEFT
)

data class TableStyle(
    val headerBackground: String = "#f0f0f0",
    val borderColor: String = "#000000",
    val borderWidth: Int = 1,
    val cellPadding: Int = 5
)

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

interface TableSchema {
    val columns: List<ColumnDefinition>
}

data class ColumnDefinition(
    val name: String,
    val type: ColumnType,
    val required: Boolean = false,
    val validation: ValidationRule? = null
)

enum class ColumnType {
    TEXT, NUMBER, CURRENCY, DATE, BOOLEAN
}

interface ValidationRule {
    fun validate(value: Any): Boolean
} 