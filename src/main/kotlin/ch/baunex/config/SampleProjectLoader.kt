package ch.baunex.config.sample

import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectStatus
import ch.baunex.user.facade.CustomerFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import org.jboss.logging.Logger

/**
 * Lädt Beispiel-Projekte und verknüpft sie mit bestehenden Kunden aus dem CustomerFacade.
 */
@ApplicationScoped
class SampleProjectLoader {

    private val logger = Logger.getLogger(SampleProjectLoader::class.java)

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Transactional
    fun load() {
        // wenn schon Projekte existieren, abbrechen
        if (projectFacade.getAllProjects().isNotEmpty()) return

        val customers = customerFacade.listAll()
        logger.info("Found ${customers.size} customers: ${customers.map { it.companyName }}")

        val today = LocalDate.now()

        fun idOf(company: String): Long {
            val customer = customers.firstOrNull { it.companyName == company }
            if (customer == null) {
                logger.error("Customer not found: $company")
                error("Kein Kunde mit companyName='$company' gefunden")
            }
            logger.info("Found customer: ${customer.companyName} with ID ${customer.id}")
            return customer.id
        }

        val samples = listOf(
            ProjectCreateDTO(
                name        = "EFH Neubau Zürich",
                customerId  = idOf("Muster AG"),
                budget      = 45_000,
                startDate   = today.minusDays(30),
                endDate     = today.plusDays(90),
                description = "Elektroinstallation für neues Einfamilienhaus in Zürich.",
                status      = ProjectStatus.IN_PROGRESS,
                street      = "Weinbergstrasse 12",
                city        = "Zürich",
                initialNotes = listOf(
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(29),
                        updatedAt     = null,
                        title         = "Kundengespräch abgeschlossen",
                        content       = "Am 05.05.2025 mit Herrn Müller abgesprochen, dass die Elektroinstallation bis Ende August fertig sein soll.",
                        category      = NoteCategory.INFO,
                        tags          = listOf("Planung", "Kunde"),
                        attachments   = emptyList()
                    ),
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(25),
                        updatedAt     = null,
                        title         = "Materialbedarf prüfen",
                        content       = "Benötigen 50m NYM-J 5x1.5, 30m NYM-J 3x1.5, sowie 10 Sicherungsautomaten Typ B16.",
                        category      = NoteCategory.MATERIALBEDARF,
                        tags          = listOf("Material", "Einkauf"),
                        attachments   = emptyList()
                    )
                )
            ),
            ProjectCreateDTO(
                name        = "Ladestation Garage Basel",
                customerId  = idOf("Beispiel GmbH"),
                budget      = 12_000,
                startDate   = today.minusDays(10),
                endDate     = today.plusDays(15),
                description = "Installation von 3 Ladepunkten in Tiefgarage.",
                status      = ProjectStatus.PLANNED,
                street      = "Steinenvorstadt 99",
                city        = "Basel",
                initialNotes = listOf(
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(9),
                        updatedAt     = null,
                        title         = "Planung Vorerst",
                        content       = "Standort wurde besichtigt, Stromanschluss ist 63A vorhanden. Genehmigung durch Parkplatzbetreiber steht noch aus.",
                        category      = NoteCategory.VORBEREITUNG,
                        tags          = listOf("Planung", "Genehmigung"),
                        attachments   = emptyList()
                    )
                )
            ),
            ProjectCreateDTO(
                name        = "Altbau-Umbau Luzern",
                customerId  = idOf("Test SA"),
                budget      = 32_000,
                startDate   = today.minusMonths(1),
                endDate     = today.plusMonths(1),
                description = "Modernisierung Elektroinstallation in MFH.",
                status      = ProjectStatus.IN_PROGRESS,
                street      = "Museggstrasse 3",
                city        = "Luzern",
                initialNotes = listOf(
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(20),
                        updatedAt     = null,
                        title         = "Erster Rundgang",
                        content       = "Bestandsaufnahme alter Kabel und Verteilerschrank. Fotos angehängt für späteren Vergleich.",
                        category      = NoteCategory.SKIZZE,
                        tags          = listOf("Bestandsaufnahme"),
                        attachments   = emptyList()
                    ),
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(15),
                        updatedAt     = null,
                        title         = "Fehler im Altkasten",
                        content       = "Sicherungskasten ist zu klein, muss gegen neuen 13-Reihen-Schrank getauscht werden.",
                        category      = NoteCategory.FEHLER,
                        tags          = listOf("Altbau", "Kasten-Erneuerung"),
                        attachments   = emptyList()
                    )
                )
            ),
            ProjectCreateDTO(
                name        = "Bürobeleuchtung Bern",
                customerId  = idOf("Prova SRL"),
                budget      = 8_500,
                startDate   = today.minusDays(5),
                endDate     = today.plusDays(7),
                description = "LED-Beleuchtung in Grossraumbüro.",
                status      = ProjectStatus.PLANNED,
                street      = "Bundesgasse 45",
                city        = "Bern",
                initialNotes = listOf(
                    NoteDto(
                        id            = 0L,
                        projectId     = null,
                        timeEntryId   = null,
                        documentId    = null,
                        createdById   = 1L,
                        createdByName = "Max Mustermann",
                        createdAt     = LocalDate.now().minusDays(4),
                        updatedAt     = null,
                        title         = "Lichtkonzept erstellen",
                        content       = "Architekturbüro hat Anforderungen übermittelt: dimmbare LED-Panels gewünscht.",
                        category      = NoteCategory.VORBEREITUNG,
                        tags          = listOf("Konzept", "Architekt"),
                        attachments   = emptyList()
                    )
                )
            )
        )

        samples.forEach { projectFacade.createProject(it) }
    }
}