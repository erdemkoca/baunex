//package ch.baunex.controlreport.service
//
//
//import ch.baunex.controlreport.repository.DefectPositionRepository
//import ch.baunex.notes.model.NoteModel
//import ch.baunex.project.model.ProjectModel
//import ch.baunex.project.model.ProjectType
//import ch.baunex.project.repository.ProjectRepository
//import ch.baunex.project.service.ProjectService
//import ch.baunex.user.model.*
//import ch.baunex.user.repository.CustomerRepository
//import io.quarkus.test.junit.QuarkusTest
//import jakarta.inject.Inject
//import jakarta.transaction.Transactional
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import java.time.LocalDate
//import java.time.LocalDateTime
//
//@QuarkusTest
//class DefectPositionServiceTest {
//
//    @Inject
//    lateinit var defectPositionService: DefectPositionService
//
//    @Inject
//    lateinit var defectPositionRepository: DefectPositionRepository
//
//    @Inject
//    lateinit var projectRepository: ProjectRepository
//
//    @Inject
//    lateinit var customerRepository: CustomerRepository
//
//    lateinit var project: ProjectModel
//    lateinit var note: NoteModel
//
//    @BeforeEach
//    @Transactional
//    fun setup() {
//        // Setup minimal Project and Note structure
//        val customer = CustomerModel().apply {
//            person = PersonModel().apply {
//                firstName = "Max"
//                lastName = "Muster"
//                details = PersonDetails().apply {
//                    street = "Kundenweg 5"
//                    zipCode = "3000"
//                    city = "Bern"
//                }
//            }
//        }
//        customerRepository.persist(customer)
//
//        val project = ProjectModel().apply {
//            name = "Testprojekt"
//            this.customer = customer   // <-- wichtig: gesetzt!
//            buildingType = ProjectType.COMMERCIAL_BUILDING
//            parcelNumber = "1234"
//            projectNumber = 1001
//        }
//        projectRepository.persist(project)
//
//        val person = PersonModel().apply {
//            firstName = "Anna"
//            lastName = "Admin"
//            details = PersonDetails().apply {
//                street = "Musterstrasse 1"
//                zipCode = "8000"
//                city = "Zürich"
//            }
//        }
//        person.persist()
//
//        val employee = EmployeeModel().apply {
//            this.person = person
//            this.email = "anna.admin@baunex.ch"
//            this.passwordHash = "secure-hash"
//            this.role = Role.ADMIN
//            this.ahvNumber = "756.1234.5678.90"
//            this.hourlyRate = 150.0
//        }
//        employee.persist()
//
//        note = NoteModel().apply {
//            content = "Testnotiz"
//            createdBy = employee
//            this.project = project     // <--- FEHLTE! Das ist Pflichtfeld in der Entity
//            createdAt = LocalDate.now()
//        }
//        note.persist()
//    }
//
//    @Test
//    @Transactional
//    fun `should create defect position from note`() {
//        val defect = defectPositionService.createFromNote(note)
//
//        assertNotNull(defect.id)
//        assertEquals(note.content, defect.description)
//        assertEquals(1, defect.positionNumber)
//        assertEquals(note, defect.note)
//        assertNotNull(defect.controlReport)
//    }
//
//    @Test
//    @Transactional
//    fun `should increment position number for new defect`() {
//        defectPositionService.createFromNote(note)
//        val note2 = NoteModel().apply {
//            content = "Zweiter Mangel"
//            project = note.project
//            controlReport = note.controlReport
//        }
//        note2.persist()
//
//        val defect2 = defectPositionService.createFromNote(note2)
//
//        assertEquals(2, defect2.positionNumber)
//    }
//
//    @Test
//    @Transactional
//    fun `should throw when note has no project`() {
//        val invalidNote = NoteModel().apply {
//            content = "Fehlerhafte Note"
//        }
//        invalidNote.persist()
//
//        val ex = assertThrows(IllegalArgumentException::class.java) {
//            defectPositionService.createFromNote(invalidNote)
//        }
//        assertTrue(ex.message!!.contains("has no project"))
//    }
//
//    @Test
//    @Transactional
//    fun `should update defect position`() {
//        val defect = defectPositionService.createFromNote(note)
//
//        val updated = defectPositionService.update(
//            defect.id!!,
//            description = "Neuer Text",
//            buildingLocation = "Keller",
//            normReferences = null,
//            resolutionStamp = null,
//            resolutionSignature = null
//        )
//
//        assertEquals("Neuer Text", updated.description)
//        assertEquals("Keller", updated.buildingLocation)
//        assertNotNull(updated.updatedAt)
//    }
//
//    @Test
//    @Transactional
//    fun `should delete defect position`() {
//        val defect = defectPositionService.createFromNote(note)
//        val id = defect.id!!
//        defectPositionService.delete(id)
//
//        val found = defectPositionRepository.findById(id)
//        assertNull(found)
//    }
//}
