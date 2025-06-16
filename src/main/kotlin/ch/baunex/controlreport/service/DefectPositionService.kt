package ch.baunex.controlreport.service

import ch.baunex.controlreport.model.DefectPositionModel
import ch.baunex.notes.model.NoteModel
import ch.baunex.controlreport.repository.DefectPositionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDateTime

@ApplicationScoped
class DefectPositionService(
    private val defectPositionRepo: DefectPositionRepository
) {

    /**
     * Create a DefectPosition based on a Note of category MÄNGEL.
     * The Note must already reference a ControlReport.
     */
    @Transactional
    fun createFromNote(note: NoteModel): DefectPositionModel {
        val report = note.controlReport
            ?: throw IllegalArgumentException("Note ${note.id} is not linked to a ControlReport")

        // determine next position number
        val nextNumber = (report.defectPositions.maxOfOrNull { it.positionNumber } ?: 0) + 1

        val defect = DefectPositionModel().apply {
            controlReport  = report
            positionNumber = nextNumber
            description    = note.content
            createdAt      = LocalDateTime.now()
            // you can add normReferences etc. later
        }
        defectPositionRepo.persist(defect)
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
