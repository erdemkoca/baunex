package ch.baunex.config.sample

import ch.baunex.project.dto.ProjectCreateDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectStatus
import ch.baunex.user.facade.CustomerFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

/**
 * Lädt Beispiel-Projekte und verknüpft sie mit bestehenden Kunden aus dem CustomerFacade.
 */
@ApplicationScoped
class SampleProjectLoader {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Transactional
    fun load() {
        // wenn schon Projekte existieren, abbrechen
        if (projectFacade.getAllProjects().isNotEmpty()) return

        val customers = customerFacade.listAll()
        val today = LocalDate.now()

        fun idOf(company: String): Long =
            customers.firstOrNull { it.companyName == company }
                ?.id ?: error("Kein Kunde mit companyName='$company' gefunden")

        val samples = listOf(
            ProjectCreateDTO(
                name        = "EFH Neubau Zürich",
                customerId  = idOf("Elektro Meier AG"),
                budget      = 45_000,
                startDate   = today.minusDays(30),
                endDate     = today.plusDays(90),
                description = "Elektroinstallation für neues Einfamilienhaus in Zürich.",
                status      = ProjectStatus.IN_PROGRESS,
                street      = "Weinbergstrasse 12",
                city        = "Zürich"
            ),
            ProjectCreateDTO(
                name        = "Ladestation Garage Basel",
                customerId  = idOf("E-Mobility Solutions GmbH"),
                budget      = 12_000,
                startDate   = today.minusDays(10),
                endDate     = today.plusDays(15),
                description = "Installation von 3 Ladepunkten in Tiefgarage.",
                status      = ProjectStatus.PLANNED,
                street      = "Steinenvorstadt 99",
                city        = "Basel"
            ),
            ProjectCreateDTO(
                name        = "Altbau-Umbau Luzern",
                customerId  = idOf("ImmoPro AG"),
                budget      = 32_000,
                startDate   = today.minusMonths(1),
                endDate     = today.plusMonths(1),
                description = "Modernisierung Elektroinstallation in MFH.",
                status      = ProjectStatus.IN_PROGRESS,
                street      = "Museggstrasse 3",
                city        = "Luzern"
            ),
            ProjectCreateDTO(
                name        = "Bürobeleuchtung Bern",
                customerId  = idOf("BüroTrend GmbH"),
                budget      = 8_500,
                startDate   = today.minusDays(5),
                endDate     = today.plusDays(7),
                description = "LED-Beleuchtung in Grossraumbüro.",
                status      = ProjectStatus.PLANNED,
                street      = "Bundesgasse 45",
                city        = "Bern"
            ),
            ProjectCreateDTO(
                name        = "Serverraum Elektro Zürich",
                customerId  = idOf("IT Solutions AG"),
                budget      = 19_000,
                startDate   = today.minusDays(20),
                endDate     = today.plusDays(10),
                description = "USV-Anlage und Stromversorgung für Serverraum.",
                status      = ProjectStatus.IN_PROGRESS,
                street      = "Badenerstrasse 101",
                city        = "Zürich"
            )
        )

        samples.forEach { projectFacade.createProject(it) }
    }
}
