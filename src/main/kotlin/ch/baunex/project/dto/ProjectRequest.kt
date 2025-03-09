package ch.baunex.project.dto

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ProjectRequest(
        var name: String = "",
        var budget: Int = 0,
        var client: String = "",
        //var contact: String? = "No Info",
        @Id @GeneratedValue var id: Long? = null
)
