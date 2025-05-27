package ch.baunex.document.generator.facade

import ch.baunex.document.generator.model.DocumentTemplate
import ch.baunex.document.generator.service.DocumentGeneratorService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class DocumentGeneratorFacade @Inject constructor(
    private val documentGeneratorService: DocumentGeneratorService
) {
    fun generateDocument(template: DocumentTemplate): ByteArray {
        return documentGeneratorService.generateDocument(template)
    }

    fun buildHtml(template: DocumentTemplate): String {
        return documentGeneratorService.buildHtml(template)
    }
} 