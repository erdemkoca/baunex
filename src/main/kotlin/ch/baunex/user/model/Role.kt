package ch.baunex.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("ADMIN") ADMIN,
    @SerialName("PROJECT_MANAGER") PROJECT_MANAGER,
    @SerialName("EMPLOYEE") EMPLOYEE,
    @SerialName("ELECTRICIAN") ELECTRICIAN,
    @SerialName("ACCOUNTANT") ACCOUNTANT,
    @SerialName("CLIENT") CLIENT
}
