package ch.baunex.timetracking.model

enum class ApprovalStatus(val displayName: String) {
    PENDING("Ausstehend"),
    APPROVED("Genehmigt"),
    REJECTED("Abgelehnt"),
    CANCELED("Storniert"),
    UNDEFINED("Nicht definiert");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): ApprovalStatus =
            values().firstOrNull { it.displayName == name } ?: UNDEFINED
    }
}
