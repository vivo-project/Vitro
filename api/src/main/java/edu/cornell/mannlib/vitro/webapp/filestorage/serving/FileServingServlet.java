/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.serving;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;

/**
 * <p>
 * Handles a request to serve an uploaded file from the file storage system.
 * </p>
 * <p>
 * The path of the request should be the "alias URL" of the desired file. We
 * need to:
 * </p>
 * <ul>
 * <li>Use the alias URL to find the URI of the file bytestream object.</li>
 * <li>Find the file surrogate object to get the MIME type of the file, and
 * confirm the filename.</li>
 * <li>Set the MIME type on the output stream and serve the bytes.</li>
 * </ul>
 * <p>
 * If the request is superficially correct, but no such file can be found,
 * return a 404. If there is a break in the data structures within the model or
 * the file system, return a 500.
 * </p>
 */
public class FileServingServlet extends VitroHttpServlet {
	/** If we can't locate the requested image, use this one instead. */
	private static final String PATH_MISSING_LINK_IMAGE = "/images/missingLink.png";

	private static final Log log = LogFactory.getLog(FileServingServlet.class);

	private FileStorage fileStorage;

	/**
	 * Get a reference to the File Storage system.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		fileStorage = ApplicationUtils.instance().getFileStorage();
	}

	@Override
	protected void doGet(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
		VitroRequest request = new VitroRequest(rawRequest);

		// Use the alias URL to get the URI of the bytestream object.
		String path = request.getServletPath() + request.getPathInfo();
		log.debug("Path is '" + path + "'");

		/*
		 * Get the mime type and an InputStream from the file. If we can't, use
		 * the dummy image file instead.
		 */
		InputStream in;
		String mimeType = null;
		try {
			FileInfo fileInfo = figureFileInfo(request.getWebappDaoFactory(),
					path);
			mimeType = fileInfo.getMimeType();

			String actualFilename = findAndValidateFilename(fileInfo, path);

			in = openImageInputStream(fileInfo, actualFilename);
		} catch (FileServingException e) {
			log.info("Failed to serve the file at '" + path + "' -- " + e);
			in = openMissingLinkImage(request);
			mimeType = "image/png";
		} catch (Exception e) {
			log.warn("Failed to serve the file at '" + path + "' -- " + e);
			in = openMissingLinkImage(request);
			mimeType = "image/png";
		}

		/*
		 * Everything is ready. Set the status and the content type, and send
		 * the image bytes.
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
		} catch (IOException e) {
			log.warn("Failed to serve the file", e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				log.warn("Serving " + request.getRequestURI()
						+ ". Failed to close input stream.", e);
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					log.warn("Serving " + request.getRequestURI()
							+ ". Failed to close output stream.", e);
				}
			}
		}
	}

	private FileInfo figureFileInfo(WebappDaoFactory fullWadf, String path)
			throws FileServingException {
		FileInfo fileInfo = FileInfo.instanceFromAliasUrl(fullWadf, path,
				getServletContext());
		if (fileInfo == null) {
			throw new FileServingException("The request path is not valid "
					+ "for the File servlet: '" + path + "'");
		}
		log.debug("File info is '" + fileInfo + "'");
		return fileInfo;
	}

	/** Validate that the file exists, with the requested URI and filename. */
	private String findAndValidateFilename(FileInfo fileInfo, String path)
			throws FileServingException {
		String requestedFilename = figureFilename(path);
		String actualFilename = fileInfo.getFilename();
		if (!actualFilename.equals(requestedFilename)
				&& !actualFilename.equals(decode(requestedFilename))) {
			throw new FileServingException(
					"The requested filename does not match the "
							+ "actual filename; request: '" + path
							+ "', actual: '" + actualFilename + "'");
		}
		log.debug("Actual filename is '" + actualFilename + "'");
		return actualFilename;
	}

	/** The filename is the portion of the path after the last slash. */
	private String figureFilename(String path) {
		int slashHere = path.lastIndexOf('/');
		if (slashHere == -1) {
			return path;
		} else {
			return path.substring(slashHere + 1);
		}
	}

	/** The filename may have been encoded for URL transfer. */
	private String decode(String filename) {
		try {
			return URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("No UTF-8 decoder? How did this happen?", e);
			return filename;
		}
	}

	private InputStream openImageInputStream(FileInfo fileInfo,
			String actualFilename) throws IOException {
		return fileStorage.getInputStream(fileInfo.getBytestreamUri(),
				actualFilename);
	}

	/** Any suprises when opening the image? Use this one instead. */
	private InputStream openMissingLinkImage(VitroRequest vreq)
			throws FileNotFoundException {
		InputStream stream = vreq.getSession().getServletContext()
				.getResourceAsStream(PATH_MISSING_LINK_IMAGE);
		if (stream == null) {
			throw new FileNotFoundException("No image file at '"
					+ PATH_MISSING_LINK_IMAGE + "'");
		}
		return stream;
	}

	/**
	 * A POST request is treated the same as a GET request.
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * There was a problem serving the file bytestream.
	 */
	private static class FileServingException extends Exception {
		public FileServingException(String message) {
			super(message);
		}
	}

}
