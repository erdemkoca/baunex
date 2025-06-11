package ch.baunex.config

import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.timetracking.dto.TimeEntryCatalogItemDTO
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
        val today = LocalDate.now()

        val sampleEntries = listOf(
            // 1) Emergency Server Room Maintenance
            TimeEntryDTO(
                employeeId         = 1,
                projectId          = 1,
                date               = today.minusDays(7),
                hoursWorked        = 12.0,
                title              = "Notfall-Einsatz Serverraum",  // hier übernehmen wir den Note-Titel
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 1L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Notfall-Einsatz Serverraum",
                        content       = "Stromausfall führte zu Ausfall der Klimaanlage. Wir mussten sofort Ersatzgenerator einsetzen.",
                        category      = NoteCategory.NOTFALL,
                        tags          = listOf("Emergency", "Serverraum"),
                        attachments   = emptyList(),
                        createdById = 1
                    )
                ),
                hourlyRate         = 75.0,
                billable           = true,
                invoiced           = true,
                hasNightSurcharge  = true,
                hasWeekendSurcharge= true,
                travelTimeMinutes  = 45,
                hasHolidaySurcharge= false,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 1,
                        quantity      = 1,
                        itemName      = "Serverraum-Klimaanlage",
                        unitPrice     = 3500.0,
                        totalPrice    = 3500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 2,
                        quantity      = 2,
                        itemName      = "Notfallkühlung",
                        unitPrice     = 450.0,
                        totalPrice    = 900.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 3,
                        quantity      = 5,
                        itemName      = "Temperatursensoren",
                        unitPrice     = 120.0,
                        totalPrice    = 600.0
                    )
                )
            ),

            // 2) Complex Electrical Installation
            TimeEntryDTO(
                employeeId         = 2,
                projectId          = 2,
                date               = today.minusDays(6),
                hoursWorked        = 8.0,
                title              = "Verteilerschrank Montage",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 2L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Verteilerschrank Montage",
                        content       = "Hauptverteiler installiert, Verkabelung geprüft. Kleinere Anpassungen am Sicherungskasten notwendig.",
                        category      = NoteCategory.INFO,
                        tags          = listOf("Verteiler", "Installation"),
                        attachments   = emptyList(),
                        createdById = 2
                    )
                ),
                hourlyRate         = 65.0,
                billable           = true,
                invoiced           = false,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 0,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 4,
                        quantity      = 1,
                        itemName      = "Hauptverteiler",
                        unitPrice     = 1200.0,
                        totalPrice    = 1200.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 5,
                        quantity      = 1,
                        itemName      = "Notstromaggregat",
                        unitPrice     = 4500.0,
                        totalPrice    = 4500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 6,
                        quantity      = 50,
                        itemName      = "Kabelkanal",
                        unitPrice     = 25.0,
                        totalPrice    = 1250.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 7,
                        quantity      = 200,
                        itemName      = "Netzwerkkabel",
                        unitPrice     = 3.50,
                        totalPrice    = 700.0
                    )
                )
            ),

            // 3) Holiday Emergency Repair
            TimeEntryDTO(
                employeeId         = 3,
                projectId          = 1,
                date               = today.minusDays(5),
                hoursWorked        = 6.0,
                title              = "Fehlerbehebung Feiertag",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 1L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Fehlerbehebung Feiertag",
                        content       = "Sicherung defekt, musste ausgetauscht werden. Kunde nicht vor Ort, Ersatzteil vorgehalten.",
                        category      = NoteCategory.FEHLER,
                        tags          = listOf("Feiertag", "Sicherung"),
                        attachments   = emptyList(),
                        createdById = 3
                    )
                ),
                hourlyRate         = 70.0,
                billable           = true,
                invoiced           = true,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= true,
                travelTimeMinutes  = 60,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 8,
                        quantity      = 1,
                        itemName      = "Transformator",
                        unitPrice     = 2800.0,
                        totalPrice    = 2800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 9,
                        quantity      = 10,
                        itemName      = "Sicherungselemente",
                        unitPrice     = 45.0,
                        totalPrice    = 450.0
                    )
                )
            ),

            // 4) Regular Maintenance with Waiting Time
            TimeEntryDTO(
                employeeId         = 1,
                projectId          = 2,
                date               = today.minusDays(4),
                hoursWorked        = 8.0,
                title              = "Warten auf Materialien",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 2L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Warten auf Materialien",
                        content       = "Lieferung der Klimaanlagenfilter verzögert sich um 2 Tage, Kunde informiert.",
                        category      = NoteCategory.MATERIALBEDARF,
                        tags          = listOf("Warten", "Filter"),
                        attachments   = emptyList(),
                        createdById = 1
                    )
                ),
                hourlyRate         = 65.0,
                billable           = true,
                invoiced           = true,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 0,
                hasWaitingTime     = true,
                waitingTimeMinutes = 120,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 10,
                        quantity      = 4,
                        itemName      = "Klimaanlagenfilter",
                        unitPrice     = 85.0,
                        totalPrice    = 340.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 11,
                        quantity      = 2,
                        itemName      = "Kältemittel",
                        unitPrice     = 150.0,
                        totalPrice    = 300.0
                    )
                )
            ),

            // 5) Complex Network Installation
            TimeEntryDTO(
                employeeId         = 2,
                projectId          = 1,
                date               = today.minusDays(3),
                hoursWorked        = 10.0,
                title              = "Netzwerkverkabelung",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 1L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Netzwerkverkabelung",
                        content       = "Cat7-Kabel verlegt, Ports getestet. Switch konfiguriert.",
                        category      = NoteCategory.INFO,
                        tags          = listOf("Netzwerk", "Switch"),
                        attachments   = emptyList(),
                        createdById = 2
                    )
                ),
                hourlyRate         = 70.0,
                billable           = true,
                invoiced           = false,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 0,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 12,
                        quantity      = 2,
                        itemName      = "Netzwerk-Switch",
                        unitPrice     = 1200.0,
                        totalPrice    = 2400.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 13,
                        quantity      = 100,
                        itemName      = "Cat7 Kabel",
                        unitPrice     = 4.50,
                        totalPrice    = 450.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 14,
                        quantity      = 50,
                        itemName      = "Netzwerkdosen",
                        unitPrice     = 15.0,
                        totalPrice    = 750.0
                    )
                )
            ),

            // 6) Weekend Emergency Service
            TimeEntryDTO(
                employeeId         = 3,
                projectId          = 2,
                date               = today.minusDays(2),
                hoursWorked        = 8.0,
                title              = "Dringender Kundeneinsatz",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 2L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Dringender Kundeneinsatz",
                        content       = "Alarmanlage defekt, Ersatzteil zufällig auf Lager, sofort eingebaut.",
                        category      = NoteCategory.NOTFALL,
                        tags          = listOf("Weekend", "Alarmanlage"),
                        attachments   = emptyList(),
                        createdById = 3
                    )
                ),
                hourlyRate         = 75.0,
                billable           = true,
                invoiced           = true,
                hasNightSurcharge  = true,
                hasWeekendSurcharge= true,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 30,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 15,
                        quantity      = 1,
                        itemName      = "Alarmanlage",
                        unitPrice     = 1800.0,
                        totalPrice    = 1800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 16,
                        quantity      = 5,
                        itemName      = "Bewegungsmelder",
                        unitPrice     = 120.0,
                        totalPrice    = 600.0
                    )
                )
            ),

            // 7) Regular Workday with Multiple Tasks
            TimeEntryDTO(
                employeeId         = 1,
                projectId          = 1,
                date               = today.minusDays(1),
                hoursWorked        = 8.0,
                title              = "Allgemeiner Check-up",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 1L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Allgemeiner Check-up",
                        content       = "Überprüfung der Steckdosen und Leitungen im gesamten Erdgeschoss.",
                        category      = NoteCategory.INFO,
                        tags          = listOf("Routine", "Check"),
                        attachments   = emptyList(),
                        createdById = 1
                    )
                ),
                hourlyRate         = 65.0,
                billable           = true,
                invoiced           = false,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 0,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 17,
                        quantity      = 10,
                        itemName      = "LED-Leuchtmittel",
                        unitPrice     = 35.0,
                        totalPrice    = 350.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 18,
                        quantity      = 5,
                        itemName      = "Steckdosen",
                        unitPrice     = 25.0,
                        totalPrice    = 125.0
                    )
                )
            ),

            // 8) Complex Security System Installation
            TimeEntryDTO(
                employeeId         = 2,
                projectId          = 2,
                date               = today,
                hoursWorked        = 9.0,
                title              = "Zutrittskontrolle konfiguriert",
                notes              = listOf(
                    NoteCreateDto(
                        id            = 0L,
                        projectId     = 2L,
                        timeEntryId   = null,
                        documentId    = null,
                        title         = "Zutrittskontrolle konfiguriert",
                        content       = "Alle Lesegeräte eingerichtet, Karten funktionieren.",
                        category      = NoteCategory.INFO,
                        tags          = listOf("Sicherheit", "Konfiguration"),
                        attachments   = emptyList(),
                        createdById = 2
                    )
                ),
                hourlyRate         = 70.0,
                billable           = true,
                invoiced           = false,
                hasNightSurcharge  = false,
                hasWeekendSurcharge= false,
                hasHolidaySurcharge= false,
                travelTimeMinutes  = 0,
                hasWaitingTime     = false,
                waitingTimeMinutes = 0,
                disposalCost       = 0.0,
                catalogItems       = listOf(
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 19,
                        quantity      = 1,
                        itemName      = "Zutrittskontrollsystem",
                        unitPrice     = 2500.0,
                        totalPrice    = 2500.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 20,
                        quantity      = 8,
                        itemName      = "Zutrittskartenleser",
                        unitPrice     = 350.0,
                        totalPrice    = 2800.0
                    ),
                    TimeEntryCatalogItemDTO(
                        catalogItemId = 21,
                        quantity      = 100,
                        itemName      = "Zutrittskarten",
                        unitPrice     = 15.0,
                        totalPrice    = 1500.0
                    )
                )
            )
        )

        // Alle Sample-Einträge speichern
        sampleEntries.forEach { timeTrackingFacade.logTime(it) }
    }
}
