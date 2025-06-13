package ch.baunex.user.model

import kotlinx.serialization.Serializable
import jakarta.persistence.Embeddable

@Embeddable
@Serializable
data class PersonDetails(
    var street: String? = null,
    var city: String? = null,
    var zipCode: String? = null,
    var country: String? = null,
    var phone: String? = null
)