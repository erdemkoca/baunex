package ch.baunex.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class  CatalogItemDTO(
    val id: Long? = null,
    val name: String,
    val unitPrice: Double,
    val description: String? = null
)
