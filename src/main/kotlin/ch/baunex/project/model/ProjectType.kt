package ch.baunex.project.model

enum class ProjectType(val displayName: String) {
    SINGLE_FAMILY_HOUSE("Einfamilienhaus"),
    TWO_FAMILY_HOUSE("Zweifamilienhaus"),
    TERRACED_HOUSE("Reihenhaus"),
    MULTI_FAMILY_HOUSE("Mehrfamilienhaus"),
    RESIDENTIAL_COMPLEX("Wohnanlage"),
    COMMERCIAL_BUILDING("Gewerbebau (Büro, Laden, Hotel)"),
    INDUSTRIAL_BUILDING("Industriebau (Fabrik, Lagerhalle)"),
    INFRASTRUCTURE("Infrastruktur (Trafostation, Straßenbeleuchtung)"),
    PUBLIC_BUILDING("Öffentliches Gebäude (Schule, Krankenhaus)"),
    RENEWABLE_ENERGY("Erneuerbare Energie (Photovoltaik, Windpark)"),
    AGRICULTURAL_BUILDING("Landwirtschaftliches Gebäude (Stall, Gewächshaus)"),
    TEMPORARY_INSTALLATION("Temporäre Installation (Baustelle, Event)"),
    DIVERSE("Sonstiges"),
    UNDEFINED("Nicht definiert");

    companion object {
        fun fromDisplayNameOrDefault(name: String?): ProjectType =
            values().firstOrNull { it.displayName == name } ?: UNDEFINED
    }
}

