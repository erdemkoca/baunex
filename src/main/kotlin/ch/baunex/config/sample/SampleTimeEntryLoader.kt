package ch.baunex.config.sample

import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.model.NoteCategory
import ch.baunex.timetracking.dto.ApprovalDTO
import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.dto.TimeEntryDTO
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.facade.TimeTrackingFacade
import ch.baunex.timetracking.service.HolidayTypeService
import ch.baunex.user.facade.EmployeeFacade
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Sample time entry loader - DEV ONLY
 * This class can be safely removed before production release.
 */
@IfBuildProfile("dev")
@ApplicationScoped
class SampleTimeEntryLoader {

    @Inject
    lateinit var timeTrackingFacade: TimeTrackingFacade

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var holidayTypeService: HolidayTypeService

    // Konfigurierbare Parameter
    private val weeksToGenerate = 10
    private val minEntriesPerDay = 1
    private val maxEntriesPerDay = 3
    private val minHoursPerDayPerProject = 2.0
    private val maxHoursPerDayPerProject = 4.0
    private val projectIds = listOf(1, 2, 3, 4, 5, 6, 7)
    private val holidayChance = 0.05 // 5% Wahrscheinlichkeit für Urlaubstag
    private val workDayStart = LocalTime.of(8, 0)
    private val workDayEnd = LocalTime.of(17, 0)

    // Sample-Titel für verschiedene Arbeitstypen
    private val workTitles = listOf(
        "Elektroinstallation",
        "Wartung Klimaanlage",
        "Netzwerkverkabelung",
        "Sicherheitssystem",
        "Beleuchtungsanlage",
        "Verteilerschrank",
        "Ladestation",
        "Smart-Home Setup",
        "Photovoltaik",
        "Alarmanlage",
        "Zutrittskontrolle",
        "Kabelverlegung",
        "Schaltanlage",
        "Notstromaggregat",
        "Temperatursensoren"
    )

    // Sample-Notizen für verschiedene Kategorien
    private val sampleNotes = mapOf(
        NoteCategory.INFO to listOf(
            "Routine-Überprüfung durchgeführt",
            "Installation erfolgreich abgeschlossen",
            "System funktioniert einwandfrei",
            "Kunde mit Arbeit zufrieden",
            "Dokumentation erstellt"
        ),
        NoteCategory.FEHLER to listOf(
            "Kleiner Defekt behoben",
            "Ersatzteil eingebaut",
            "Problem gelöst",
            "Sicherung ausgetauscht",
            "Kabelbruch repariert"
        ),
        NoteCategory.MATERIALBEDARF to listOf(
            "Material nachbestellt",
            "Lieferung erwartet",
            "Ersatzteile geordert",
            "Kabel nachgemessen",
            "Zubehör besorgt"
        ),
        NoteCategory.NOTFALL to listOf(
            "Notfall-Einsatz",
            "Dringende Reparatur",
            "Sofortmaßnahme",
            "Kritischer Defekt",
            "Sicherheitsproblem"
        )
    )

    @Transactional
    fun load() {
        val employees = employeeFacade.listAll()
        
        employees.forEach { employee ->
            generateTimeEntriesForEmployee(employee)
        }
    }

    private fun generateTimeEntriesForEmployee(employee: ch.baunex.user.dto.EmployeeDTO) {
        // Startdatum = Ende der letzten Woche (vor 10 Wochen)
        val endDate = LocalDate.now().minusWeeks(1) // Letzte Woche
        val startDate = endDate.minusWeeks(weeksToGenerate.toLong() - 1) // 10 Wochen zurück
        
        var currentDate = startDate

        for (week in 1..weeksToGenerate) {
            // Innerhalb einer Woche: Montag bis Freitag
            val weekDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            for (dayOfWeek in weekDays) {
                // Nächstes Datum auf diesen Wochentag verschieben
                currentDate = currentDate.with(TemporalAdjusters.nextOrSame(dayOfWeek))
                
                // Prüfen ob es ein Urlaubstag werden soll
                if (Math.random() < holidayChance) {
                    createHoliday(employee, currentDate)
                } else {
                    createMultipleTimeEntries(employee, currentDate)
                }
                
                // Zum nächsten Tag
                currentDate = currentDate.plusDays(1)
            }
        }
    }

