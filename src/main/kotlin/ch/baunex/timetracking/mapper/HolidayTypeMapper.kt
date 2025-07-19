package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayTypeDTO
import ch.baunex.timetracking.dto.HolidayTypeCreateDTO
import ch.baunex.timetracking.dto.HolidayTypeUpdateDTO
import ch.baunex.timetracking.model.HolidayTypeModel
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayTypeMapper {
    private val log = Logger.getLogger(HolidayTypeMapper::class.java)

    fun toDTO(model: HolidayTypeModel): HolidayTypeDTO {
        log.debug("Mapping holiday type model to DTO: ${model.code}")
        return HolidayTypeDTO(
            id = model.id,
            code = model.code,
            displayName = model.displayName,
            factor = model.factor,
            active = model.active,
            description = model.description,
            sortOrder = model.sortOrder,
            isSystemType = model.isSystemType,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    fun toModel(dto: HolidayTypeCreateDTO): HolidayTypeModel {
        log.debug("Mapping holiday type create DTO to model: ${dto.code}")
        return HolidayTypeModel().apply {
            code = dto.code
            displayName = dto.displayName
            factor = dto.factor
            description = dto.description
            sortOrder = dto.sortOrder
            active = true
            isSystemType = false
            createdAt = LocalDate.now()
        }
    }
    
    fun toModelFromDTO(dto: HolidayTypeDTO): HolidayTypeModel {
        log.debug("Mapping holiday type DTO to model: ${dto.code}")
        return HolidayTypeModel().apply {
            id = dto.id
            code = dto.code
            displayName = dto.displayName
            factor = dto.factor
            description = dto.description
            sortOrder = dto.sortOrder
            active = dto.active
            isSystemType = dto.isSystemType
            createdAt = dto.createdAt ?: LocalDate.now()
            updatedAt = dto.updatedAt ?: LocalDate.now()
        }
    }

    fun updateModel(model: HolidayTypeModel, dto: HolidayTypeUpdateDTO) {
        log.debug("Updating holiday type model: ${model.code}")
        dto.displayName?.let { model.displayName = it }
        dto.factor?.let { model.factor = it }
        dto.active?.let { model.active = it }
        dto.description?.let { model.description = it }
        dto.sortOrder?.let { model.sortOrder = it }
        model.updatedAt = LocalDate.now()
    }

    fun toDTOList(models: List<HolidayTypeModel>): List<HolidayTypeDTO> {
        log.debug("Mapping ${models.size} holiday type models to DTOs")
        return models.map { toDTO(it) }
    }
} 