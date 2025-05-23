package ch.baunex.config

import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
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
            // Regular workday entry with catalog items
            TimeEntryDTO(
                employeeId = 1,
                projectId = 1,
                date = LocalDate.now().minusDays(5),
                hoursWorked = 8.0,
                note = "Elektroinstallation Bürotrakt - Hauptverteilung",
                hourlyRate = 65.0,
                billable = true,
                invoiced = false,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 1,
                        quantity = 2,
                        itemName = "Hauptverteiler",
                        unitPrice = 450.0,
                        totalPrice = 900.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 2,
                        quantity = 10,
                        itemName = "Kabelkanal",
                        unitPrice = 25.0,
                        totalPrice = 250.0
                    )
                )
            ),
            // Weekend work with night surcharge
            TimeEntryDTO(
                employeeId = 2,
                projectId = 1,
                date = LocalDate.now().minusDays(4),
                hoursWorked = 6.0,
                note = "Notfallreparatur - Stromausfall",
                hourlyRate = 70.0,
                billable = true,
                invoiced = true,
                hasWeekendSurcharge = true,
                hasNightSurcharge = true,
                travelTimeMinutes = 45,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 3,
                        quantity = 1,
                        itemName = "Notstromaggregat",
                        unitPrice = 1200.0,
                        totalPrice = 1200.0
                    )
                )
            ),
            // Holiday work with disposal
            TimeEntryDTO(
                employeeId = 3,
                projectId = 2,
                date = LocalDate.now().minusDays(3),
                hoursWorked = 7.5,
                note = "Altkabel Entsorgung und Neuverkabelung",
                hourlyRate = 55.0,
                billable = true,
                invoiced = false,
                hasHolidaySurcharge = true,
                disposalCost = 250.0,
                travelTimeMinutes = 30,
                waitingTimeMinutes = 45,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 4,
                        quantity = 100,
                        itemName = "Netzwerkkabel",
                        unitPrice = 3.50,
                        totalPrice = 350.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 5,
                        quantity = 20,
                        itemName = "Netzwerkdosen",
                        unitPrice = 15.0,
                        totalPrice = 300.0
                    )
                )
            ),
            // Regular workday with waiting time
            TimeEntryDTO(
                employeeId = 1,
                projectId = 2,
                date = LocalDate.now().minusDays(2),
                hoursWorked = 8.0,
                note = "Wartung Klimaanlage",
                hourlyRate = 65.0,
                billable = true,
                invoiced = true,
                hasWaitingTime = true,
                waitingTimeMinutes = 120,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 6,
                        quantity = 1,
                        itemName = "Klimaanlagenfilter",
                        unitPrice = 85.0,
                        totalPrice = 85.0
                    )
                )
            ),
            // Non-billable internal work
            TimeEntryDTO(
                employeeId = 2,
                projectId = 1,
                date = LocalDate.now().minusDays(1),
                hoursWorked = 4.0,
                note = "Interne Schulung - Neue Sicherheitsrichtlinien",
                hourlyRate = 70.0,
                billable = false,
                invoiced = false
            ),
            // Complex project with multiple surcharges
            TimeEntryDTO(
                employeeId = 3,
                projectId = 2,
                date = LocalDate.now(),
                hoursWorked = 10.0,
                note = "Notfallwartung Serverraum - Klimaanlage ausgefallen",
                hourlyRate = 55.0,
                billable = true,
                invoiced = false,
                hasNightSurcharge = true,
                hasWeekendSurcharge = true,
                hasHolidaySurcharge = true,
                travelTimeMinutes = 60,
                disposalCost = 150.0,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 7,
                        quantity = 1,
                        itemName = "Serverraum-Klimaanlage",
                        unitPrice = 3500.0,
                        totalPrice = 3500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 8,
                        quantity = 2,
                        itemName = "Notfallkühlung",
                        unitPrice = 450.0,
                        totalPrice = 900.0
                    )
                )
            )
        )

        sampleEntries.forEach { timeTrackingFacade.logTime(it) }
    }
}
