package ch.baunex.timetracking.validation

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.exception.*
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.service.EmployeeService
import ch.baunex.timetracking.service.HolidayTypeService
import ch.baunex.timetracking.model.ApprovalStatus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Validator for holiday/absence requests
 * Performs comprehensive validation of holiday data and business rules
 */
@ApplicationScoped
class HolidayValidator @Inject constructor(
    private val employeeService: EmployeeService,
    private val holidayTypeService: HolidayTypeService,
    private val holidayRepository: HolidayRepository
) {
    private val log: Logger = Logger.getLogger(HolidayValidator::class.java)

    /**
     * Validates a holiday request
     * Throws appropriate exceptions if validation fails
     */
    fun validateHoliday(dto: HolidayDTO) {
        log.info("Validating holiday request for employee ${dto.employeeId}")
        
        // Validate basic data
        validateBasicData(dto)
        
        // Validate entities exist
        validateEntities(dto)
        
        // Validate business rules
        validateBusinessRules(dto)
        
        log.info("Holiday validation passed for employee ${dto.employeeId}")
    }

    /**
     * Validates basic data requirements
     */
    private fun validateBasicData(dto: HolidayDTO) {
        // Check required fields
        if (dto.employeeId == null) {
            throw MissingRequiredFieldException("employeeId")
        }
        
        if (dto.startDate == null) {
            throw MissingRequiredFieldException("startDate")
        }
        
        if (dto.endDate == null) {
            throw MissingRequiredFieldException("endDate")
        }
        
        if (dto.type.isNullOrBlank()) {
            throw MissingRequiredFieldException("type")
        }
        
        // Validate date range
        if (dto.startDate!!.isAfter(dto.endDate!!)) {
            throw InvalidDateException(
                dto.startDate!!,
                "Start date cannot be after end date"
            )
        }
        
        // Validate that start date is not in the past
        if (dto.startDate!!.isBefore(LocalDate.now())) {
            throw InvalidDateException(
                dto.startDate!!,
                "Holiday start date cannot be in the past"
            )
        }
    }

    /**
     * Validates that referenced entities exist
     */
    private fun validateEntities(dto: HolidayDTO) {
        // Validate employee exists
        val employee = employeeService.findEmployeeById(dto.employeeId!!)
            ?: throw EmployeeNotFoundException(dto.employeeId!!)
        
        // Validate holiday type exists (can be either code or display name)
        val holidayType = holidayTypeService.getHolidayTypeByCode(dto.type)
        if (holidayType == null) {
            // Try to find by display name as fallback
            val allTypes = holidayTypeService.getAllHolidayTypes()
            val foundByDisplayName = allTypes.find { it.displayName == dto.type }
            if (foundByDisplayName == null) {
                throw IllegalArgumentException("Holiday type '${dto.type}' not found")
            }
        }
    }

    /**
     * Validates business rules
     */
    private fun validateBusinessRules(dto: HolidayDTO) {
        // Check for overlapping holidays
        validateNoOverlap(dto)
        
        // Check for reasonable duration (e.g., not more than 30 days)
        val daysBetween = ChronoUnit.DAYS.between(dto.startDate!!, dto.endDate!!) + 1
        if (daysBetween > 30) {
            throw BusinessRuleViolationException(
                "MAX_DURATION",
                "Holiday duration cannot exceed 30 days (requested: $daysBetween days)"
            )
        }
    }

    /**
     * Validates that there are no overlapping holidays for the same employee
     */
    private fun validateNoOverlap(dto: HolidayDTO) {
        val existingHolidays = holidayRepository.findByEmployeeAndDateRange(
            dto.employeeId!!,
            dto.startDate!!,
            dto.endDate!!
        )
        
        // Filter for pending or approved holidays only
        val conflictingHolidays = existingHolidays.filter { holiday ->
            holiday.approvalStatus == ApprovalStatus.PENDING || holiday.approvalStatus == ApprovalStatus.APPROVED
        }
        
        if (conflictingHolidays.isNotEmpty()) {
            val conflictingHoliday = conflictingHolidays.first()
            // Business logic validation - log as INFO, not WARN or ERROR
            log.info("Holiday overlap validation failed for employee ${dto.employeeId}: " +
                    "requested ${dto.startDate} to ${dto.endDate}, " +
                    "conflicts with existing ${conflictingHoliday.startDate} to ${conflictingHoliday.endDate}")
            
            throw HolidayOverlapException(
                dto.employeeId!!,
                dto.startDate!!,
                dto.endDate!!,
                conflictingHoliday.startDate,
                conflictingHoliday.endDate,
                conflictingHoliday.holidayType?.displayName ?: "Unknown"
            )
        }
    }
} 