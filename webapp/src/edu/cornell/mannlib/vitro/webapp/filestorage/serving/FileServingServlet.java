/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.serving;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;

/**
 * <p>
 * Handles a request to serve an uploaded file from the file storage system.
 * </p>
 * <p>
 * The path of the request should be the "alias URL" of the desired file. We
 * need to:
 * <ul>
 * <li>Use the alias URL to find the URI of the file bytestream object.</li>
 * <li>Find the file surrogate object to get the MIME type of the file, and
 * confirm the filename.</li>
 * <li>Set the MIME type on the output stream and serve the bytes.</li>
 * </ul>
 * </p>
 * <p>
 * If the request is superficially correct, but no such file can be found,
 * return a 404. If there is a break in the data structures within the model or
 * the file system, return a 500.
 * </p>
 */
public class FileServingServlet extends VitroHttpServlet {
	private static final Log log = LogFactory.getLog(FileServingServlet.class);

	private FileStorage fileStorage;

	/**
	 * Get a reference to the File Storage system.
	 */
	@Override
	public void init() throws ServletException {
		Object o = getServletContext().getAttribute(
				FileStorageSetup.ATTRIBUTE_NAME);
		if (o instanceof FileStorage) {
			fileStorage = (FileStorage) o;
		} else {
			throw new UnavailableException(
					"The ServletContext did not hold a FileStorage object at '"
							+ FileStorageSetup.ATTRIBUTE_NAME
							+ "'; found this instead: " + o);
		}
	}

	@Override
	protected void doGet(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
		VitroRequest request = new VitroRequest(rawRequest);

		// Use the alias URL to get the URI of the bytestream object.
		String path = request.getServletPath() + request.getPathInfo();
		log.debug("Path is '" + path + "'");

		FileInfo fileInfo = FileInfo.instanceFromAliasUrl(
				request.getFullWebappDaoFactory(), path, getServletContext());
		log.debug("File info is '" + fileInfo + "'");
		if (fileInfo == null) {
			String message = "The request path is not valid for the File servlet: '"
					+ path + "'";
			log.error(message);
			response.sendError(SC_INTERNAL_SERVER_ERROR, message);
			return;
		}

		// Validate that the file exists, with the requested URI and filename.
		String requestedFilename = getFilename(path);
		String actualFilename = fileInfo.getFilename();
		if (!actualFilename.equals(requestedFilename)
				&& !actualFilename.equals(decode(requestedFilename))) {
			log.warn("The requested filename does not match the "
					+ "actual filename; request: '" + path + "', actual: '"
					+ actualFilename + "'");
			response.sendError(SC_NOT_FOUND, ("File not found: " + path));
			return;
		}

		// Get the MIME type.
		String mimeType = fileInfo.getMimeType();

		// Open the actual byte stream.
		InputStream in;
		try {
			in = fileStorage.getInputStream(fileInfo.getBytestreamUri(),
					actualFilename);
		} catch (FileNotFoundException e) {
			log.error(e, e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}

		/*
		 * Everything is ready and working. Set the status and the content type,
		 * and send the image bytes.
		 */
		response.setStatus(SC_OK);

		if (mimeType != null) {
			response.setContentType(mimeType);
		}

		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			byte[] buffer = new byte[8192];
			int howMany;
			while (-1 != (howMany = in.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * The filename is the portion of the path after the last slash.
	 */
	private String getFilename(String path) {
		int slashHere = path.lastIndexOf('/');
		if (slashHere == -1) {
			return path;
		} else {
			return path.substring(slashHere + 1);
		}
	}

	/**
	 * The filename may have been encoded for URL transfer.
	 */
	private String decode(String filename) {
		try {
			return URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("How did this happen?", e);
			return filename;
		}
	}

	/**
	 * A POST request is treated the same as a GET request.
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
