package ch.baunex.project.model

enum class ProjectType {
    // Wohnbauten
    SINGLE_FAMILY_HOUSE,    // Einfamilienhaus
    TWO_FAMILY_HOUSE,       // Zweifamilienhaus
    TERRACED_HOUSE,         // Reihenhaus
    MULTI_FAMILY_HOUSE,     // Mehrfamilienhaus
    RESIDENTIAL_COMPLEX,    // Wohnanlage (Stadthäuser, Wohnblöcke)

    // Gewerbliche Bauten
    COMMERCIAL_BUILDING,    // Büro, Laden, Hotel etc.

    // Industriebauten
    INDUSTRIAL_BUILDING,    // Fabrik, Lagerhalle

    // Infrastruktur
    INFRASTRUCTURE,         // Trafostation, Straßenbeleuchtung, Ladeinfrastruktur

    // Öffentliche Bauten
    PUBLIC_BUILDING,        // Schule, Krankenhaus, Rathaus

    // Erneuerbare Energien
    RENEWABLE_ENERGY,       // Photovoltaik, Windpark etc.

    // Landwirtschaft
    AGRICULTURAL_BUILDING,  // Stall, Gewächshaus

    // Temporäre / Mobile Installationen
    TEMPORARY_INSTALLATION, // Baustellen-Container, Event-Technik

    // Sonstiges / nicht definiert
    DIVERSE,
    UNDEFINED
}
