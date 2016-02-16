/*
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbles.markdown;

import lds.stack.scribbles.config.RepositoryProperties;
import lds.stack.scribbles.util.io.IO;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.pegdown.Extensions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Provides REST-style access control to a Markdown resource.
 *
 * @author Robert Thornton
 */
@RestController
@RequestMapping("/api/**/*.md")
public class MarkdownController
{
	private static final Logger LOG = LoggerFactory.getLogger(MarkdownController.class);

	@Autowired
	private RepositoryProperties repo;

	@ResponseStatus(OK)
	@RequestMapping(method = GET, produces = TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getResource(HttpServletRequest request) throws IOException
	{
		String resource = request.getServletPath().substring("/api/".length());
		File markdownFile = IO.getFile(repo.getContentStore(), resource);
		if (markdownFile.exists()) {
			return ResponseEntity.ok(IO.read(markdownFile, "UTF-8"));
		}
		return ResponseEntity.notFound().build();
	}

	@ResponseStatus(NO_CONTENT)
	@RequestMapping(method = PUT, consumes = TEXT_PLAIN_VALUE)
	public void putResource(HttpServletRequest request, @RequestBody String content) throws IOException
	{
		String title = request.getParameter("title");
		title = title == null ? "" : title.replaceAll("\\W+", " ");
		String markdownResource = request.getServletPath().substring("/api/".length());
		String htmlResource = markdownResource.replaceAll("\\.md$", ".html");
		File markdownFile = IO.getFile(repo.getContentStore(), markdownResource);
		File htmlFile = IO.getFile(repo.getPublicContent(), htmlResource);

		Files.createDirectories(markdownFile.getParentFile().toPath());
		Files.createDirectories(htmlFile.getParentFile().toPath());

		try (FileWriter out = new FileWriter(markdownFile)) {
			out.write(content);
			out.flush();
		}

		LOG.info("{}.markdown written", "/" + markdownResource);

		String markdownContent =
				repo.readHeader()
						.replace("{{title}}", title)
						.replace("{{path}}", "/" + htmlResource)
						.replace("{{format}}", ".md") +
				getPegDownProcessor().markdownToHtml(content.toCharArray()) +
				repo.readFooter()
						.replace("{{title}}", title)
						.replace("{{path}}", "/" + htmlResource)
						.replace("{{format}}", ".md");

		try (FileWriter out = new FileWriter(htmlFile)) {
			out.write(markdownContent);
			out.flush();
		}

		LOG.info("{}.html written", markdownResource);
	}

	private PegDownProcessor getPegDownProcessor() {
		return new PegDownProcessor(
				SMARTS | QUOTES | ABBREVIATIONS | AUTOLINKS | TABLES | DEFINITIONS | FENCED_CODE_BLOCKS |
				SUPPRESS_HTML_BLOCKS | SUPPRESS_INLINE_HTML | WIKILINKS | STRIKETHROUGH | ATXHEADERSPACE |
				FORCELISTITEMPARA | TASKLISTITEMS | EXTANCHORLINKS);
	}
}
