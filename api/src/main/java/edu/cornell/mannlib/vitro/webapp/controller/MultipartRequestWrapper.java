/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps a multipart HTTP request, and pre-parses it for file uploads, without
 * losing the request parameters.
 * 
 * Parsing through the request with an Apache ServletFileUpload builds a list of
 * FileItems that includes the parameters and the file parts. After that,
 * however, the parameters are no longer accessible from the request. This
 * wrapper will see that they don't get lost.
 * 
 * The List of FileItems includes both "formField" items and "file" items. As
 * with the usual parameters on a request, we can have more than one value with
 * the same name. So this creates a map of &lt;String, List&lt;String&gt;&gt; to hold the
 * parameters, and a map of &lt;String, List&lt;FileItem&gt;&gt; to hold the files.
 * 
 * The parameters will be available to the wrapper through the normal methods.
 * The files will be available as an attribute that holds the map. Also, a
 * separate attribute will hold a Boolean to indicate that this was indeed a
 * multipart request.
 * 
 * Conveninence methods in VitroRequest will make these easy to handle, without
 * actually touching the attributes.
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {
	private static final Log log = LogFactory
			.getLog(MultipartRequestWrapper.class);

	private static final String CLASS_NAME = MultipartRequestWrapper.class
			.getSimpleName();
	public static final String ATTRIBUTE_IS_MULTIPART = CLASS_NAME
			+ "_isMultipart";
	public static final String ATTRIBUTE_FILE_ITEM_MAP = CLASS_NAME
			+ "_fileItemMap";
	public static final String ATTRIBUTE_FILE_SIZE_EXCEPTION = CLASS_NAME
			+ "_fileSizeException";

	private static final String[] EMPTY_ARRAY = new String[0];

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	/**
	 * If this is a multipart request, wrap it. Otherwise, just return the
	 * request as it is.
	 */
	public static HttpServletRequest parse(HttpServletRequest req,
			ParsingStrategy strategy) throws IOException {
		if (!ServletFileUpload.isMultipartContent(req)) {
			return req;
		}

		ListsMap<String> parameters = new ListsMap<>();
		ListsMap<FileItem> files = new ListsMap<>();

		parseQueryString(req.getQueryString(), parameters);
		parseFileParts(req, parameters, files, strategy);

		return new MultipartRequestWrapper(req, parameters, files);
	}

	/**
	 * Pull any parameters out of the URL.
	 */
	private static void parseQueryString(String queryString,
			ListsMap<String> parameters) {
		log.debug("Query string is : '" + queryString + "'");
		if (queryString != null) {
			String[] pieces = queryString.split("&");

			for (String piece : pieces) {
				int equalsHere = piece.indexOf('=');
				if (piece.trim().isEmpty()) {
					// Ignore an empty piece.
				} else if (equalsHere <= 0) {
					// A parameter without a value.
					parameters.add(decode(piece), "");
				} else {
					// A parameter with a value.
					String key = piece.substring(0, equalsHere);
					String value = piece.substring(equalsHere + 1);
					parameters.add(decode(key), decode(value));
				}
			}
		}
		log.debug("Parameters from query string are: " + parameters);
	}

	/**
	 * Remove any special URL-style encoding.
	 */
	private static String decode(String encoded) {
		try {
			return URLDecoder.decode(encoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e, e);
			return encoded;
		}
	}

	private static void parseFileParts(HttpServletRequest req,
			ListsMap<String> parameters, ListsMap<FileItem> files,
			ParsingStrategy strategy) throws IOException {

		ServletFileUpload upload = createUploadHandler(req,
				strategy.maximumMultipartFileSize());
		List<FileItem> items = parseRequestIntoFileItems(req, upload, strategy);

		for (FileItem item : items) {
			// Process a regular form field
			String name = item.getFieldName();
			if (item.isFormField()) {
				String value;
				try {
					value = item.getString("UTF-8");
				} catch (UnsupportedEncodingException e) {
					value = item.getString();
				}
				parameters.add(name, value);
				log.debug("Form field (parameter) " + name + "=" + value);
			} else {
				files.add(name, item);
				log.debug("File " + name + ": " + item.getSize() + " bytes.");
			}
		}
	}

	/**
	 * Create an upload handler that will throw an exception if the file is too
	 * large.
	 */
	private static ServletFileUpload createUploadHandler(
			HttpServletRequest req, long maxFileSize) {
		File tempDir = figureTemporaryDirectory(req);

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
		factory.setRepository(tempDir);

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxFileSize);
		return upload;
	}

	/**
	 * Find the temporary storage directory for this webapp.
	 */
	private static File figureTemporaryDirectory(HttpServletRequest request) {
		return (File) request.getSession().getServletContext()
				.getAttribute("javax.servlet.context.tempdir");
	}

	/**
	 * Parse the raw request into a list of parts.
	 * 
	 * If there is a parsing error, let the strategy handle it. If the strategy
	 * throws it back, wrap it in an IOException and throw it on up.
	 */
	@SuppressWarnings("unchecked")
	private static List<FileItem> parseRequestIntoFileItems(
			HttpServletRequest req, ServletFileUpload upload,
			ParsingStrategy strategy) throws IOException {
		try {
			return upload.parseRequest(req);
		} catch (FileSizeLimitExceededException | SizeLimitExceededException e) {
			if (strategy.stashFileSizeException()) {
				req.setAttribute(ATTRIBUTE_FILE_SIZE_EXCEPTION, e);
				return Collections.emptyList();
			} else {
				throw new IOException(e);
			}
		} catch (FileUploadException e) {
			throw new IOException(e);
		}
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final ListsMap<String> parameters;

	public MultipartRequestWrapper(HttpServletRequest req,
			ListsMap<String> parameters, ListsMap<FileItem> files) {
		super(req);

		this.parameters = parameters;

		req.setAttribute(ATTRIBUTE_IS_MULTIPART, Boolean.TRUE);
		req.setAttribute(ATTRIBUTE_FILE_ITEM_MAP, files);
	}

	/**
	 * Look in the map of parsed parameters.
	 */
	@Override
	public String getParameter(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).get(0);
		} else {
			return null;
		}
	}

	/**
	 * Look in the map of parsed parameters. Make a protective copy.
	 */
	@Override
	public Enumeration<?> getParameterNames() {
		return Collections.enumeration(new HashSet<>(parameters.keySet()));
	}

	/**
	 * Look in the map of parsed parameters.
	 */
	@Override
	public String[] getParameterValues(String name) {
		if (parameters.containsKey(name)) {
			return parameters.get(name).toArray(EMPTY_ARRAY);
		} else {
			return null;
		}
	}

	/**
	 * Make a copy of the map of parsed parameters;
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		for (String key : parameters.keySet()) {
			result.put(key, parameters.get(key).toArray(EMPTY_ARRAY));
		}
		return result;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class ListsMap<T> extends HashMap<String, List<T>> {
		void add(String key, T value) {
			if (!containsKey(key)) {
				put(key, new ArrayList<T>());
			}
			get(key).add(value);
		}
	}

	public interface ParsingStrategy {
		long maximumMultipartFileSize();

		/**
		 * Allows you to handle the exception in your code.
		 * 
		 * Be aware that the multipart parameters have been lost, and that may
		 * include form fields.
		 */
		boolean stashFileSizeException();
	}

}
