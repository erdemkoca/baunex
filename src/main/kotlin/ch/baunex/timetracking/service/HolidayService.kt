package ch.baunex.timetracking.service

import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.repository.HolidayRepository
import ch.baunex.user.repository.EmployeeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayService @Inject constructor(
    private val holidayRepository: HolidayRepository,
    private val employeeRepository: EmployeeRepository
) {
    private val log = Logger.getLogger(HolidayService::class.java)

    @Transactional
    fun createHoliday(model: HolidayModel, employeeId: Long): HolidayModel {
        log.info("Creating holiday for employee $employeeId")
        return try {
            val employee = employeeRepository.findById(employeeId)
                ?: throw IllegalArgumentException("Employee not found with id: $employeeId")
            model.employee = employee
            model.approvalStatus = ApprovalStatus.PENDING
            holidayRepository.persist(model)
            log.info("Created holiday with ID: ${model.id}")
            model
        } catch (e: Exception) {
            log.error("Failed to create holiday for employee $employeeId", e)
            throw e
        }
    }

    fun getHolidaysForEmployee(employeeId: Long): List<HolidayModel> {
        log.info("Fetching holidays for employee $employeeId")
        return try {
            val holidays = holidayRepository.list("employee.id", employeeId)
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
            val holidays = holidayRepository.listAll()
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
}
