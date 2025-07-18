package ch.baunex.timetracking.facade

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.dto.HolidayApprovalDTO
import ch.baunex.timetracking.mapper.HolidayMapper
import ch.baunex.timetracking.service.HolidayService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

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
        log.info("Approving holiday $holidayId with status ${dto.approval.status}")
        val updated = holidayService.approveHoliday(
            holidayId = holidayId,
            approverId = dto.approval.approverId ?: throw IllegalArgumentException("Approver ID is required"),
            approvalStatus = dto.approval.status
        ) ?: return null

        return holidayMapper.toDTO(updated)
    }
}
