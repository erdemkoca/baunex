package ch.baunex.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectCatalogItemDTO(
    val id: Long? = null,
    val projectId: Long,
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val catalogItemId: Long? = null
)
