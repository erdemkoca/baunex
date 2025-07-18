package ch.baunex.config.sample

import ch.baunex.timetracking.dto.ApprovalDTO
import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.facade.HolidayFacade
import ch.baunex.timetracking.service.HolidayTypeService
import ch.baunex.user.facade.EmployeeFacade
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Sample holiday loader - DEV ONLY
 * This class can be safely removed before production release.
 */
@IfBuildProfile("dev")
@ApplicationScoped
class SampleHolidayLoader {

    @Inject
    lateinit var holidayFacade: HolidayFacade

    @Inject
    lateinit var employeeFacade: EmployeeFacade

    @Inject
    lateinit var holidayTypeService: HolidayTypeService
    
    @Inject
    lateinit var employeeRepository: ch.baunex.user.repository.EmployeeRepository

    // Konfigurierbare Parameter
    private val weeksToGenerate = 10
    private val holidayChance = 0.15 // 15% Wahrscheinlichkeit für Urlaubstag (höher als in TimeEntryLoader)
    private val workDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

    @Transactional
    fun load() {
        println("🎯 Loading sample holidays...")
        
        val employees = employeeRepository.listAllEmployeesWithoutPerson()
        val holidayTypes = holidayTypeService.getActiveHolidayTypes()
        
        if (holidayTypes.isEmpty()) {
            println("❌ ERROR: No holiday types found! Core data bootstrap may have failed.")
            return
        }
        
        println("✅ Found ${holidayTypes.size} holiday types: ${holidayTypes.map { it.code }}")
        
        var totalHolidaysCreated = 0
        
        employees.forEach { employee ->
            val holidaysForEmployee = generateHolidaysForEmployee(employee, holidayTypes)
            totalHolidaysCreated += holidaysForEmployee
        }
    }

    private fun generateHolidaysForEmployee(employee: ch.baunex.user.model.EmployeeModel, holidayTypes: List<ch.baunex.timetracking.dto.HolidayTypeDTO>): Int {
        // Generate holidays for the next 10 weeks (future dates only)
        val startDate = LocalDate.now().plusDays(1) // Start from tomorrow
        val endDate = startDate.plusWeeks(weeksToGenerate.toLong()) // 10 weeks into the future
        
        var currentDate = startDate
        var holidaysCreated = 0

        while (!currentDate.isAfter(endDate)) {
            // Only create holidays on workdays (Monday to Friday)
            if (currentDate.dayOfWeek in workDays) {
                if (Math.random() < holidayChance) {
                    if (createHoliday(employee, currentDate, holidayTypes)) {
                        holidaysCreated++
                    }
                }
            }
            
            // Move to next day
            currentDate = currentDate.plusDays(1)
        }
        
        return holidaysCreated
    }

    private fun createHoliday(employee: ch.baunex.user.model.EmployeeModel, date: LocalDate, holidayTypes: List<ch.baunex.timetracking.dto.HolidayTypeDTO>): Boolean {
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
        
        return try {
            // Create realistic approval status distribution
            val approvalStatus = when {
                Math.random() < 0.7 -> "PENDING"    // 70% pending
                Math.random() < 0.85 -> "APPROVED"  // 15% approved (30% of remaining 30%)
                else -> "REJECTED"                  // 15% rejected (15% of remaining 30%)
            }
            
            holidayFacade.requestHoliday(
                HolidayDTO(
                    id = null,
                    employeeId = employee.id!!,
                    startDate = date,
                    endDate = date,
                    type = selectedType,
                    reason = selectedReason,
                    status = approvalStatus,
                    approval = ApprovalDTO(
                        approved = approvalStatus == "APPROVED",
                        approverId = 1,
                        approverName = "Admin",
                        status = approvalStatus
                    )
                )
            )
            true
        } catch (e: Exception) {
            println("⚠️  Warning: Could not create holiday for employee ${employee.id} on $date: ${e.message}")
            false
        }
    }
} 