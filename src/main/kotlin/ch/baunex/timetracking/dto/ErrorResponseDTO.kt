package ch.baunex.timetracking.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Standardized error response DTO for API errors
 */
@Serializable
data class ErrorResponseDTO(
    val timestamp: String,
    val error: String,
    val type: String,
    val field: String? = null,
    val value: String? = null,
    val details: String? = null
) {
    companion object {
        fun create(
            error: String,
            type: String,
            field: String? = null,
            value: String? = null,
            details: String? = null
        ): ErrorResponseDTO {
            return ErrorResponseDTO(
                timestamp = LocalDateTime.now().toString(),
                error = error,
                type = type,
                field = field,
                value = value,
                details = details
            )
        }
    }
} 