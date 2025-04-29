package ch.baunex.config

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.TimeTrackingFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class SampleTimeEntryLoader {

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Transactional
    fun load() {
        val sampleEntries = listOf(
            TimeEntryDTO(
                employeeId = 1,
                projectId = 1,
                date = LocalDate.now().minusDays(2),
                hoursWorked = 7.5,
                note = "Elektroinstallation Bürotrakt",
                hourlyRate = 65.0,
                billable = true,
                invoiced = false,
                catalogItemDescription = "Steckdosenmontage",
                catalogItemPrice = 120.0
            ),
            TimeEntryDTO(
                employeeId = 2,
                projectId = 1,
                date = LocalDate.now().minusDays(1),
                hoursWorked = 8.0,
                note = "Netzwerkkabel verlegen",
                hourlyRate = 70.0,
                billable = true,
                invoiced = true,
                catalogItemDescription = "LAN Verkabelung",
                catalogItemPrice = 90.0
            ),
            TimeEntryDTO(
                employeeId = 3,
                projectId = 2,
                date = LocalDate.now(),
                hoursWorked = 6.0,
                note = "Kleinmaterial vorbereiten",
                hourlyRate = 55.0,
                billable = false,
                invoiced = false
            )
        )

        sampleEntries.forEach { timeTrackingFacade.logTime(it) }
    }
}
