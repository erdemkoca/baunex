package ch.baunex.config

import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.catalog.facade.CatalogFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleCatalogLoader {

    @Inject
    lateinit var catalogFacade: CatalogFacade

    @Transactional
    fun load() {
        if (catalogFacade.getAllItems().isNotEmpty()) return

        val items = listOf(
            CatalogItemDTO(name = "Steckdose AP weiss", unitPrice = 15.5, description = "Aufputz-Steckdose, Farbe weiss"),
            CatalogItemDTO(name = "Lichtschalter", unitPrice = 12.0, description = "Standard Lichtschalter, weiss"),
            CatalogItemDTO(name = "LED Panel 600x600", unitPrice = 89.0, description = "Einbau-LED Panel für Deckenmontage"),
            CatalogItemDTO(name = "Installationskabel 3x1.5mm²", unitPrice = 1.2, description = "Preis pro Meter"),
            CatalogItemDTO(name = "Sicherungsautomat B16", unitPrice = 19.5, description = "Leitungsschutzschalter, 1-polig, B-Charakteristik"),
            CatalogItemDTO(name = "Stundenansatz Elektroinstallateur", unitPrice = 95.0, description = "Dienstleistung, pro Stunde"),
            CatalogItemDTO(name = "Stundenansatz Lernender", unitPrice = 45.0, description = "Dienstleistung, pro Stunde (Lernender)")
        )

        items.forEach { catalogFacade.createItem(it) }
    }
}
