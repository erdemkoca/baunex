package ch.baunex.project.model

enum class ProjectType(val displayName: String) {
    SINGLE_FAMILY_HOUSE("Einfamilienhaus"),
    TWO_FAMILY_HOUSE("Zweifamilienhaus"),
    TERRACED_HOUSE("Reihenhaus"),
    MULTI_FAMILY_HOUSE("Mehrfamilienhaus"),
    RESIDENTIAL_COMPLEX("Wohnanlage"),

    // Gewerbliche Bauten
    COMMERCIAL_BUILDING("Gewerbebau (Büro, Laden, Hotel)"),

    // Industriebauten
    INDUSTRIAL_BUILDING("Industriebau (Fabrik, Lagerhalle)"),

    // Infrastruktur
    INFRASTRUCTURE("Infrastruktur (Trafostation, Straßenbeleuchtung)"),

    // Öffentliche Bauten
    PUBLIC_BUILDING("Öffentliches Gebäude (Schule, Krankenhaus)"),

    // Erneuerbare Energien
    RENEWABLE_ENERGY("Erneuerbare Energie (Photovoltaik, Windpark)"),

    // Landwirtschaft
    AGRICULTURAL_BUILDING("Landwirtschaftliches Gebäude (Stall, Gewächshaus)"),

    // Temporäre / Mobile Installationen
    TEMPORARY_INSTALLATION("Temporäre Installation (Baustelle, Event)"),

    // Sonstiges / nicht definiert
    DIVERSE("Sonstiges"),
    UNDEFINED("Nicht definiert")
}
