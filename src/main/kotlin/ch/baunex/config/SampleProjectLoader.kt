package ch.baunex.config.sample

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectStatus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class SampleProjectLoader {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Transactional
    fun load() {
        if (projectFacade.getAllProjects().isNotEmpty()) return

        val today = LocalDate.now()

        val sampleProjects = listOf(
            ProjectDTO(
                name = "EFH Neubau Zürich",
                client = "Elektro Meier AG",
                budget = 45000,
                contact = "Hans Meier, h.meier@elektromeier.ch",
                startDate = today.minusDays(30),
                endDate = today.plusDays(90),
                description = "Elektroinstallation für ein neues Einfamilienhaus (EFH) in Zürich.",
                status = ProjectStatus.IN_PROGRESS,
                street = "Weinbergstrasse 12",
                city = "Zürich"
            ),
            ProjectDTO(
                name = "Ladestation Garage Basel",
                client = "E-Mobility Solutions GmbH",
                budget = 12000,
                contact = "Stefan Burri, sburri@emobility.ch",
                startDate = today.minusDays(10),
                endDate = today.plusDays(15),
                description = "Installation von 3 Ladepunkten für Elektrofahrzeuge in einer Tiefgarage.",
                status = ProjectStatus.PLANNED,
                street = "Steinenvorstadt 99",
                city = "Basel"
            ),
            ProjectDTO(
                name = "Altbau-Umbau Luzern",
                client = "ImmoPro AG",
                budget = 32000,
                contact = "Claudia Haller, challer@immopro.ch",
                startDate = today.minusMonths(1),
                endDate = today.plusMonths(1),
                description = "Modernisierung der Elektroinstallation in einem Mehrfamilienhaus (MFH).",
                status = ProjectStatus.IN_PROGRESS,
                street = "Museggstrasse 3",
                city = "Luzern"
            ),
            ProjectDTO(
                name = "Bürobeleuchtung Bern",
                client = "BüroTrend GmbH",
                budget = 8500,
                contact = "Patrick Frei, pfrei@buerotrend.ch",
                startDate = today.minusDays(5),
                endDate = today.plusDays(7),
                description = "Planung und Installation energieeffizienter LED-Beleuchtung in einem Grossraumbüro.",
                status = ProjectStatus.PLANNED,
                street = "Bundesgasse 45",
                city = "Bern"
            ),
            ProjectDTO(
                name = "Serverraum Elektro Zürich",
                client = "IT Solutions AG",
                budget = 19000,
                contact = "Martina Keller, mkeller@itsolutions.ch",
                startDate = today.minusDays(20),
                endDate = today.plusDays(10),
                description = "Spezialisierte Stromversorgung und USV-Anlage für Serverraum.",
                status = ProjectStatus.IN_PROGRESS,
                street = "Badenerstrasse 101",
                city = "Zürich"
            )
        )

        sampleProjects.forEach { projectFacade.createProject(it) }
    }
}