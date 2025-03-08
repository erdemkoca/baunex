package ch.baunex.project.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    var projects: List<ProjectRequest>
)