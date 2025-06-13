package ch.baunex.invoice.dto

data class ProjectDTO(
    val id: Long,
    val name: String,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String? = null,
    val status: String
) 