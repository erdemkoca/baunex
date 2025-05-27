package ch.baunex.document.generator.model

import java.util.UUID

sealed class DocumentSection {
    abstract val id: UUID
    abstract val type: SectionType
    abstract val position: Int
    abstract val content: SectionContent
}

data class MarkdownSection(
    override val id: UUID = UUID.randomUUID(),
    override val position: Int,
    override val content: SectionContent.MarkdownContent
) : DocumentSection() {
    override val type: SectionType = SectionType.Markdown
}

data class TableSection(
    override val id: UUID = UUID.randomUUID(),
    override val position: Int,
    override val content: SectionContent.TableContent
) : DocumentSection() {
    override val type: SectionType = SectionType.Table
}

data class HeaderSection(
    override val id: UUID = UUID.randomUUID(),
    override val position: Int,
    override val content: SectionContent.MarkdownContent
) : DocumentSection() {
    override val type: SectionType = SectionType.Header
}

data class FooterSection(
    override val id: UUID = UUID.randomUUID(),
    override val position: Int,
    override val content: SectionContent.MarkdownContent
) : DocumentSection() {
    override val type: SectionType = SectionType.Footer
}

data class ImageSection(
    override val id: UUID = UUID.randomUUID(),
    override val position: Int,
    override val content: SectionContent.ImageContent
) : DocumentSection() {
    override val type: SectionType = SectionType.Image
} 