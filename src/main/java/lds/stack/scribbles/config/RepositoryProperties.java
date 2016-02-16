/*
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbles.config;

import lds.stack.scribbles.util.io.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ConfigurationProperties("repository")
public class RepositoryProperties
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryProperties.class);

	private File contentStore = new File("repository/content");
	private File publicContent = new File("repository/public");
	private File includeHeader = new File("repository/include/header.html");
	private File includeFooter = new File("repository/include/header.html");

	@PostConstruct
	void init() throws IOException
	{
		Files.createDirectories(contentStore.toPath());
		Files.createDirectories(publicContent.toPath());

		contentStore = contentStore.getAbsoluteFile();
		publicContent = publicContent.getAbsoluteFile();
		includeHeader = includeHeader.getAbsoluteFile();
		includeFooter = includeFooter.getAbsoluteFile();
	}

	public File getContentStore()
	{
		return contentStore;
	}

	public void setContentStore(File contentStore)
	{
		this.contentStore = contentStore;
	}

	public File getPublicContent()
	{
		return publicContent;
	}

	public void setPublicContent(File publicContent)
	{
		this.publicContent = publicContent;
	}

	public File getIncludeHeader()
	{
		return includeHeader;
	}

	public void setIncludeHeader(File includeHeader)
	{
		this.includeHeader = includeHeader;
	}

	public File getIncludeFooter() {
		return includeFooter;
	}

	public void setIncludeFooter(File includeFooter)
	{
		this.includeFooter = includeFooter;
	}

	public String readHeader()
	{
		return read(getIncludeHeader());
	}

	public String readFooter()
	{
		return read(getIncludeFooter());
	}

	private static String read(File f)
	{
		try {
			return IO.read(f, "UTF-8");
		} catch (IOException ex) {
			LOG.error("Failed to read {}: {}", f, ex);
		}
		return "";
	}
}
