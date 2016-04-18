package scribbler.config;

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

	private File contentStore = new File("public");
	private File templateFile = new File(contentStore, "include/template.html");

	@PostConstruct
	void init() throws IOException
	{
		contentStore = contentStore.getCanonicalFile();
		templateFile = templateFile.getAbsoluteFile();
		Files.createDirectories(contentStore.toPath());
	}

	public File getContentStore()
	{
		return contentStore;
	}

	public File getTemplateFile()
	{
		return templateFile;
	}
}
