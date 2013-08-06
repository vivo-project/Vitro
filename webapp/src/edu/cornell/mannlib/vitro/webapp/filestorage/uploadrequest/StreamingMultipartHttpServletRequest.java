/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Wrapping ServletRequest that does multipart.  In order to allow
 * streaming, this class does NOT save the parts to a temporary directory. 
 * 
 *
 */
public class StreamingMultipartHttpServletRequest extends
        FileUploadServletRequest {    
    /** 
     * Parse the multipart request. Store the info about the request parameters.
     * Don't store the uploaded files to a temporary directory to allow streaming.
     *
     * Only use this if you plan to consume the FileItems using streaming
     * to deal with inputs of very large sizes.
     * 
     */
    public StreamingMultipartHttpServletRequest(HttpServletRequest request) {
        super(request);        
        
        //use a file uploader that does not save the files to a temporary directory.
        stashParametersInRequest( request , new ServletFileUpload());
    }

    @Override
    public boolean isMultipart() { 
        return true;
    }


}
