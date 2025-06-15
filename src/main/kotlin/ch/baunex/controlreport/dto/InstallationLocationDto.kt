package ch.baunex.controlreport.dto

import ch.baunex.project.model.ProjectType
import kotlinx.serialization.Serializable

@Serializable
data class InstallationLocationDto(
    val street: String,
    val postalCode: String,
    val city: String,
    val buildingType: ProjectType?,
    val parcelNumber: String?
)