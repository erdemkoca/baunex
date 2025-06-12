package ch.baunex.documentGenerator.pdf

interface DocumentHtmlBuilder<T> {
    fun buildHtml(source: T): String
}
