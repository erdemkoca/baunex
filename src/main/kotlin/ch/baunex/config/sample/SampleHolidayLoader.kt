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
        println("🔍 DEBUG: Current system date: ${LocalDate.now()}")
        
        try {
            val employees = employeeRepository.listAllEmployeesWithoutPerson()
            if (employees.isEmpty()) {
                println("⚠️  No employees found. Skipping holiday generation.")
                return
            }
            
            println("👥 Found ${employees.size} employees:")
            employees.forEach { emp ->
                println("   - ${emp.person.firstName} ${emp.person.lastName} (start: ${emp.startDate})")
            }
            
            val holidayTypes = holidayTypeService.getActiveHolidayTypes()
            
            if (holidayTypes.isEmpty()) {
                println("❌ ERROR: No holiday types found! Core data bootstrap may have failed.")
                println("   This might be due to database schema not being created yet.")
                println("   Please restart the application to ensure proper database initialization.")
                return
            }
            
            println("✅ Found ${holidayTypes.size} holiday types: ${holidayTypes.map { it.code }}")
            
            var totalHolidaysCreated = 0
            
            employees.forEach { employee ->
                try {
                    println("\n--- Processing ${employee.person.firstName} ${employee.person.lastName} ---")
                    val holidaysForEmployee = generateHolidaysForEmployee(employee, holidayTypes)
                    totalHolidaysCreated += holidaysForEmployee
                } catch (e: Exception) {
                    println("⚠️  Error generating holidays for ${employee.person.firstName} ${employee.person.lastName}: ${e.message}")
                }
            }
            
            println("\n🎉 Total holidays created: $totalHolidaysCreated")
            
        } catch (e: Exception) {
            println("❌ ERROR: Failed to load sample holidays: ${e.message}")
            println("   This might be due to database connection or schema issues.")
            e.printStackTrace()
        }
    }

    private fun generateHolidaysForEmployee(employee: ch.baunex.user.model.EmployeeModel, holidayTypes: List<ch.baunex.timetracking.dto.HolidayTypeDTO>): Int {
        // Generate holidays from employee start date to future
        val employeeStartDate = employee.startDate
        val currentDate = LocalDate.now()
        
        println("🔍 DEBUG: Date Analysis for ${employee.person.firstName} ${employee.person.lastName}")
        println("   Current system date: $currentDate")
        println("   Employee start date: $employeeStartDate")
        println("   Is employee start date in future? ${employeeStartDate.isAfter(currentDate)}")
        
        // For development/testing: If employee start date is in the future, use a realistic past date
        val effectiveStartDate = if (employeeStartDate.isAfter(currentDate)) {
            // Employee start date is in the future (like 2025), use a realistic past date
            val pastDate = currentDate.minusMonths(6) // 6 months ago
            println("   ⚠️  Employee start date is in future, using past date: $pastDate")
            pastDate
        } else {
            println("   ✅ Employee start date is in past, using as-is")
            employeeStartDate
        }
        
        // For development: If we're in a future year (like 2025), generate holidays from employee start date
        // This allows us to have realistic historical data even when system date is in the future
        val startDate = if (currentDate.year > 2024) {
            // We're in a future year, generate holidays from employee start date for realistic historical data
            println("   📅 System date is in future year (${currentDate.year}), generating from employee start date: $effectiveStartDate")
            effectiveStartDate
        } else if (effectiveStartDate.isBefore(currentDate)) {
            val tomorrow = currentDate.plusDays(1) // Start from tomorrow if employee started in the past
            println("   📅 Effective start date is in past, starting from tomorrow: $tomorrow")
            tomorrow
        } else {
            println("   📅 Effective start date is in future, using as start date: $effectiveStartDate")
            effectiveStartDate // Start from employee start date if it's in the future
        }
        
        val endDate = startDate.plusWeeks(weeksToGenerate.toLong()) // 10 weeks from start date
        
        println("📅 Final holiday generation for ${employee.person.firstName} ${employee.person.lastName}")
        println("   Original employee start date: $employeeStartDate")
        println("   Effective start date: $effectiveStartDate")
        println("   Holiday generation range: $startDate to $endDate")
        println("   Total days to generate: ${java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)}")
        
        var currentHolidayDate = startDate
        var holidaysCreated = 0
        var workdaysChecked = 0

        while (!currentHolidayDate.isAfter(endDate)) {
            // Only create holidays on workdays (Monday to Friday)
            if (currentHolidayDate.dayOfWeek in workDays) {
                workdaysChecked++
                if (Math.random() < holidayChance) {
                    if (createHoliday(employee, currentHolidayDate, holidayTypes)) {
                        holidaysCreated++
                        println("   🎯 Created holiday on: $currentHolidayDate")
                    }
                }
            }
            
            // Move to next day
            currentHolidayDate = currentHolidayDate.plusDays(1)
        }
        
        println("   ✅ Created $holidaysCreated holidays out of $workdaysChecked workdays checked")
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
            println("⚠️  Warning: Could not create holiday for employee ${employee.person.firstName} ${employee.person.lastName} on $date: ${e.message}")
            if (e.message?.contains("relation") == true) {
                println("   Database schema issue detected. Please restart the application.")
            }
            false
        }
    }
} 