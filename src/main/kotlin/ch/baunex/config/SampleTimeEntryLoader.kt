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
            // Emergency Server Room Maintenance
            TimeEntryDTO(
                employeeId = 1,
                projectId = 1,
                date = LocalDate.now().minusDays(7),
                hoursWorked = 12.0,
                notes = emptyList(),
                hourlyRate = 75.0,
                billable = true,
                invoiced = true,
                hasNightSurcharge = true,
                hasWeekendSurcharge = true,
                travelTimeMinutes = 45,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 1,
                        quantity = 1,
                        itemName = "Serverraum-Klimaanlage",
                        unitPrice = 3500.0,
                        totalPrice = 3500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 2,
                        quantity = 2,
                        itemName = "Notfallkühlung",
                        unitPrice = 450.0,
                        totalPrice = 900.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 3,
                        quantity = 5,
                        itemName = "Temperatursensoren",
                        unitPrice = 120.0,
                        totalPrice = 600.0
                    )
                )
            ),
            // Complex Electrical Installation
            TimeEntryDTO(
                employeeId = 2,
                projectId = 2,
                date = LocalDate.now().minusDays(6),
                hoursWorked = 8.0,
                notes = emptyList(),
                hourlyRate = 65.0,
                billable = true,
                invoiced = false,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 4,
                        quantity = 1,
                        itemName = "Hauptverteiler",
                        unitPrice = 1200.0,
                        totalPrice = 1200.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 5,
                        quantity = 1,
                        itemName = "Notstromaggregat",
                        unitPrice = 4500.0,
                        totalPrice = 4500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 6,
                        quantity = 50,
                        itemName = "Kabelkanal",
                        unitPrice = 25.0,
                        totalPrice = 1250.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 7,
                        quantity = 200,
                        itemName = "Netzwerkkabel",
                        unitPrice = 3.50,
                        totalPrice = 700.0
                    )
                )
            ),
            // Holiday Emergency Repair
            TimeEntryDTO(
                employeeId = 3,
                projectId = 1,
                date = LocalDate.now().minusDays(5),
                hoursWorked = 6.0,
                notes = emptyList(),
                hourlyRate = 70.0,
                billable = true,
                invoiced = true,
                hasHolidaySurcharge = true,
                hasNightSurcharge = true,
                travelTimeMinutes = 60,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 8,
                        quantity = 1,
                        itemName = "Transformator",
                        unitPrice = 2800.0,
                        totalPrice = 2800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 9,
                        quantity = 10,
                        itemName = "Sicherungselemente",
                        unitPrice = 45.0,
                        totalPrice = 450.0
                    )
                )
            ),
            // Regular Maintenance with Waiting Time
            TimeEntryDTO(
                employeeId = 1,
                projectId = 2,
                date = LocalDate.now().minusDays(4),
                hoursWorked = 8.0,
                notes = emptyList(),
                hourlyRate = 65.0,
                billable = true,
                invoiced = true,
                hasWaitingTime = true,
                waitingTimeMinutes = 120,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 10,
                        quantity = 4,
                        itemName = "Klimaanlagenfilter",
                        unitPrice = 85.0,
                        totalPrice = 340.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 11,
                        quantity = 2,
                        itemName = "Kältemittel",
                        unitPrice = 150.0,
                        totalPrice = 300.0
                    )
                )
            ),
            // Complex Network Installation
            TimeEntryDTO(
                employeeId = 2,
                projectId = 1,
                date = LocalDate.now().minusDays(3),
                hoursWorked = 10.0,
                notes = emptyList(),
                hourlyRate = 70.0,
                billable = true,
                invoiced = false,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 12,
                        quantity = 2,
                        itemName = "Netzwerk-Switch",
                        unitPrice = 1200.0,
                        totalPrice = 2400.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 13,
                        quantity = 100,
                        itemName = "Cat7 Kabel",
                        unitPrice = 4.50,
                        totalPrice = 450.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 14,
                        quantity = 50,
                        itemName = "Netzwerkdosen",
                        unitPrice = 15.0,
                        totalPrice = 750.0
                    )
                )
            ),
            // Weekend Emergency Service
            TimeEntryDTO(
                employeeId = 3,
                projectId = 2,
                date = LocalDate.now().minusDays(2),
                hoursWorked = 8.0,
                notes = emptyList(),
                hourlyRate = 75.0,
                billable = true,
                invoiced = true,
                hasWeekendSurcharge = true,
                hasNightSurcharge = true,
                travelTimeMinutes = 30,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 15,
                        quantity = 1,
                        itemName = "Alarmanlage",
                        unitPrice = 1800.0,
                        totalPrice = 1800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 16,
                        quantity = 5,
                        itemName = "Bewegungsmelder",
                        unitPrice = 120.0,
                        totalPrice = 600.0
                    )
                )
            ),
            // Regular Workday with Multiple Tasks
            TimeEntryDTO(
                employeeId = 1,
                projectId = 1,
                date = LocalDate.now().minusDays(1),
                hoursWorked = 8.0,
                notes = emptyList(),
                hourlyRate = 65.0,
                billable = true,
                invoiced = false,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 17,
                        quantity = 10,
                        itemName = "LED-Leuchtmittel",
                        unitPrice = 35.0,
                        totalPrice = 350.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 18,
                        quantity = 5,
                        itemName = "Steckdosen",
                        unitPrice = 25.0,
                        totalPrice = 125.0
                    )
                )
            ),
            // Complex Security System Installation
            TimeEntryDTO(
                employeeId = 2,
                projectId = 2,
                date = LocalDate.now(),
                hoursWorked = 9.0,
                notes = emptyList(),
                hourlyRate = 70.0,
                billable = true,
                invoiced = false,
                catalogItems = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 19,
                        quantity = 1,
                        itemName = "Zutrittskontrollsystem",
                        unitPrice = 2500.0,
                        totalPrice = 2500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 20,
                        quantity = 8,
                        itemName = "Zutrittskartenleser",
                        unitPrice = 350.0,
                        totalPrice = 2800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 21,
                        quantity = 100,
                        itemName = "Zutrittskarten",
                        unitPrice = 15.0,
                        totalPrice = 1500.0
                    )
                )
            )
        )

        sampleEntries.forEach { timeTrackingFacade.logTime(it) }
    }
}
