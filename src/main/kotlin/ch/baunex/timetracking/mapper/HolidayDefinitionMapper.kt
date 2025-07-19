package ch.baunex.timetracking.mapper

import ch.baunex.timetracking.dto.HolidayDefinitionDTO
import ch.baunex.timetracking.model.HolidayDefinitionModel
import ch.baunex.timetracking.model.HolidayTypeModel
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

@ApplicationScoped
class HolidayDefinitionMapper @Inject constructor(
    private val holidayTypeService: ch.baunex.timetracking.service.HolidayTypeService,
    private val holidayTypeMapper: ch.baunex.timetracking.mapper.HolidayTypeMapper
) {
    private val log = Logger.getLogger(HolidayDefinitionMapper::class.java)

    fun toModel(dto: HolidayDefinitionDTO): HolidayDefinitionModel {
        log.debug("Mapping holiday definition DTO to model: ${dto.name}")
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
            // TODO: Get holiday type from database by code or name
        holidayType = getHolidayTypeFromDatabase(dto.holidayType)
            description = dto.description
            createdAt = dto.createdAt
            updatedAt = dto.updatedAt
        }
    }

    fun toDTO(model: HolidayDefinitionModel): HolidayDefinitionDTO {
        log.debug("Mapping holiday definition model to DTO: ${model.name}")
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
    
    private fun getHolidayTypeFromDatabase(holidayTypeName: String?): HolidayTypeModel {
        if (holidayTypeName == null) {
                    return holidayTypeService.getHolidayTypeByCode("PUBLIC_HOLIDAY")?.let { 
            holidayTypeMapper.toModelFromDTO(it) 
        } ?: throw IllegalStateException("Default holiday type not found")
        }
        
        // Try to find by display name first
        val holidayType = holidayTypeService.getAllHolidayTypes()
            .find { it.displayName == holidayTypeName }
            ?.let { holidayTypeMapper.toModelFromDTO(it) }
        
        return holidayType ?: holidayTypeService.getHolidayTypeByCode("PUBLIC_HOLIDAY")?.let { 
            holidayTypeMapper.toModelFromDTO(it) 
        } ?: throw IllegalStateException("Holiday type not found: $holidayTypeName")
    }
} 