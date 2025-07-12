package ch.baunex.timetracking.model

enum class HolidayType(val displayName: String) {
    PAID_VACATION("Bezahlter Urlaub"),
    UNPAID_LEAVE("Unbezahlter Urlaub"),
    SICK_LEAVE("Krankheit"),
    PUBLIC_HOLIDAY("Feiertag"),
    SPECIAL_LEAVE("Sonderurlaub"),
    UNDEFINED("Nicht definiert");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): HolidayType {
            if (name == null) return UNDEFINED
            
            // First try to find by display name
            values().firstOrNull { it.displayName == name }?.let { return it }
            
            // If not found, try to find by enum name (for backward compatibility)
            try {
                return valueOf(name)
            } catch (e: IllegalArgumentException) {
                return UNDEFINED
            }
        }
    }
}
