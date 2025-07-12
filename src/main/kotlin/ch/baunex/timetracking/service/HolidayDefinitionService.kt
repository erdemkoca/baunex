package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.HolidayDefinitionModel
import ch.baunex.timetracking.repository.HolidayDefinitionRepository
import ch.baunex.timetracking.util.HolidayCalculator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import io.quarkus.scheduler.Scheduled

@ApplicationScoped
class HolidayDefinitionService @Inject constructor(
    private val holidayDefinitionRepository: HolidayDefinitionRepository
) {

    /**
     * Prüft ob Feiertage für ein Jahr existieren
     */
    fun existsByYear(year: Int): Boolean {
        return holidayDefinitionRepository.existsByYear(year)
    }

    /**
     * Generiert automatisch alle Schweizer Feiertage für ein Jahr
     */
    @Transactional
    fun generateHolidaysForYear(year: Int) {
        // Prüfen ob bereits Feiertage für dieses Jahr existieren
        if (holidayDefinitionRepository.existsByYear(year)) {
            return
        }

        val swissHolidays = HolidayCalculator.generateSwissHolidays(year)
        
        swissHolidays.forEach { (date, name, isFixed) ->
            val holiday = HolidayDefinitionModel().apply {
                this.year = year
                this.date = date
                this.name = name
                this.isFixed = isFixed
                this.isEditable = false // Automatisch generierte Feiertage sind nicht editierbar
                this.active = true
                this.isWorkFree = true
                this.holidayType = ch.baunex.timetracking.model.HolidayDefinitionType.PUBLIC_HOLIDAY
                this.description = if (isFixed) "Fester Schweizer Feiertag" else "Beweglicher Schweizer Feiertag"
                this.createdAt = LocalDate.now()
            }
            holidayDefinitionRepository.persist(holiday)
        }
    }

    /**
     * Holt alle Feiertage für ein Jahr
     */
    fun getHolidaysForYear(year: Int): List<HolidayDefinitionModel> {
        val currentYear = LocalDate.now().year
        
        // Automatisch generieren falls noch keine existieren
        // Auch für zukünftige Jahre (bis zu 2 Jahre in die Zukunft)
        if (!holidayDefinitionRepository.existsByYear(year) && year >= currentYear - 1 && year <= currentYear + 2) {
            generateHolidaysForYear(year)
        }
        
        return holidayDefinitionRepository.findByYear(year)
    }

    /**
     * Holt alle Feiertage für einen Datumsbereich
     */
    fun getHolidaysForDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        return holidayDefinitionRepository.findByDateRange(startDate, endDate)
    }

    /**
     * Holt alle arbeitsfreien Feiertage für einen Datumsbereich
     */
    fun getWorkFreeHolidaysForDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        return holidayDefinitionRepository.findWorkFreeHolidaysByDateRange(startDate, endDate)
    }

    /**
     * Erstellt einen neuen Feiertag
     */
    @Transactional
    fun createHoliday(holiday: HolidayDefinitionModel): HolidayDefinitionModel {
        holiday.createdAt = LocalDate.now()
        holidayDefinitionRepository.persist(holiday)
        return holiday
    }

    /**
     * Aktualisiert einen bestehenden Feiertag
     */
    @Transactional
    fun updateHoliday(id: Long, holiday: HolidayDefinitionModel): HolidayDefinitionModel? {
        val existingHoliday = holidayDefinitionRepository.findById(id) ?: return null
        
        // Nur editierbare Feiertage können geändert werden
        if (!existingHoliday.isEditable) {
            throw IllegalArgumentException("Dieser Feiertag kann nicht bearbeitet werden")
        }

        existingHoliday.apply {
            date = holiday.date
            name = holiday.name
            canton = holiday.canton
            isWorkFree = holiday.isWorkFree
            holidayType = holiday.holidayType
            description = holiday.description
            updatedAt = LocalDate.now()
        }

        return existingHoliday
    }

    /**
     * Löscht einen Feiertag (setzt ihn auf inaktiv)
     */
    @Transactional
    fun deleteHoliday(id: Long): Boolean {
        val holiday = holidayDefinitionRepository.findById(id) ?: return false
        
        // Nur editierbare Feiertage können gelöscht werden
        if (!holiday.isEditable) {
            throw IllegalArgumentException("Dieser Feiertag kann nicht gelöscht werden")
        }

        holiday.active = false
        holiday.updatedAt = LocalDate.now()
        return true
    }

    /**
     * Prüft ob ein Datum ein Feiertag ist
     */
    fun isHoliday(date: LocalDate): Boolean {
        val holidays = holidayDefinitionRepository.findByDateRange(date, date)
        return holidays.any { it.isWorkFree && it.active }
    }

    /**
     * Berechnet die Anzahl Arbeitstage zwischen zwei Daten
     */
    fun calculateWorkingDays(startDate: LocalDate, endDate: LocalDate): Int {
        val holidays = holidayDefinitionRepository.findWorkFreeHolidaysByDateRange(startDate, endDate)
        return HolidayCalculator.calculateWorkingDays(startDate, endDate, holidays)
    }

    /**
     * Automatische Generierung von Feiertagen für neue Jahre
     * Läuft täglich um 2:00 Uhr
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    fun generateHolidaysForNewYears() {
        val currentYear = LocalDate.now().year
        val nextYear = currentYear + 1
        
        // Prüfe ob Feiertage für das nächste Jahr existieren
        if (!holidayDefinitionRepository.existsByYear(nextYear)) {
            generateHolidaysForYear(nextYear)
            println("🔄 Automatically generated holidays for year $nextYear")
        }
    }
} 