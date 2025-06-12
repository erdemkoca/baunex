package ch.baunex.documentGenerator.dto

interface DocumentData {
    val headerMarkdown: String?
    val footerMarkdown: String?
    val entries: List<EntryData>
    // …
}