    private fun createHoliday(employee: ch.baunex.user.dto.EmployeeDTO, date: LocalDate) {
        // Lade Holiday-Types aus der Datenbank
        val holidayTypes = holidayTypeService.getActiveHolidayTypes()
        
        if (holidayTypes.isEmpty()) {
            println("WARNING: No holiday types found in database, skipping holiday creation")
            return
        }
        
        // Erstelle Wahrscheinlichkeitsverteilung basierend auf Holiday-Types
        val holidayTypesWithProbabilities = mutableMapOf<String, Double>()
        
        // Standard-Wahrscheinlichkeiten für bekannte Typen
        val defaultProbabilities = mapOf(
            "PAID_VACATION" to 0.45,     // 45% - häufigster Typ
            "SICK_LEAVE" to 0.25,        // 25% - relativ häufig
            "UNPAID_LEAVE" to 0.15,      // 15% - gelegentlich
            "SPECIAL_LEAVE" to 0.08,     // 8% - selten
            "COMPENSATORY_TIME" to 0.04, // 4% - selten
            "MATERNITY_LEAVE" to 0.015,  // 1.5% - sehr selten
            "PATERNITY_LEAVE" to 0.015   // 1.5% - sehr selten
        )
        
        // Verwende Standard-Wahrscheinlichkeiten für bekannte Typen, sonst 0.01
        holidayTypes.forEach { holidayType ->
            val probability = defaultProbabilities[holidayType.code] ?: 0.01
            holidayTypesWithProbabilities[holidayType.code] = probability
        }
        
        // Normalisiere Wahrscheinlichkeiten (sollten sich zu 1.0 addieren)
        val totalProbability = holidayTypesWithProbabilities.values.sum()
        holidayTypesWithProbabilities.replaceAll { _, value -> value / totalProbability }
        
        // Wähle Holiday-Type basierend auf Wahrscheinlichkeiten
        val random = Math.random()
        var cumulativeProbability = 0.0
        var selectedType = holidayTypes.first().code // Fallback auf ersten verfügbaren Typ
        
        for ((type, probability) in holidayTypesWithProbabilities) {
            cumulativeProbability += probability
            if (random <= cumulativeProbability) {
                selectedType = type
                break
            }
        }
        
        // Gründe für verschiedene Holiday-Types
        val reasonsByType = mapOf(
            "PAID_VACATION" to listOf(
                "Sommerurlaub",
                "Winterurlaub",
                "Familienurlaub",
                "Erholungsurlaub",
                "Urlaub mit Freunden",
                "Kurzer Ausflug"
            ),
            "SICK_LEAVE" to listOf(
                "Erkältung",
                "Grippe",
                "Migräne",
                "Rückenprobleme",
                "Magen-Darm",
                "Arzttermin"
            ),
            "UNPAID_LEAVE" to listOf(
            "Persönliche Angelegenheit",
                "Familienangelegenheit",
                "Reise ohne Urlaub",
                "Studium",
                "Weiterbildung"
            ),
            "SPECIAL_LEAVE" to listOf(
                "Hochzeit",
                "Beerdigung",
                "Geburt",
                "Umzug",
                "Gerichtstermin",
                "Prüfung"
            ),
            "COMPENSATORY_TIME" to listOf(
                "Überstundenausgleich",
                "Zeitausgleich",
                "Gleitzeitausgleich",
                "Feiertagsausgleich"
            ),
            "MATERNITY_LEAVE" to listOf(
                "Mutterschaftsurlaub",
                "Schwangerschaftsurlaub"
            ),
            "PATERNITY_LEAVE" to listOf(
                "Vaterschaftsurlaub",
                "Vaterschaftsfreistellung"
            )
        )
        
        val reasons = reasonsByType[selectedType] ?: listOf("Urlaub")
        val selectedReason = reasons.random()
        
        try {
            holidayFacade.requestHoliday(
                HolidayDTO(
                    id = null,
                    employeeId = employee.id,
                    startDate = date,
                    endDate = date,
                    type = selectedType,
                    reason = selectedReason,
                    approval = ApprovalDTO(
                        approved = Math.random() > 0.2, // 80% approved
                        approverId = 1,
                        approverName = "Admin"
                    )
                )
            )
        } catch (e: Exception) {
            // Ignore if holiday already exists
        }
    }

