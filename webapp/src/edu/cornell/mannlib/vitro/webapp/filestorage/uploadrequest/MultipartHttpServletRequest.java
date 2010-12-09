/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper for a servlet request that holds multipart content. Parsing the
 * request will consume the parameters, so we need to hold them here to answer
 * any parameter-related requests. File-related information will also be held
 * here, to answer file-related requests.
 */
class MultipartHttpServletRequest extends FileUploadServletRequest {
	private static final Log log = LogFactory
			.getLog(MultipartHttpServletRequest.class);

	private static final String[] EMPTY_ARRAY = new String[0];

	private final Map<String, List<String>> parameters;
	private final Map<String, List<FileItem>> files;
	private FileUploadException fileUploadException;

	/**
	 * Parse the multipart request. Store the info about the request parameters
	 * and the uploaded files.
	 */
	public MultipartHttpServletRequest(HttpServletRequest request,
			int maxFileSize) throws IOException {
		super(request);

		Map<String, List<String>> parameters = new HashMap<String, List<String>>();
		Map<String, List<FileItem>> files = new HashMap<String, List<FileItem>>();

		File tempDir = figureTemporaryDirectory(request);
		ServletFileUpload upload = createUploadHandler(maxFileSize, tempDir);

		parseQueryString(request.getQueryString(), parameters);

		try {
			List<FileItem> items = parseRequestIntoFileItems(request, upload);

			for (FileItem item : items) {
				// Process a regular form field
				if (item.isFormField()) {
					addToParameters(parameters, item.getFieldName(), item
							.getString("UTF-8"));
					log.debug("Form field (parameter) " + item.getFieldName()
							+ "=" + item.getString());
				} else {
					addToFileItems(files, item);
					log.debug("File " + item.getFieldName() + ": "
							+ item.getName());
				}
			}
		} catch (FileUploadException e) {
			fileUploadException = e;
			request.setAttribute(
					FileUploadServletRequest.FILE_UPLOAD_EXCEPTION, e);
		}

		this.parameters = Collections.unmodifiableMap(parameters);
		log.debug("Parameters are: " + this.parameters);
		this.files = Collections.unmodifiableMap(files);
		log.debug("Files are: " + this.files);
		request.setAttribute(FILE_ITEM_MAP, this.files);
	}

	/**
	 * Pull any parameters out of the URL.
	 */
	private void parseQueryString(String queryString,
			Map<String, List<String>> parameters) {
		log.debug("Query string is : '" + queryString + "'");
		if (queryString != null) {
			String[] pieces = queryString.split("&");

			for (String piece : pieces) {
				int equalsHere = piece.indexOf('=');
				if (piece.trim().isEmpty()) {
					// Ignore an empty piece.
				} else if (equalsHere <= 0) {
					// A parameter without a value.
					addToParameters(parameters, decode(piece), "");
				} else {
					// A parameter with a value.
					String key = piece.substring(0, equalsHere);
					String value = piece.substring(equalsHere + 1);
					addToParameters(parameters, decode(key), decode(value));
				}
			}
		}
		log.debug("Parameters from query string are: " + parameters);
	}

	/**
	 * Remove any special URL-style encoding.
	 */
	private String decode(String encoded) {
		try {
			return URLDecoder.decode(encoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e, e);
			return encoded;
		}
	}

	/**
	 * Find the temporary storage directory for this webapp.
	 */
	private File figureTemporaryDirectory(HttpServletRequest request) {
		return (File) request.getSession().getServletContext().getAttribute(
				"javax.servlet.context.tempdir");
	}

	/**
	 * Create an upload handler that will throw an exception if the file is too
	 * large.
	 */
	private ServletFileUpload createUploadHandler(int maxFileSize, File tempDir) {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
		factory.setRepository(tempDir);

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxFileSize);

		return upload;
	}

	/** Either create a new List for the value, or add to an existing List. */
	private void addToParameters(Map<String, List<String>> map, String name,
			String value) {
		if (!map.containsKey(name)) {
			map.put(name, new ArrayList<String>());
		}
		map.get(name).add(value);
	}

	/** Either create a new List for the file, or add to an existing List. */
	private void addToFileItems(Map<String, List<FileItem>> map, FileItem file) {
		String name = file.getFieldName();
		if (!map.containsKey(name)) {
			map.put(name, new ArrayList<FileItem>());
		}
		map.get(name).add(file);
	}

	/** Minimize the code that uses the unchecked cast. */
	@SuppressWarnings("unchecked")
	private List<FileItem> parseRequestIntoFileItems(HttpServletRequest req,
			ServletFileUpload upload) throws FileUploadException {
		return upload.parseRequest(req);
	}

	// ----------------------------------------------------------------------
	// This is a multipart request, so make the file info available. If there
	// was an exception during parsing, make that available too.
	// ----------------------------------------------------------------------

	@Override
	public boolean isMultipart() {
		return true;
	}

	@Override
	public Map<String, List<FileItem>> getFiles() {
		return files;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * There may be more than one file item with the given name. If the first
	 * one is empty (size is zero), keep looking for a non-empty one.
	 * </p>
	 */
	@Override
	public FileItem getFileItem(String name) {
		List<FileItem> items = files.get(name);
		if (items == null) {
			return null;
		}

		for (FileItem item : items) {
			if (item.getSize() > 0L) {
				return item;
			}
		}

		return null;
	}

	@Override
	public FileUploadException getFileUploadException() {
		return fileUploadException;
	}

	@Override
	public boolean hasFileUploadException() {
		return fileUploadException != null;
	}

	// ----------------------------------------------------------------------
	// Parameter-related methods won't find anything on the delegate request,
	// since parsing consumed the parameters. So we need to look to the parsed
	// info for the answers.
	// ----------------------------------------------------------------------

	@Override
	public String getParameter(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).get(0);
		} else {
			return null;
		}
	}

	@Override
	public Enumeration<?> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).toArray(EMPTY_ARRAY);
		} else {
			return null;
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		for (Entry<String, List<String>> entry : parameters.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toArray(EMPTY_ARRAY));
		}
		log.debug("resulting parameter map: " + result);
		return result;
	}

}
