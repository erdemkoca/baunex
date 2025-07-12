package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayDefinitionDTO
import ch.baunex.timetracking.mapper.HolidayDefinitionMapper
import ch.baunex.timetracking.service.HolidayDefinitionService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class HolidayDefinitionFacade @Inject constructor(
    private val holidayDefinitionService: HolidayDefinitionService,
    private val holidayDefinitionMapper: HolidayDefinitionMapper
) {

    /**
     * Generiert automatisch alle Feiertage für ein Jahr
     */
    @Transactional
    fun generateHolidaysForYear(year: Int) {
        holidayDefinitionService.generateHolidaysForYear(year)
    }

    /**
     * Holt alle Feiertage für ein Jahr als DTOs
     */
    fun getHolidaysForYear(year: Int): List<HolidayDefinitionDTO> {
        val holidays = holidayDefinitionService.getHolidaysForYear(year)
        return holidays.map { holidayDefinitionMapper.toDTO(it) }
    }

    /**
     * Holt alle Feiertage für einen Datumsbereich als DTOs
     */
    fun getHolidaysForDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionDTO> {
        val holidays = holidayDefinitionService.getHolidaysForDateRange(startDate, endDate)
        return holidays.map { holidayDefinitionMapper.toDTO(it) }
    }

    /**
     * Erstellt einen neuen Feiertag
     */
    @Transactional
    fun createHoliday(dto: HolidayDefinitionDTO): HolidayDefinitionDTO {
        val model = holidayDefinitionMapper.toModel(dto)
        val createdHoliday = holidayDefinitionService.createHoliday(model)
        return holidayDefinitionMapper.toDTO(createdHoliday)
    }

    /**
     * Aktualisiert einen bestehenden Feiertag
     */
    @Transactional
    fun updateHoliday(id: Long, dto: HolidayDefinitionDTO): HolidayDefinitionDTO? {
        val model = holidayDefinitionMapper.toModel(dto)
        val updatedHoliday = holidayDefinitionService.updateHoliday(id, model) ?: return null
        return holidayDefinitionMapper.toDTO(updatedHoliday)
    }

    /**
     * Löscht einen Feiertag
     */
    @Transactional
    fun deleteHoliday(id: Long): Boolean {
        return holidayDefinitionService.deleteHoliday(id)
    }

    /**
     * Prüft ob ein Datum ein Feiertag ist
     */
    fun isHoliday(date: LocalDate): Boolean {
        return holidayDefinitionService.isHoliday(date)
    }

    /**
     * Berechnet die Anzahl Arbeitstage zwischen zwei Daten
     */
    fun calculateWorkingDays(startDate: LocalDate, endDate: LocalDate): Int {
        return holidayDefinitionService.calculateWorkingDays(startDate, endDate)
    }
} 