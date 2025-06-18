package ch.baunex.controlreport.controller

import ch.baunex.controlreport.dto.*
import ch.baunex.controlreport.facade.ControlReportFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger

@Path("/api/controlreport")
@ApplicationScoped
class ControlReportRestController {

    @Inject
    lateinit var facade: ControlReportFacade

    private val log = Logger.getLogger(ControlReportRestController::class.java)

    /*** Einzelnen Bericht laden ***/
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getReport(@PathParam("id") id: Long): ControlReportDto? {
        log.info("Lade ControlReport mit ID $id")
        return facade.getReport(id)
    }

    /*** Neue Bericht anlegen ***/
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createReport(dto: ControlReportCreateDto): ControlReportDto {
        log.info("Erstelle neuen ControlReport: $dto")
        val result = facade.createReport(dto)
        log.info("ControlReport erstellt mit ID ${result.id}")
        return result
    }

    /*** Vorhandenen Bericht updaten ***/
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateReport(
        @PathParam("id") id: Long,
        dto: ControlReportUpdateDto
    ): ControlReportDto? {
        log.info("Update ControlReport $id mit Daten $dto")
        val result = facade.updateReport(id, dto)
        log.info("ControlReport $id aktualisiert")
        return result
    }

    /*** Bericht löschen ***/
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteReport(@PathParam("id") id: Long) {
        log.info("Lösche ControlReport mit ID $id")
        facade.deleteReport(id)
        log.info("ControlReport $id gelöscht")
    }

    /*** Liste aller Berichte ***/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listReports(): List<ControlReportDto> {
        log.info("Hole alle ControlReports")
        return facade.listReports()
    }

    /*** Defect‐Position hinzufügen ***/
    @POST
    @Path("/{id}/defect")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addDefect(
        @PathParam("id") id: Long,
        defectDto: DefectPositionCreateDto
    ): ControlReportDto? {
        log.info("Füge DefectPosition zu Report $id hinzu: $defectDto")
        val updatedReport = facade.addDefectPosition(id, defectDto)
        log.info("DefectPosition hinzugefügt, aktualisierter Report ID ${updatedReport?.id}")
        return updatedReport
    }

    // Analog könntest du noch Endpunkte für updateDefectPosition, removeDefectPosition,
    // addCompletionConfirmation hinzufügen, z.B.:

    // statt DefectPositionDto als Typ hier:
    @PUT
    @Path("/{id}/defect/{pos}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateDefect(
        @PathParam("id") id: Long,
        @PathParam("pos") positionNumber: Int,
        updateDto: DefectPositionUpdateDto    // <-- hier den passenden Update‐DTO nehmen
    ): ControlReportDto? {
        log.info("Aktualisiere DefectPosition $positionNumber in Report $id: $updateDto")
        val updated = facade.updateDefectPosition(id, positionNumber, updateDto)
        log.info("DefectPosition aktualisiert")
        return updated
    }

    @DELETE
    @Path("/{id}/defect/{pos}")
    @Produces(MediaType.APPLICATION_JSON)
    fun removeDefect(
        @PathParam("id") id: Long,
        @PathParam("pos") positionNumber: Int
    ): ControlReportDto? {
        log.info("Entferne DefectPosition $positionNumber aus Report $id")
        val updated = facade.removeDefectPosition(id, positionNumber)
        log.info("DefectPosition entfernt")
        return updated
    }
}
