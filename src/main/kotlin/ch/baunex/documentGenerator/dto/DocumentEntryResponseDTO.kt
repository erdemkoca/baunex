package ch.baunex.documentGenerator.dto

data class DocumentEntryResponseDTO(
    val id: Long,
    val description: String?,
    val quantity: Double?,
    val price: Double?
)
