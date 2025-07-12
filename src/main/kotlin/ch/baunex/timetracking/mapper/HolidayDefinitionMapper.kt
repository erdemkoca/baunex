package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayDefinitionDTO
import ch.baunex.timetracking.model.HolidayDefinitionModel
import ch.baunex.timetracking.model.HolidayDefinitionType
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class HolidayDefinitionMapper {

    fun toModel(dto: HolidayDefinitionDTO): HolidayDefinitionModel {
        return HolidayDefinitionModel().apply {
            id = dto.id
            year = dto.year
            date = dto.date
            name = dto.name
            canton = dto.canton
            isFixed = dto.isFixed
            isEditable = dto.isEditable
            active = dto.active
            isWorkFree = dto.isWorkFree
            holidayType = HolidayDefinitionType.fromDisplayNameOrDefault(dto.holidayType)
            description = dto.description
            createdAt = dto.createdAt
            updatedAt = dto.updatedAt
        }
    }

    fun toDTO(model: HolidayDefinitionModel): HolidayDefinitionDTO {
        return HolidayDefinitionDTO(
            id = model.id,
            year = model.year,
            date = model.date,
            name = model.name,
            canton = model.canton,
            isFixed = model.isFixed,
            isEditable = model.isEditable,
            active = model.active,
            isWorkFree = model.isWorkFree,
            holidayType = model.holidayType.displayName,
            description = model.description,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
} 