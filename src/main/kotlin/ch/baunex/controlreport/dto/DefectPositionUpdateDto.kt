package ch.baunex.controlreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class DefectPositionUpdateDto(
    val id: Long?,
    val normReferences: List<String>,
    val noteId: Long?,               // referenziert die Note
    val noteContent: String         // neuer Inhalt der Note
)