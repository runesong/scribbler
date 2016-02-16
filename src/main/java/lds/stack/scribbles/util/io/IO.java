/*
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbles.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpServerErrorException;

import java.io.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 *
 *
 * @author Robert Thornton
 */
public class IO
{
	private static final Logger LOG = LoggerFactory.getLogger(IO.class);

	public static int copy(InputStream in, OutputStream out) throws IOException
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

	public static byte[] read(File f) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		try (InputStream in = new FileInputStream(f)) {
			copy(in, out);
		}
		return out.toByteArray();
	}

	public static String read(File f, String charset) throws IOException
	{
		return new String(read(f), charset);
	}

	public static File getFile(File container, String path)
	{
		// TODO deploy and run with a FileSecurity policy that only allows read & write to the repository folder

		// Ascending the file structure is a security risk. Don't allow it.
		if (path.contains("..")) {
			LOG.warn("Illegal file path: {}", path);
			throw new HttpServerErrorException(BAD_REQUEST); //
		}

		File result = new File(container, path).getAbsoluteFile();

		// Sanity check. Make sure the file is contained is in the container folder.
		File parent = result.getParentFile();
		while (parent != null) {
			if (parent.equals(container)) {
				return result;
			}
			parent = parent.getParentFile();
		}
		LOG.warn("File is not contained in {} : {}", container, path);
		throw new HttpServerErrorException(BAD_REQUEST);
	}
}
