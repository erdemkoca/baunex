package ch.baunex.timetracking.model

enum class HolidayType(val displayName: String) {
    PAID_VACATION("Bezahlter Urlaub"),
    UNPAID_LEAVE("Unbezahlter Urlaub"),
    SICK_LEAVE("Krankheit"),
    PUBLIC_HOLIDAY("Feiertag"),
    SPECIAL_LEAVE("Sonderurlaub"),
    UNDEFINED("Nicht definiert");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): HolidayType =
            values().firstOrNull { it.displayName == name } ?: UNDEFINED
    }
}
