package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.HolidayDefinitionModel
import ch.baunex.timetracking.repository.HolidayDefinitionRepository
import ch.baunex.timetracking.util.HolidayCalculator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import io.quarkus.scheduler.Scheduled
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayDefinitionService @Inject constructor(
    private val holidayDefinitionRepository: HolidayDefinitionRepository,
    private val holidayTypeService: ch.baunex.timetracking.service.HolidayTypeService,
    private val holidayTypeMapper: ch.baunex.timetracking.mapper.HolidayTypeMapper
) {
    private val log = Logger.getLogger(HolidayDefinitionService::class.java)

    /**
     * Prüft ob Feiertage für ein Jahr existieren
     */
    fun existsByYear(year: Int): Boolean {
        log.info("Checking if holidays exist for year: $year")
        return try {
            val exists = holidayDefinitionRepository.existsByYear(year)
            log.info("Exists for year $year: $exists")
            exists
        } catch (e: Exception) {
            log.error("Failed to check if holidays exist for year: $year", e)
            throw e
        }
    }

    /**
     * Generiert automatisch alle Schweizer Feiertage für ein Jahr
     */
    @Transactional
    fun generateHolidaysForYear(year: Int) {
        log.info("Generating holidays for year: $year")
        try {
            // Prüfen ob bereits Feiertage für dieses Jahr existieren
            if (holidayDefinitionRepository.existsByYear(year)) {
                log.warn("Holidays for year $year already exist")
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
                    // TODO: Get default holiday type from database
        this.holidayType = holidayTypeService.getHolidayTypeByCode("PUBLIC_HOLIDAY")?.let { 
            holidayTypeMapper.toModelFromDTO(it) 
        } ?: throw IllegalStateException("Default holiday type not found")
                    this.description = if (isFixed) "Fester Schweizer Feiertag" else "Beweglicher Schweizer Feiertag"
                    this.createdAt = LocalDate.now()
                }
                holidayDefinitionRepository.persist(holiday)
            }
            log.info("Generated ${swissHolidays.size} holidays for year: $year")
        } catch (e: Exception) {
            log.error("Failed to generate holidays for year: $year", e)
            throw e
        }
    }

    /**
     * Holt alle Feiertage für ein Jahr
     */
    fun getHolidaysForYear(year: Int): List<HolidayDefinitionModel> {
        log.info("Fetching holidays for year: $year")
        return try {
            val currentYear = LocalDate.now().year
            
            // Automatisch generieren falls noch keine existieren
            // Auch für zukünftige Jahre (bis zu 2 Jahre in die Zukunft)
            if (!holidayDefinitionRepository.existsByYear(year) && year >= currentYear - 1 && year <= currentYear + 2) {
                generateHolidaysForYear(year)
            }
            
            val holidays = holidayDefinitionRepository.findByYear(year)
            log.info("Fetched ${holidays.size} holidays for year: $year")
            holidays
        } catch (e: Exception) {
            log.error("Failed to fetch holidays for year: $year", e)
            throw e
        }
    }

    /**
     * Holt alle Feiertage für einen Datumsbereich
     */
    fun getHolidaysForDateRange(startDate: LocalDate, endDate: LocalDate): List<HolidayDefinitionModel> {
        log.info("Fetching holidays for date range: $startDate to $endDate")
        return try {
            val holidays = holidayDefinitionRepository.findByDateRange(startDate, endDate)
            log.info("Fetched ${holidays.size} holidays for date range: $startDate to $endDate")
            holidays
        } catch (e: Exception) {
            log.error("Failed to fetch holidays for date range: $startDate to $endDate", e)
            throw e
        }
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
        log.info("Updating holiday with ID: $id")
        return try {
            val existingHoliday = holidayDefinitionRepository.findById(id) ?: return null
            
            // Nur editierbare Feiertage können geändert werden
            if (!existingHoliday.isEditable) {
                log.warn("Holiday with ID $id is not editable")
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
            log.info("Updated holiday with ID: $id")
            existingHoliday
        } catch (e: Exception) {
            log.error("Failed to update holiday with ID: $id", e)
            throw e
        }
    }

    /**
     * Löscht einen Feiertag (setzt ihn auf inaktiv)
     */
    @Transactional
    fun deleteHoliday(id: Long): Boolean {
        log.info("Deleting holiday with ID: $id")
        return try {
            val holiday = holidayDefinitionRepository.findById(id) ?: return false
            
            // Nur editierbare Feiertage können gelöscht werden
            if (!holiday.isEditable) {
                log.warn("Holiday with ID $id is not deletable")
                throw IllegalArgumentException("Dieser Feiertag kann nicht gelöscht werden")
            }

            holiday.active = false
            holiday.updatedAt = LocalDate.now()
            log.info("Deleted holiday with ID: $id (set to inactive)")
            true
        } catch (e: Exception) {
            log.error("Failed to delete holiday with ID: $id", e)
            throw e
        }
    }

    /**
     * Prüft ob ein Datum ein Feiertag ist
     */
    fun isHoliday(date: LocalDate): Boolean {
        log.info("Checking if $date is a holiday")
        return try {
            val holidays = holidayDefinitionRepository.findByDateRange(date, date)
            val isHoliday = holidays.any { it.isWorkFree && it.active }
            log.info("$date is holiday: $isHoliday")
            isHoliday
        } catch (e: Exception) {
            log.error("Failed to check if $date is a holiday", e)
            throw e
        }
    }

    /**
     * Berechnet die Anzahl Arbeitstage zwischen zwei Daten
     */
    fun calculateWorkingDays(startDate: LocalDate, endDate: LocalDate): Int {
        log.info("Calculating working days from $startDate to $endDate")
        return try {
            val holidays = holidayDefinitionRepository.findWorkFreeHolidaysByDateRange(startDate, endDate)
            val workingDays = HolidayCalculator.calculateWorkingDays(startDate, endDate, holidays)
            log.info("Calculated $workingDays working days from $startDate to $endDate")
            workingDays
        } catch (e: Exception) {
            log.error("Failed to calculate working days from $startDate to $endDate", e)
            throw e
        }
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