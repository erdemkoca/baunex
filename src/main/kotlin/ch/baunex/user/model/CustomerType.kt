package ch.baunex.user.model

enum class CustomerType(val displayName: String) {
    OWNER("Eigentümer"),
    PROPERTY_MANAGER("Verwaltung"),
    UTILITY_PROVIDER("Stromversorger"),
    ELECTRICIAN("Elektro-Installateur/Betrieb"),
    GENERAL_CONTRACTOR("Generalunternehmer"),
    SUBCONTRACTOR("Subunternehmer / Fachfirma"),
    MAINTENANCE_PROVIDER("Wartungs- oder Servicefirma"),
    TENANT("Endkunde oder Mieter"),
    INSPECTION_BODY("Externe Prüf- oder Kontrollstelle"),
    UNDEFINED("-");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): CustomerType =
            values().firstOrNull { it.displayName == name } ?: UNDEFINED
    }
}