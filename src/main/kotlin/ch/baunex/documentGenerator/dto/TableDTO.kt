package ch.baunex.documentGenerator.dto

data class TableDTO(
    val headers: List<String>,               // Spaltenüberschriften
    val rows: List<List<String>>             // jede Zeile ist eine Liste von Zellen (Strings)
)