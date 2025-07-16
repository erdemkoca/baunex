package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.model.HolidayTypeModel
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class HolidayTypeMapper {

    fun toDTO(model: HolidayTypeModel): HolidayTypeDTO {
        return HolidayTypeDTO(
            id = model.id,
            code = model.code,
            displayName = model.displayName,
            defaultExpectedHours = model.defaultExpectedHours,
            active = model.active,
            description = model.description,
            sortOrder = model.sortOrder,
            isSystemType = model.isSystemType,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    fun toModel(dto: HolidayTypeCreateDTO): HolidayTypeModel {
        return HolidayTypeModel().apply {
            code = dto.code
            displayName = dto.displayName
            defaultExpectedHours = dto.defaultExpectedHours
            description = dto.description
            sortOrder = dto.sortOrder
            active = true
            isSystemType = false
            createdAt = LocalDate.now()
        }
    }

    fun updateModel(model: HolidayTypeModel, dto: HolidayTypeUpdateDTO) {
        dto.displayName?.let { model.displayName = it }
        dto.defaultExpectedHours?.let { model.defaultExpectedHours = it }
        dto.active?.let { model.active = it }
        dto.description?.let { model.description = it }
        dto.sortOrder?.let { model.sortOrder = it }
        model.updatedAt = LocalDate.now()
    }

    fun toDTOList(models: List<HolidayTypeModel>): List<HolidayTypeDTO> {
        return models.map { toDTO(it) }
    }
} 