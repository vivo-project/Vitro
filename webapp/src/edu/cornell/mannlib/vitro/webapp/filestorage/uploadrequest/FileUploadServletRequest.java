/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

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
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Wraps an HTTP request and parses it for file uploads, without losing the
 * request parameters.
 * </p>
 * <p>
 * The request will have an attribute named by {@link #FILE_ITEM_MAP}. Either
 * this attribute or the call to {@link #getFiles()} will produce a map that may
 * be empty but is never null. The keys to the map are the field names for the
 * file fields. Since a form may have multiple fields with the same name, each
 * field name maps to a list of items. If a user does not provide a file to be
 * uploaded in a given field, the length of the file will be 0.
 * </p>
 * <p>
 * If the uploaded file(s) would be larger than the <code>maxFileSize</code>,
 * {@link #parseRequest(HttpServletRequest, int)} does not throw an Exception.
 * Instead, it records the exception in a request attribute named by
 * {@link #FILE_UPLOAD_EXCEPTION}. This attribute can be accessed directly, or
 * indirectly via the methods {@link #hasFileUploadException()} and
 * {@link #getFileUploadException()}. If there is an exception, the file item
 * map (see above) will still be non-null, but it will be empty.
 * </p>
 * <p>
 * Most methods are declared here simply delegate to the wrapped request.
 * Methods that have to do with parameters, files, or parsing exceptions, are
 * handled differently for simple requests and multipart request, and are
 * implemented in the sub-classes.
 * </p>
 */
@SuppressWarnings("deprecation")
public abstract class FileUploadServletRequest extends HttpServletRequestWrapper  {
    
    private static final Log log = LogFactory
            .getLog(FileUploadServletRequest.class);
    
	public static final String FILE_ITEM_MAP = "file_item_map";
	public static final String FILE_UPLOAD_EXCEPTION = "file_upload_exception";

	private Map<String, List<String>> parameters;
    private Map<String, List<FileItem>> files;
    private FileUploadException fileUploadException;

    private static final String[] EMPTY_ARRAY = new String[0];
    
	// ----------------------------------------------------------------------
	// The factory method
	// ----------------------------------------------------------------------

	/**
	 * Wrap this {@link HttpServletRequest} in an appropriate wrapper class.
	 * set maxTempFileSize to 0 or -1 if streaming is desired.  Set it to > 0 if
	 * you want any parts uploaded to a temporary directory 
	 */
	public static FileUploadServletRequest parseRequest(		
	        HttpServletRequest request, int maxTempFileSize) throws IOException {
	    
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if (isMultipart) {
		    if( maxTempFileSize <= 0 ){
		        return new StreamingMultipartHttpServletRequest(request);
		    }else{
		        return new MultipartHttpServletRequest(request, maxTempFileSize);
		    }
		} else {
			return new SimpleHttpServletRequestWrapper(request);
		}
	}

	// ----------------------------------------------------------------------
	// The constructor and the delegate.
	// ----------------------------------------------------------------------

	private final HttpServletRequest delegate;

	public FileUploadServletRequest(HttpServletRequest delegate) {
	    super(delegate);
		this.delegate = delegate;
	}

	protected HttpServletRequest getDelegate() {
		return this.delegate;
	}

	// ----------------------------------------------------------------------
	// New functionality to be implemented by the subclasses.
	// ----------------------------------------------------------------------

	/** Was this a multipart HTTP request? */
	public abstract boolean isMultipart();
	
	protected void stashParametersInRequest(HttpServletRequest request, ServletFileUpload upload){
	    Map<String, List<String>> parameters = new HashMap<String, List<String>>();
	    Map<String, List<FileItem>> files = new HashMap<String, List<FileItem>>();      

	    parseQueryString(request.getQueryString(), parameters);

	    try {
	        List<FileItem> items = upload.parseRequest( request );

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
	    } catch (UnsupportedEncodingException e) {
	        log.error("could not convert to UTF-8",e);
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

	
	public FileUploadException getFileUploadException() {
	    return fileUploadException;
	}

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

	
    /**
     * {@inheritDoc}
     * <p>
     * There may be more than one file item with the given name. If the first
     * one is empty (size is zero), keep looking for a non-empty one.
     * </p>
     */    
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

    /**
     * Gets a map of parameter names to files.
     * This will should return null.
     */
    public Map<String, List<FileItem>> getFiles() {
        if( files == null )
            return Collections.emptyMap();
        else
            return files;
    }

}
