package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.dto.HolidayApprovalDTO
import ch.baunex.timetracking.mapper.HolidayMapper
import ch.baunex.timetracking.service.HolidayService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.time.LocalDate

@ApplicationScoped
class HolidayFacade @Inject constructor(
    private val holidayService: HolidayService,
    private val holidayMapper: HolidayMapper
) {
    private val log = Logger.getLogger(HolidayFacade::class.java)

    @Transactional
    fun requestHoliday(dto: HolidayDTO): HolidayDTO {
        log.info("Requesting holiday for employee ${dto.employeeId} from ${dto.startDate} to ${dto.endDate}")
        // First create the model to get the holiday type
        val model = holidayMapper.toModel(dto)
        
        // Create holiday with validation, passing the holiday type
        val saved = holidayService.createHolidayWithValidation(dto, model.holidayType)
        
        return holidayMapper.toDTO(saved)
    }

    fun getAllHolidays(): List<HolidayDTO> {
        log.debug("Getting all holidays")
        return holidayService.getAllHolidays().map { holidayMapper.toDTO(it) }
    }

    fun getHolidaysForEmployee(employeeId: Long): List<HolidayDTO> {
        log.debug("Getting holidays for employee: $employeeId")
        return holidayService.getHolidaysForEmployee(employeeId).map { holidayMapper.toDTO(it) }
    }

    @Transactional
    fun approveHoliday(holidayId: Long, dto: HolidayApprovalDTO): HolidayDTO? {
        log.debug("Approving holiday: $holidayId")
        val updated = holidayService.approveHoliday(
            holidayId,
            dto.approval.approverId ?: throw IllegalArgumentException("Approver ID is required"),
            dto.approval.status
        )
        return updated?.let { holidayMapper.toDTO(it) }
    }

    /**
     * Get holiday conflicts for a given date range and employee
     */
    fun getHolidayConflicts(employeeId: Long, startDate: LocalDate, endDate: LocalDate): ch.baunex.timetracking.dto.HolidayConflictDTO {
        log.debug("Getting holiday conflicts for employee: $employeeId from $startDate to $endDate")
        val conflictingHolidays = holidayService.findHolidayConflicts(employeeId, startDate, endDate)
        
        val conflictingHolidayDTOs = conflictingHolidays.map { holiday ->
            ch.baunex.timetracking.dto.ConflictingHolidayDTO(
                id = holiday.id,
                startDate = holiday.startDate,
                endDate = holiday.endDate,
                type = holiday.holidayType.displayName ?: holiday.holidayType.code,
                status = holiday.approvalStatus.name,
                reason = holiday.reason
            )
        }
        
        return ch.baunex.timetracking.dto.HolidayConflictDTO(
            employeeId = employeeId,
            requestedStartDate = startDate,
            requestedEndDate = endDate,
            conflictingHolidays = conflictingHolidayDTOs
        )
    }

    /**
     * Cancel a holiday
     */
    @Transactional
    fun cancelHoliday(holidayId: Long): HolidayDTO? {
        log.debug("Canceling holiday: $holidayId")
        val canceled = holidayService.cancelHoliday(holidayId)
        return canceled?.let { holidayMapper.toDTO(it) }
    }
}
