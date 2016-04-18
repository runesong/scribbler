package scribbler.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import scribbler.config.RepositoryProperties;

import java.io.*;
import java.nio.file.Files;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Repository
public class ContentRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(ContentRepository.class);

	@Autowired
	private RepositoryProperties repo;

	public String readTemplate()
	{
		return read(repo.getTemplateFile());
	}

	public String read(String resourcePath)
	{
		File resourceFile = getFile(resourcePath);
		if (!resourceFile.exists()) {
			throw new ResourceNotFoundException(resourcePath);
		}
		return read(resourceFile);
	}

	public void write(String resourcePath, String content) throws ResourceException
	{
		File outputFile = getFile(resourcePath);
		try {
			Files.createDirectories(outputFile.getParentFile().toPath());
			try (FileWriter out = new FileWriter(outputFile)) {
				out.write(content);
				out.flush();
			}
			LOG.info("/{} written", resourcePath);
		} catch (FileNotFoundException ex) {
			throw new ResourceNotFoundException(outputFile.getPath(), ex);
		} catch (IOException ex) {
			throw new ResourceException(outputFile.getPath(), ex);
		}
	}

	private  String read(File f)
	{
		try {
			return read(f, "UTF-8");
		} catch (IOException ex) {
			LOG.error("Failed to read {}: {}", f, ex);
		}
		return "";
	}

	private String read(File f, String charset) throws IOException
	{
		return new String(readImpl(f), charset);
	}

	private byte[] readImpl(File f) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		try (InputStream in = new FileInputStream(f)) {
			copy(in, out);
		}
		return out.toByteArray();
	}

	private int copy(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[8092];
		int count;
		int total = 0;
		do {
			count = in.read(buffer, 0, buffer.length);
			if (count > 0) {
				total += count;
				out.write(buffer, 0, count);
			}
		} while (count > -1);
		out.flush();
		return total;
	}

	private File getFile(String resourcePath)
	{
		// TODO deploy and run with a FileSecurity policy that only allows read & write to the repository folder

		// Ascending the file structure is a security risk. Don't allow it.
		if (resourcePath.contains("..")) {
			LOG.warn("Illegal file path: {}", resourcePath);
			throw new HttpServerErrorException(BAD_REQUEST); //
		}

		File result = new File(repo.getContentStore(), resourcePath).getAbsoluteFile();

		// Sanity check. Make sure the file is contained is in the container folder.
		File parent = result.getParentFile();
		while (parent != null) {
			if (parent.equals(repo.getContentStore())) {
				return result;
			}
			parent = parent.getParentFile();
		}
		LOG.warn("File is not contained in {} : {}", repo.getContentStore(), resourcePath);
		throw new ResourceNotFoundException(resourcePath);
	}
}
