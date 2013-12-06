/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import static edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest.FILE_ITEM_MAP;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Class that performs the update of the uris in the search index for the
 * SearchService.
 */

public class UpdateUrisInIndex {
	private static final Log log = LogFactory.getLog(UpdateUrisInIndex.class);

	/** Pattern to split URIs on whitespace and commas. */
	public static final Pattern DELIMITER = Pattern.compile("[,\\s]+");

	/**
	 * Web service for update in search index of a list of URIs.
	 * 
	 * @throws IOException
	 */
	protected int doUpdateUris(HttpServletRequest req, IndexBuilder builder)
			throws ServletException, IOException {
		@SuppressWarnings("unchecked")
		Map<String, List<FileItem>> map = (Map<String, List<FileItem>>) req
				.getAttribute(FILE_ITEM_MAP);
		if (map == null) {
			throw new ServletException("Expected Multipart Content");
		}
		
		Charset enc = getEncoding(req);

		int uriCount = 0;
		for (String name : map.keySet()) {
			for (FileItem item : map.get(name)) {
				log.debug("Found " + item.getSize() + " byte file for '" + name + "'");
				uriCount += processFileItem(builder, name, item, enc);
			}
		}
		return uriCount;
	}

	private int processFileItem(IndexBuilder builder, String name,
			FileItem item, Charset enc) throws IOException {
		int count = 0;
		Reader reader = new InputStreamReader(item.getInputStream(), enc.name());
		try (Scanner scanner = createScanner(reader)) {
			while (scanner.hasNext()) {
				String uri = scanner.next();
				log.debug("Request to index uri '" + uri + "'");
				builder.addToChanged(uri);
				count++;
			}
		}
		return count;
	}

	@SuppressWarnings("resource")
	protected Scanner createScanner(Reader in) {
		return new Scanner(in).useDelimiter(DELIMITER);
	}

	/**
	 * Get the encoding of the request, default to UTF-8 since that is in the
	 * vitro install instructions to put on the connector.
	 */
	private Charset getEncoding(HttpServletRequest req) {
		String enc = req.getCharacterEncoding();
		if (StringUtils.isBlank(enc)) {
			log.debug("No encoding on POST request, That is acceptable.");
			enc = "UTF-8";
		} else if (enc.length() > 30) {
			log.debug("Ignoring odd encoding of '" + enc + "'");
			enc = "UTF-8";
		} else {
			log.debug("Encoding set on POST request: " + enc);
		}
		log.debug("Reading POSTed URIs with encoding " + enc);
		return Charset.forName(enc);
	}

}
