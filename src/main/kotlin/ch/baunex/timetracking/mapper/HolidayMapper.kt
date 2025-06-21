package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayDTO
import ch.baunex.timetracking.model.ApprovalStatus
import ch.baunex.timetracking.model.HolidayModel
import ch.baunex.timetracking.model.HolidayType
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class HolidayMapper {

    fun toModel(dto: HolidayDTO): HolidayModel {
        return HolidayModel().apply {
            id = dto.id
            startDate = dto.startDate
            endDate = dto.endDate
            type = HolidayType.valueOf(dto.type)
            status = ApprovalStatus.valueOf(dto.status)
            reason = dto.reason
        }
    }

    fun toDTO(model: HolidayModel): HolidayDTO {
        return HolidayDTO(
            id = model.id,
            employeeId = model.employee.id!!,
            employeeName = "${model.employee.person.firstName} ${model.employee.person.lastName}",
            startDate = model.startDate,
            endDate = model.endDate,
            type = model.type.name,
            status = model.status.name,
            reason = model.reason
        )
    }
}
