package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.repository.HolidayTypeRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayMapper @Inject constructor(
    private val holidayTypeRepository: HolidayTypeRepository
) {
    private val log = Logger.getLogger(HolidayMapper::class.java)

    fun toModel(dto: HolidayDTO): HolidayModel {
        log.debug("Mapping holiday DTO to model for employee ${dto.employeeId}")
        return HolidayModel().apply {
            id = dto.id
            startDate = dto.startDate
            endDate = dto.endDate
            // Find holiday type by code or display name
            holidayType = holidayTypeRepository.findActiveByCode(dto.type) ?: run {
                // Fallback: try to find by display name
                holidayTypeRepository.findActive().find { it.displayName == dto.type } ?: run {
                    // Default to PAID_VACATION if not found
                    holidayTypeRepository.findActiveByCode("PAID_VACATION") ?: 
                        throw IllegalStateException("No holiday type found for: ${dto.type}")
                }
            }
            // Safely convert status, default to PENDING if invalid
            approvalStatus = try {
                ApprovalStatus.valueOf(dto.status.uppercase())
            } catch (e: IllegalArgumentException) {
                ApprovalStatus.PENDING
            }
            reason = dto.reason
        }
    }

    fun toDTO(model: HolidayModel): HolidayDTO {
        log.debug("Mapping holiday model to DTO for employee ${model.employee.id}")
        return HolidayDTO(
            id = model.id,
            employeeId = model.employee.id ?: throw IllegalStateException("Employee ID is null for holiday model ${model.id}"),
            employeeName = "${model.employee.person.firstName} ${model.employee.person.lastName}",
            startDate = model.startDate,
            endDate = model.endDate,
            type = model.holidayType.displayName,
            status = model.approvalStatus.name,
            reason = model.reason,
            approval = ch.baunex.timetracking.dto.ApprovalDTO(
                approved = model.approvalStatus == ApprovalStatus.APPROVED,
                approverId = model.approvedBy?.id,
                approverName = model.approvedBy?.let { "${it.person.firstName} ${it.person.lastName}" } ?: "",
                approvedAt = model.approvedAt,
                status = model.approvalStatus.name
            ),
            createdAt = model.createdAt
        )
    }
}
