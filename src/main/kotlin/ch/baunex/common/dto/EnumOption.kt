package ch.baunex.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class EnumOption(
    val code: String,
    val label: String
)