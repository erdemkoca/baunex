package ch.baunex.config

import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.service.HolidayTypeService
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleHolidayTypeLoader @Inject constructor(
    private val holidayTypeService: HolidayTypeService
) {
    @Transactional
    fun onStart(@Observes ev: StartupEvent) {
        val defaultTypes = listOf(
            HolidayTypeCreateDTO("PAID_VACATION", "Bezahlter Urlaub", 0.0),
            HolidayTypeCreateDTO("UNPAID_LEAVE", "Unbezahlter Urlaub", 8.0),
            HolidayTypeCreateDTO("SICK_LEAVE", "Krankheit", 0.0),
            HolidayTypeCreateDTO("SPECIAL_LEAVE", "Sonderurlaub", 0.0),
            HolidayTypeCreateDTO("COMPENSATORY_TIME", "Zeitausgleich", 0.0),
            HolidayTypeCreateDTO("MATERNITY_LEAVE", "Mutterschaftsurlaub", 0.0),
            HolidayTypeCreateDTO("PATERNITY_LEAVE", "Vaterschaftsurlaub", 0.0)
        )
        for (type in defaultTypes) {
            try {
                if (holidayTypeService.getHolidayTypeByCode(type.code) == null) {
                    holidayTypeService.createHolidayType(type)
                }
            } catch (e: Exception) {
                // ignore if already exists or fails
            }
        }
        println("✅ Loaded sample holiday types.")
    }
}
