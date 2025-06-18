package ch.baunex.controlreport.repository

import ch.baunex.controlreport.model.DefectPositionModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

/**
 * Repository for persisting and querying DefectPositionModel entities.
 */
@ApplicationScoped
class DefectPositionRepository : PanacheRepository<DefectPositionModel> {
    fun findByControlReportId(reportId: Long) =
        list("controlReport.id", reportId)

    fun deleteByReportIdAndNotInIds(reportId: Long, keepIds: Collection<Long>) {
        delete("controlReport.id = ?1 and id not in ?2", reportId, keepIds)
    }
}
