package scribbler.content;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.OptionsBuilder;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.pegdown.Extensions.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Provides REST-style access control to a Markdown resource.
 *
 * @author Robert Thornton
 */
@RestController
@RequestMapping("/api/content")
public class MarkdownController
{
	private static final Logger LOG = LoggerFactory.getLogger(MarkdownController.class);

	@Autowired
	private Asciidoctor asciidoctor;

	@Autowired
	private ContentRepository repo;

	@ResponseStatus(OK)
	@RequestMapping(method = GET, path = "/**/*.md", produces = "text/asciidoctor")
	public ResponseEntity<?> getAsciidoctorContent(HttpServletRequest request) throws IOException
	{
		String resource = request.getServletPath().substring("/api/content/".length());
		return ResponseEntity.ok(repo.read(resource));
	}

	@ResponseStatus(OK)
	@RequestMapping(method = GET, path = "/**/*.adoc", produces = "text/markdown")
	public ResponseEntity<?> getMarkdownContent(HttpServletRequest request) throws IOException
	{
		String resource = request.getServletPath().substring("/api/content/".length());
		return ResponseEntity.ok(repo.read(resource));
	}

	@ResponseStatus(NO_CONTENT)
	@RequestMapping(method = PUT, path = "/**/*.md", consumes = "text/markdown")
	public void putMarkdown(HttpServletRequest request, @RequestBody String markdownContent) throws IOException
	{
		String title = request.getParameter("title");
		title = title == null ? "" : title.replaceAll("\\W+", " ");
		String markdownResource = request.getServletPath().substring("/api/content/".length());
		String htmlResource = markdownResource.replaceAll("\\.md$", ".html");

		repo.write(markdownResource, markdownContent);

		String content = getPegDownProcessor().markdownToHtml(markdownContent.toCharArray());
		renderTemplate(markdownResource, htmlResource, title, "text/markdown", content);
	}

	@ResponseStatus(NO_CONTENT)
	@RequestMapping(method = PUT, path = "/**/*.adoc", consumes = "text/asciidoctor")
	public void putAsciidoctor(HttpServletRequest request, @RequestBody String adocContent) throws IOException
	{
		String title = request.getParameter("title");
		title = title == null ? "" : title.replaceAll("\\W+", " ");
		String adocResource = request.getServletPath().substring("/api/content/".length());
		String htmlResource = adocResource.replaceAll("\\.md$", ".html");

		repo.write(adocResource, adocContent);

		Map<String, Object> attributes = new HashMap<>();
		attributes.put(Attributes.SOURCE_HIGHLIGHTER, "highlightjs");

		StringReader reader = new StringReader(adocContent);
		StringWriter writer = new StringWriter();
		asciidoctor.convert(reader, writer, OptionsBuilder.options().attributes(attributes).get());
		renderTemplate(adocResource, htmlResource, title, "text/asciidoctor", writer.toString());
	}

	private void renderTemplate(String sourcePath, String targetPath, String title, String mediaType, String content)
	{
		String htmlContent = repo.readTemplate()
				.replace("{{title}}", title)
				.replace("{{path}}", "/" + sourcePath)
				.replace("{{format}}", mediaType)
				.replace("{{content}}", content);

		repo.write(targetPath, htmlContent);
	}

	private PegDownProcessor getPegDownProcessor() {
		return new PegDownProcessor(
				SMARTS | QUOTES | ABBREVIATIONS | AUTOLINKS | TABLES | DEFINITIONS | FENCED_CODE_BLOCKS |
				SUPPRESS_HTML_BLOCKS | SUPPRESS_INLINE_HTML | WIKILINKS | STRIKETHROUGH | ATXHEADERSPACE |
				FORCELISTITEMPARA | TASKLISTITEMS | EXTANCHORLINKS);
	}
}
