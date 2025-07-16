package ch.baunex.bootstrap

import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.service.HolidayTypeService
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional

/**
 * Core data bootstrap that runs in ALL environments (dev, test, prod).
 * Ensures essential system data is always present.
 */
@ApplicationScoped
class CoreDataBootstrap @Inject constructor(
    private val holidayTypeService: HolidayTypeService
) {
    
    @Transactional
    fun bootstrapCoreData(@Observes ev: StartupEvent) {
        bootstrapHolidayTypes()
        // Add other core data bootstrap methods here as needed
    }
    
    private fun bootstrapHolidayTypes() {
        val coreHolidayTypes = listOf(
            HolidayTypeCreateDTO("PAID_VACATION", "Bezahlter Urlaub", 0.0),
            HolidayTypeCreateDTO("UNPAID_LEAVE", "Unbezahlter Urlaub", 8.0),
            HolidayTypeCreateDTO("SICK_LEAVE", "Krankheit", 0.0),
            HolidayTypeCreateDTO("SPECIAL_LEAVE", "Sonderurlaub", 0.0),
            HolidayTypeCreateDTO("COMPENSATORY_TIME", "Zeitausgleich", 0.0),
            HolidayTypeCreateDTO("MATERNITY_LEAVE", "Mutterschaftsurlaub", 0.0),
            HolidayTypeCreateDTO("PATERNITY_LEAVE", "Vaterschaftsurlaub", 0.0)
        )
        
        for (type in coreHolidayTypes) {
            try {
                if (holidayTypeService.getHolidayTypeByCode(type.code) == null) {
                    holidayTypeService.createHolidayType(type)
                }
            } catch (e: Exception) {
                // Log but don't fail startup if holiday type creation fails
                println("⚠️  Warning: Could not create holiday type ${type.code}: ${e.message}")
            }
        }
        println("✅ Core holiday types bootstrapped.")
    }
} 