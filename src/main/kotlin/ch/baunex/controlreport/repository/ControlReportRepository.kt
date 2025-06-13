package ch.baunex.controlreport.repository

import ch.baunex.controlreport.model.ControlReportModel
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ControlReportRepository : PanacheRepository<ControlReportModel> {

    /**
     * Alle Berichte zu einem bestimmten Projekt
     */
    fun findByProjectId(projectId: Long): List<ControlReportModel> =
        find("project.id", projectId).list()

    /**
     * Alle Berichte zu einem bestimmten Kunden
     */
    fun findByCustomerId(customerId: Long): List<ControlReportModel> =
        find("customerId", customerId).list()

    /**
     * Einen Bericht mitsamt aller DefectPositions und Notizen laden
     */
    fun findByIdWithDetails(id: Long): ControlReportModel? =
        find(
            "SELECT cr FROM ControlReportModel cr " +
                    "LEFT JOIN FETCH cr.defectPositions " +
                    "LEFT JOIN FETCH cr.notes " +
                    "WHERE cr.id = ?1",
            id
        ).firstResult()
}