    private fun createMultipleTimeEntries(employee: ch.baunex.user.dto.EmployeeDTO, date: LocalDate) {
        // Zufällige Anzahl Einträge pro Tag (1-3)
        val numberOfEntries = (minEntriesPerDay..maxEntriesPerDay).random()
        
        // Verfügbare Projekte für diesen Tag (ohne Duplikate)
        val availableProjects = projectIds.shuffled().take(numberOfEntries)
        
        // Zeitfenster für den Arbeitstag in Minuten
        val workDayStartMinutes = workDayStart.hour * 60 + workDayStart.minute
        val workDayEndMinutes = workDayEnd.hour * 60 + workDayEnd.minute
        val totalWorkMinutes = workDayEndMinutes - workDayStartMinutes
        
        // Mindestzeit pro Projekt (in Minuten)
        val minMinutesPerProject = (minHoursPerDayPerProject * 60).toInt()
        val maxMinutesPerProject = (maxHoursPerDayPerProject * 60).toInt()
        
        // Pausen zwischen Projekten (in Minuten)
        val breakMinutes = 15
        
        // Berechne verfügbare Zeit für alle Projekte + Pausen
        val totalBreakTime = (numberOfEntries - 1) * breakMinutes
        val availableWorkTime = totalWorkMinutes - totalBreakTime
        
        // Verteile die verfügbare Zeit auf die Projekte
        val timeSlots = mutableListOf<Int>()
        var remainingTime = availableWorkTime
        
        for (i in 0 until numberOfEntries) {
            val isLastEntry = i == numberOfEntries - 1
            val maxTimeForThisProject = if (isLastEntry) remainingTime else {
                val minTimeForOthers = (numberOfEntries - i - 1) * minMinutesPerProject
                remainingTime - minTimeForOthers
            }
            
            val projectTime = if (isLastEntry) {
                remainingTime
            } else {
                (minMinutesPerProject..minOf(maxMinutesPerProject, maxTimeForThisProject)).random()
            }
            
            timeSlots.add(projectTime)
            remainingTime -= projectTime
        }
        
        // Erstelle die Einträge mit nicht überlappenden Zeiten
        var currentStartMinutes = workDayStartMinutes
        
        for (i in 0 until numberOfEntries) {
            val projectTime = timeSlots[i]
            val projectId = availableProjects[i]
            
            val startTime = LocalTime.of(currentStartMinutes / 60, currentStartMinutes % 60)
            val endTime = LocalTime.of((currentStartMinutes + projectTime) / 60, (currentStartMinutes + projectTime) % 60)
            val hours = projectTime / 60.0
            
            createTimeEntry(employee, date, projectId, startTime, endTime, hours)
            
            // Nächste Startzeit (Projektzeit + Pause)
            currentStartMinutes += projectTime + breakMinutes
        }
    }
    
    private fun createTimeEntry(employee: ch.baunex.user.dto.EmployeeDTO, date: LocalDate, projectId: Int, startTime: LocalTime, endTime: LocalTime, hours: Double) {
        // Zufälliger Titel
        val title = workTitles.random()
        
        // Zufällige Notiz
        val noteCategory = NoteCategory.values().random()
        val noteContent = sampleNotes[noteCategory]?.random() ?: "Arbeit durchgeführt"
        
        // Zufällige Sonderfaktoren
        val hasNightSurcharge = Math.random() < 0.1 // 10% Nachtarbeit
        val hasWeekendSurcharge = Math.random() < 0.05 // 5% Wochenendarbeit
        val hasHolidaySurcharge = Math.random() < 0.02 // 2% Feiertagsarbeit
        val travelTimeMinutes = if (Math.random() < 0.3) (Math.random() * 60).toInt() else 0
        val waitingTimeMinutes = if (Math.random() < 0.2) (Math.random() * 120).toInt() else 0

        try {
            timeTrackingFacade.logTime(
                TimeEntryDTO(
                    employeeId = employee.id,
                    projectId = projectId.toLong(),
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    hoursWorked = hours,
                    title = title,
                    notes = listOf(
                        NoteDto(
                            id = 0,
                            projectId = projectId.toLong(),
                            timeEntryId = null,
                            documentId = null,
                            title = title,
                            content = noteContent,
                            category = noteCategory,
                            tags = listOf("Auto-Generated", date.dayOfWeek.name),
                            attachments = emptyList(),
                            createdById = employee.id,
                            createdAt = null,
                            updatedAt = null
                        )
                    ),
                    hourlyRate = employee.hourlyRate,
                    billable = Math.random() > 0.1, // 90% billable
                    invoiced = Math.random() > 0.3, // 70% invoiced
                    hasNightSurcharge = hasNightSurcharge,
                    hasWeekendSurcharge = hasWeekendSurcharge,
                    hasHolidaySurcharge = hasHolidaySurcharge,
                    travelTimeMinutes = travelTimeMinutes,
                    waitingTimeMinutes = waitingTimeMinutes,
                    disposalCost = 0.0,
                    catalogItems = emptyList(), // Keine Katalog-Items für automatische Einträge
                    employeeLastName = "",
                    employeeFirstName = "",
                    projectName = "",
                    employeeEmail = "",
                    cost = 0.0
                )
            )
        } catch (e: Exception) {
            // Ignore if entry already exists or other errors
        }
    }
} 