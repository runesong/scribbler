package scribbler.content

import org.springframework.stereotype.Repository
import scribbler.LOG
import java.io.*
import java.nio.file.Files

@Repository
class DocumentRepository
{
	private val repo : File = File("public").absoluteFile

	fun write(path: String, content: String)
	{
		val f = getFile(path)
		try {
			Files.createDirectories(f.parentFile.toPath())
			FileWriter(f).use { out ->
				out.write(content)
				out.flush()
			}
			LOG.info("{} written", f)
		} catch (ex: FileNotFoundException) {
			throw DocumentNotFoundException(f.path, ex)
		}
	}

	fun read(path: String, output: OutputStream)
	{
		read(getFile(path), output)
	}

	private fun read(f: File, output: OutputStream)
	{
		try {
			FileInputStream(f).copyTo(output)
			output.flush()
		} catch (ex: FileNotFoundException) {
			throw DocumentNotFoundException(f.path, ex)
		}

	}

	private fun getFile(resourcePath: String): File
	{
		// Ascending the file structure is a security risk. Don't allow it.
		if (!resourcePath.contains("..")) {
			val candidate = File(repo, resourcePath).absoluteFile

			// Sanity check. Make sure the file is contained is in the container folder.
			var parent: File? = candidate.parentFile
			while (parent != null) {
				if (parent == repo) {
					return candidate
				}
				parent = parent.parentFile
			}
		}
		LOG.warn("Invalid file request: {}", resourcePath)
		throw DocumentNotFoundException(resourcePath)
	}
}