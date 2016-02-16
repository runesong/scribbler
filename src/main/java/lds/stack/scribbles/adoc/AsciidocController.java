/**
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbles.adoc;

import lds.stack.scribbles.config.RepositoryProperties;
import lds.stack.scribbles.util.io.IO;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Provides REST-style access control to an Asciidoc resource.
 *
 * @author Robert Thornton
 */
@RestController
@RequestMapping("/api/adoc/{resource}")
public class AsciidocController
{
	private static final Logger LOG = LoggerFactory.getLogger(AsciidocController.class);

	@Autowired
	private RepositoryProperties repo;

	@Autowired
	private Asciidoctor asciidoctor;

	@RequestMapping(method = GET, produces = TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getResource(@PathVariable String resource) throws IOException
	{
		File adocFile = IO.getFile(repo.getContentStore(), resource + ".adoc");
		if (adocFile.exists()) {
			return ResponseEntity.ok(IO.read(adocFile, "UTF-8"));
		}
		return ResponseEntity.notFound().build();
	}

	@RequestMapping(method = PUT, consumes = TEXT_PLAIN_VALUE)
	public void postResource(@PathVariable String resource, @RequestBody String content) throws Exception
	{
		File adocFile = IO.getFile(repo.getContentStore(), resource + ".adoc");
		File htmlFile = IO.getFile(repo.getPublicContent(), resource + ".html");
		try (FileWriter out = new FileWriter(adocFile)) {
			out.write(content);
			out.flush();
		}
		LOG.info("{}.adoc written", resource);

		Map<String, Object> map = new HashMap<>();
		map.put(Attributes.SOURCE_HIGHLIGHTER, "highlightjs");
		asciidoctor.convertFile(adocFile, OptionsBuilder.options()
				.toDir(htmlFile.getParentFile())
				.attributes(map)
				.get());

		LOG.info("{}.html written", resource);
	}
}
