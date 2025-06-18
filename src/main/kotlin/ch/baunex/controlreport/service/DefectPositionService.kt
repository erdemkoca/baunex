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
                // bootstrap a report for this note’s project
                val newReport = controlReportService
                    .getOrInitializeModel(
                        note.project?.id
                            ?: throw IllegalArgumentException("Note ${note.id} has no project")
                    )
                // link & persist the FK on NoteModel
                note.controlReport = newReport
                note.persist()
                newReport
            }

        // 2) determine next position number
        val nextNumber = (report.defectPositions.maxOfOrNull { it.positionNumber } ?: 0) + 1

        // 3) build & save the DefectPosition, wiring both sides
        val defect = DefectPositionModel().apply {
            this.note           = note
            this.controlReport  = report
            this.positionNumber = nextNumber
            this.description    = note.content
            this.createdAt      = LocalDateTime.now()
        }
        defectPositionRepo.persist(defect)

        // 4) add to the in‐memory list to keep Hibernate happy
        report.defectPositions.add(defect)

        return defect
    }

    /** List all defects for a given control‐report ID */
    fun listByReport(reportId: Long): List<DefectPositionModel> =
        defectPositionRepo.findByControlReportId(reportId)

    /** Fetch a single defect by its id */
    fun getById(id: Long): DefectPositionModel =
        defectPositionRepo.findById(id)
            ?: throw IllegalArgumentException("DefectPosition $id not found")

    /**
     * Update an existing defect position; for example to add resolution info.
     */
    @Transactional
    fun update(
        id: Long,
        description: String?,
        photoUrl: String?,
        normReferences: List<String>?,
        resolutionStamp: String?,
        resolutionSignature: String?
    ): DefectPositionModel {
        val defect = getById(id)
        description?.let    { defect.description = it }
        normReferences?.let { defect.normReferences = it.toMutableList() }
        return defect
    }

    /** Remove a defect position entirely */
    @Transactional
    fun delete(id: Long) {
        val defect = getById(id)
        defectPositionRepo.delete(defect)
    }
}
