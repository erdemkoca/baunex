package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.timetracking.validation.HolidayValidator
import ch.baunex.timetracking.dto.HolidayDTO
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayService @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository,
    private val holidayValidator: HolidayValidator
) {
    private val log = Logger.getLogger(HolidayService::class.java)

    @Transactional
    fun createHolidayWithValidation(dto: HolidayDTO, holidayType: ch.baunex.timetracking.model.HolidayTypeModel): HolidayModel {
        log.info("Creating holiday with validation for employee ${dto.employeeId}")
        return try {
            // Validate the holiday request before processing
            holidayValidator.validateHoliday(dto)
            
            val employee = employeeRepository.findById(dto.employeeId)
                ?: throw IllegalArgumentException("Employee not found with id: ${dto.employeeId}")
            
            // Create the holiday model
            val model = HolidayModel().apply {
                this.employee = employee
                this.startDate = dto.startDate
                this.endDate = dto.endDate
                this.reason = dto.reason
                this.holidayType = holidayType
                // Use the status from DTO, default to PENDING if not specified
                this.approvalStatus = try {
                    ApprovalStatus.valueOf(dto.status.uppercase())
                } catch (e: IllegalArgumentException) {
                    ApprovalStatus.PENDING
                }
                this.createdAt = LocalDate.now()
            }
            
            holidayRepository.persist(model)
            log.info("Successfully created holiday with ID: ${model.id} for employee ${dto.employeeId} with status: ${model.approvalStatus}")
            model
        } catch (e: ch.baunex.timetracking.exception.TimeTrackingException) {
            // Business logic exceptions should not be logged as errors - they're expected
            log.info("Holiday validation failed for employee ${dto.employeeId}: ${e.message}")
            throw e
        } catch (e: Exception) {
            // Only log unexpected exceptions as errors
            log.error("Unexpected error creating holiday for employee ${dto.employeeId}", e)
            throw e
        }
    }

    fun getHolidaysForEmployee(employeeId: Long): List<HolidayModel> {
        log.info("Fetching holidays for employee $employeeId")
        return try {
            val holidays = holidayRepository.find("FROM HolidayModel h WHERE h.employee.id = ?1", employeeId).list<HolidayModel>()
            log.info("Fetched ${holidays.size} holidays for employee $employeeId")
            holidays
        } catch (e: Exception) {
            log.error("Failed to fetch holidays for employee $employeeId", e)
            throw e
        }
    }

    fun getAllHolidays(): List<HolidayModel> {
        log.info("Fetching all holidays")
        return try {
            val holidays = holidayRepository.findAllWithoutCollections()
            log.info("Fetched ${holidays.size} holidays")
            holidays
        } catch (e: Exception) {
            log.error("Failed to fetch all holidays", e)
            throw e
        }
    }

    @Transactional
    fun approveHoliday(holidayId: Long, approverId: Long, approvalStatus: String): HolidayModel? {
        log.info("Approving holiday $holidayId by approver $approverId with status $approvalStatus")
        return try {
            val holiday = holidayRepository.findById(holidayId) ?: return null
            val approver = employeeRepository.findById(approverId) ?: return null
            holiday.approvalStatus = ApprovalStatus.valueOf(approvalStatus)
            holiday.approvedBy = approver
            holiday.approvedAt = java.time.LocalDate.now()
            log.info("Approved holiday $holidayId with status $approvalStatus")
            holiday
        } catch (e: Exception) {
            log.error("Failed to approve holiday $holidayId", e)
            throw e
        }
    }

    /**
     * Find holiday conflicts for a given date range and employee
     * Returns conflicting holidays that would overlap with the requested period
     */
    fun findHolidayConflicts(employeeId: Long, startDate: LocalDate, endDate: LocalDate): List<HolidayModel> {
        log.info("Finding holiday conflicts for employee $employeeId from $startDate to $endDate")
        return try {
            val existingHolidays = holidayRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate)
            
            // Filter for pending or approved holidays only
            val conflictingHolidays = existingHolidays.filter { holiday ->
                holiday.approvalStatus == ApprovalStatus.PENDING || holiday.approvalStatus == ApprovalStatus.APPROVED
            }
            
            log.info("Found ${conflictingHolidays.size} conflicting holidays for employee $employeeId")
            conflictingHolidays
        } catch (e: Exception) {
            log.error("Failed to find holiday conflicts for employee $employeeId", e)
            throw e
        }
    }

    /**
     * Cancel a holiday by setting its status to CANCELED
     */
    @Transactional
    fun cancelHoliday(holidayId: Long): HolidayModel? {
        log.info("Canceling holiday $holidayId")
        return try {
            val holiday = holidayRepository.findById(holidayId) ?: return null
            holiday.approvalStatus = ApprovalStatus.CANCELED
            holiday.approvedAt = java.time.LocalDate.now()
            log.info("Canceled holiday $holidayId")
            holiday
        } catch (e: Exception) {
            log.error("Failed to cancel holiday $holidayId", e)
            throw e
        }
    }
}
