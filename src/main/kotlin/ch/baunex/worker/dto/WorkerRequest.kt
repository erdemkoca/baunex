package ch.baunex.worker.dto

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class WorkerRequest(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phone: String = "",
    var position: String = "",
    var hourlyRate: Double = 0.0,
    @Id @GeneratedValue var id: Long? = null
) 