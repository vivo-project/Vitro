package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
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
    public StreamingMultipartHttpServletRequest(HttpServletRequest request)
        throws IOException{
        super(request);        
        
        //use a file uploader that does not save the files to a temporary directory.
        stashParametersInRequest( request , new ServletFileUpload());
    }

    @Override
    public boolean isMultipart() { 
        return true;
    }


}
