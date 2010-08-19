/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

/**
 * A wrapper for a servlet request that does not hold multipart content. Pass
 * all parameter-related requests to the delegate, and give simple answers to
 * all file-related requests.
 */
class SimpleHttpServletRequestWrapper extends FileUploadServletRequest {

	SimpleHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		request.setAttribute(FILE_ITEM_MAP, Collections.EMPTY_MAP);
	}

	// ----------------------------------------------------------------------
	// Not a multipart request, so there are no files or upload exceptions.
	// ----------------------------------------------------------------------

	@Override
	public boolean isMultipart() {
		return false;
	}

	@Override
	public Map<String, List<FileItem>> getFiles() {
		return Collections.emptyMap();
	}

	@Override
	public FileItem getFileItem(String string) {
		return null;
	}

	@Override
	public FileUploadException getFileUploadException() {
		return null;
	}

	@Override
	public boolean hasFileUploadException() {
		return false;
	}

	// ----------------------------------------------------------------------
	// Since this is not a multipart request, the parameter methods can be
	// delegated.
	// ----------------------------------------------------------------------

	@Override
	public String getParameter(String name) {
		return getDelegate().getParameter(name);
	}

	@Override
	public Map<?, ?> getParameterMap() {
		return getDelegate().getParameterMap();
	}

	@Override
	public Enumeration<?> getParameterNames() {
		return getDelegate().getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return getDelegate().getParameterValues(name);
	}

}
