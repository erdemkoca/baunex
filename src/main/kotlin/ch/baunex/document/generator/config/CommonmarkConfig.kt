package ch.baunex.document.generator.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

@ApplicationScoped
class CommonmarkConfig {
    @Produces
    @ApplicationScoped
    fun markdownParser(): Parser {
        return Parser.builder().build()
    }

    @Produces
    @ApplicationScoped
    fun htmlRenderer(): HtmlRenderer {
        return HtmlRenderer.builder().build()
    }
} 