package ch.baunex.user.dto

data class CustomerContactCreateDTO(
    val firstName:   String,
    val lastName:    String,
    val email:       String?,
    val street:      String?,
    val city:        String?,
    val zipCode:     String?,
    val country:     String?,
    val phone:       String?,
    val role:        String?,
    val isPrimary:   Boolean = false
)