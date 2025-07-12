package ch.baunex.timetracking.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object HolidayCalculator {
    
    /**
     * Berechnet das Osterdatum für ein gegebenes Jahr (Gaußsche Osterformel)
     */
    fun calculateEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        
        return LocalDate.of(year, month, day)
    }
    
    /**
     * Berechnet alle beweglichen Feiertage für ein Jahr
     */
    fun calculateMovableHolidays(year: Int): Map<String, LocalDate> {
        val easterSunday = calculateEasterSunday(year)
        
        return mapOf(
            "Karfreitag" to easterSunday.minusDays(2),
            "Ostermontag" to easterSunday.plusDays(1),
            "Auffahrt" to easterSunday.plusDays(39), // Christi Himmelfahrt
            "Pfingstmontag" to easterSunday.plusDays(50)
        )
    }
    
    /**
     * Generiert alle Schweizer Feiertage für ein Jahr
     */
    fun generateSwissHolidays(year: Int): List<Triple<LocalDate, String, Boolean>> {
        val holidays = mutableListOf<Triple<LocalDate, String, Boolean>>()
        
        // Fixe Feiertage
        holidays.add(Triple(LocalDate.of(year, 1, 1), "Neujahr", true))
        holidays.add(Triple(LocalDate.of(year, 8, 1), "Bundesfeier", true))
        holidays.add(Triple(LocalDate.of(year, 12, 25), "Weihnachten", true))
        holidays.add(Triple(LocalDate.of(year, 12, 26), "Stephanstag", true))
        
        // Bewegliche Feiertage
        val movableHolidays = calculateMovableHolidays(year)
        movableHolidays.forEach { (name, date) ->
            holidays.add(Triple(date, name, false))
        }
        
        return holidays.sortedBy { it.first }
    }
    
    /**
     * Prüft ob ein Datum ein Feiertag ist
     */
    fun isHoliday(date: LocalDate, holidayDefinitions: List<ch.baunex.timetracking.model.HolidayDefinitionModel>): Boolean {
        return holidayDefinitions.any { it.date == date && it.active && it.isWorkFree }
    }
    
    /**
     * Berechnet die Anzahl Arbeitstage zwischen zwei Daten (ohne Wochenenden und Feiertage)
     */
    fun calculateWorkingDays(startDate: LocalDate, endDate: LocalDate, holidayDefinitions: List<ch.baunex.timetracking.model.HolidayDefinitionModel>): Int {
        var workingDays = 0
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            val dayOfWeek = currentDate.dayOfWeek.value
            val isWeekend = dayOfWeek == 6 || dayOfWeek == 7 // Samstag = 6, Sonntag = 7
            val isHoliday = isHoliday(currentDate, holidayDefinitions)
            
            if (!isWeekend && !isHoliday) {
                workingDays++
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return workingDays
    }
} 