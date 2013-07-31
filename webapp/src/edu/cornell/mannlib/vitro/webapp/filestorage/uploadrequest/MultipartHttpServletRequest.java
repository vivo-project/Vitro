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
public class MultipartHttpServletRequest extends FileUploadServletRequest {
	private static final Log log = LogFactory
			.getLog(MultipartHttpServletRequest.class);

    private int maxFileSize = 0;
    private File tempDir = null; 
            
	/**
	 * Parse the multipart request. Store the info about the request parameters
	 * and the uploaded files to a temporary directory.
     *
     * This offers a simple way to deal with uploaded files by having a size
     * limit.  This limit may be rather large if desired. Many megabytes for example.
	 */
	public MultipartHttpServletRequest(HttpServletRequest request,
			int maxFileSize) throws IOException {
		super(request);
		
        this.maxFileSize = maxFileSize;
        this.tempDir = figureTemporaryDirectory(request);

        //use an upload handler that will stash the file items in a temporary directory.
        ServletFileUpload upload = createUploadHandler( this.maxFileSize, this.tempDir );
        stashParametersInRequest( request , upload );
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

    @Override
    public boolean isMultipart() {
        return true;
    }

	
}
