package ch.baunex.controlreport.service

import ch.baunex.controlreport.model.ControlReportModel
import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.controlreport.repository.DefectPositionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@ApplicationScoped
class DefectPositionService(
    private val defectPositionRepo: DefectPositionRepository,
    private val controlReportService: ControlReportService
) {

    @Transactional
    fun createFromNote(note: NoteModel): DefectPositionModel {
        // 1) find‐or‐create the ControlReportModel
        val report: ControlReportModel = note.controlReport
            ?: run {
                val newReport = controlReportService
                    .getOrInitializeModel(
                        note.project?.id
                            ?: throw IllegalArgumentException("Note ${note.id} has no project")
                    )
                note.controlReport = newReport
                note.persist()
                newReport
            }

        // 2) determine next position number
        val nextNumber = (report.defectPositions.maxOfOrNull { it.positionNumber } ?: 0) + 1

        // 3) build & save the DefectPosition
        val defect = DefectPositionModel().apply {
            this.note            = note
            this.controlReport   = report
            this.positionNumber  = nextNumber
            this.description     = note.content                 // initialer Text aus Note
            this.buildingLocation = null                        // leer beim Anlegen
            this.createdAt       = LocalDateTime.now()
        }

        defectPositionRepo.persist(defect)
        report.defectPositions.add(defect)

        return defect
    }

    fun getById(id: Long): DefectPositionModel =
        defectPositionRepo.findById(id)
            ?: throw IllegalArgumentException("DefectPosition $id not found")

    @Transactional
    fun update(
        id: Long,
        description: String?,
        buildingLocation: String?,
        normReferences: List<String>?,
        resolutionStamp: String?,
        resolutionSignature: String?
    ): DefectPositionModel {
        val defect = getById(id)
        description?.let { defect.description = it }
        buildingLocation?.let { defect.buildingLocation = it }
        defect.updatedAt = LocalDateTime.now()
        return defect
    }

    @Transactional
    fun delete(id: Long) {
        val defect = getById(id)
        defectPositionRepo.delete(defect)
    }
}
