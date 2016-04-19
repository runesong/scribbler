package scribbler.content

import com.fasterxml.jackson.databind.ObjectMapper
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.OptionsBuilder
import org.pegdown.Extensions.*
import org.pegdown.PegDownProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.util.*

@Service
class DocumentService
{
	@Autowired
	val repository: DocumentRepository? = null

	@Autowired
	val asciidoctorProcessor: Asciidoctor? = null

	@Autowired
	val objectMapper : ObjectMapper? = null

	private val attributes = HashMap<String, Any>()
	init {
		attributes.put(Attributes.SOURCE_HIGHLIGHTER, "highlightjs")
	}

	fun save(path: String, source: Document)
	{
		val json: String = objectMapper!!.writeValueAsString(source);
		repository!!.write(path, json)
		when (source.format) {
			"text/markdown" ->  renderTemplate(source.title, source.format, path, renderMarkdown(path, source.body))
			"text/asciidoctor" -> renderTemplate(source.title, source.format, path, renderAsciidoctor(path, source.body))
			else -> throw DocumentProcessingException(path)
		}
	}

	//-- Private Implementation --------------------------------------------------------------------------------------//

	private fun renderMarkdown(path: String, content: String): String
	{
		try {
			return markdownProcessor.markdownToHtml(content.toCharArray())
		} catch (ex: RuntimeException) {
			throw DocumentProcessingException(path, ex)
		}

	}

	private fun renderAsciidoctor(path: String, content: String): String
	{
		try {
			StringReader(content).use { reader ->
				StringWriter().use { writer ->
					asciidoctorProcessor!!.convert(reader, writer, OptionsBuilder.options().attributes(attributes).get())
					return writer.toString()
				}
			}
		} catch (ex: IOException) {
			throw DocumentProcessingException(path, ex)
		}
	}

	private fun renderTemplate(title: String, format: String, path: String, html: String)
	{
		val htmlPath = path.replace("\\.json$".toRegex(), ".html")
		val output = ByteArrayOutputStream()
		repository!!.read("include/template.html", output);
		repository.write(htmlPath, output.toString("UTF-8")
				.replace("{{title}}", title)
				.replace("{{path}}", path)
				.replace("{{format}}", format)
				.replace("{{content}}", html))
	}

	private val markdownProcessor: PegDownProcessor get() = PegDownProcessor(
			SMARTS or QUOTES or ABBREVIATIONS or AUTOLINKS or TABLES or DEFINITIONS or FENCED_CODE_BLOCKS or
					SUPPRESS_HTML_BLOCKS or SUPPRESS_INLINE_HTML or WIKILINKS or STRIKETHROUGH or ATXHEADERSPACE or
					FORCELISTITEMPARA or TASKLISTITEMS or EXTANCHORLINKS)
}
