/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;


import static edu.cornell.mannlib.vitro.webapp.controller.MultipartRequestWrapper.ATTRIBUTE_FILE_ITEM_MAP;
import static edu.cornell.mannlib.vitro.webapp.controller.MultipartRequestWrapper.ATTRIBUTE_FILE_SIZE_EXCEPTION;
import static edu.cornell.mannlib.vitro.webapp.controller.MultipartRequestWrapper.ATTRIBUTE_IS_MULTIPART;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource.ModelName;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class VitroRequest extends HttpServletRequestWrapper {
    
    final static Log log = LogFactory.getLog(VitroRequest.class);
    
    //Attribute in case of special model editing such as display model editing
    public static final String SPECIAL_WRITE_MODEL = "specialWriteModel";     

    public  static final String ID_FOR_WRITE_MODEL = "idForWriteModel";
    public  static final String ID_FOR_TBOX_MODEL = "idForTboxModel";
    public  static final String ID_FOR_ABOX_MODEL = "idForAboxModel";
    public static final String ID_FOR_DISPLAY_MODEL = "idForDisplayModel";
    
    private HttpServletRequest _req;

    public VitroRequest(HttpServletRequest _req) {
        super(_req);
        this._req = _req;
    }

    public RDFService getRDFService() {
    	return ModelAccess.on(this).getRDFService();
    }
    
    public RDFService getUnfilteredRDFService() {
    	return ModelAccess.on(this).getRDFService(CONTENT, LANGUAGE_NEUTRAL);
    }
    
    /** Gets WebappDaoFactory with appropriate filtering for the request */
    public WebappDaoFactory getWebappDaoFactory(){
    	return ModelAccess.on(this).getWebappDaoFactory();
    }
    
    /** gets assertions+inference WebappDaoFactory with no policy filtering */
    public WebappDaoFactory getUnfilteredWebappDaoFactory() {
    	return ModelAccess.on(this).getWebappDaoFactory(POLICY_NEUTRAL);
    }
    
    /** gets assertions-only WebappDaoFactory with no policy filtering */
    public WebappDaoFactory getUnfilteredAssertionsWebappDaoFactory() {
    	return ModelAccess.on(this).getWebappDaoFactory(POLICY_NEUTRAL, ASSERTIONS_ONLY);
    }
    
    public Dataset getDataset() {
    	return ModelAccess.on(this).getDataset(CONTENT);
    }
    
    public Dataset getUnfilteredDataset() {
    	return ModelAccess.on(this).getDataset(CONTENT, LANGUAGE_NEUTRAL);
    }
    
    //Method that retrieves write model, returns special model in case of write model
    public OntModel getWriteModel() {
    	//if special write model doesn't exist use get ont model 
    	if(this.getAttribute(SPECIAL_WRITE_MODEL) != null) {
    		return (OntModel)this.getAttribute(SPECIAL_WRITE_MODEL);
    	} else {
    		return getJenaOntModel();
    	}
    }
    
    public OntModelSelector getOntModelSelector() {
    	return ModelAccess.on(this).getOntModelSelector();
    }
    
    public OntModel getJenaOntModel() {
    	return ModelAccess.on(this).getOntModel(ModelNames.FULL_UNION);
    }
    
    public OntModel getDisplayModel(){
    	return ModelAccess.on(this).getOntModel(DISPLAY);
    }
        
    /**
     * Gets an identifier for the display model associated 
     * with this request.  It may have been switched from
     * the normal display model to a different one.
     * This could be a URI or a {@link ModelName}
     */
    public String getIdForDisplayModel(){
        return (String)getAttribute(ID_FOR_DISPLAY_MODEL);        
    }
    
    /**
     * Gets an identifier for the a-box model associated 
     * with this request.  It may have been switched from
     * the standard one to a different one.
     * This could be a URI or a {@link ModelName}
     */    
    public String  getNameForABOXModel(){
        return (String)getAttribute(ID_FOR_ABOX_MODEL);        
    }
    
    /**
     * Gets an identifier for the t-box model associated 
     * with this request.  It may have been switched from
     * the standard one to a different one.
     * This could be a URI or a {@link ModelName}
     */    
    public String  getNameForTBOXModel(){
        return (String)getAttribute(ID_FOR_TBOX_MODEL);        
    }
    
    /**
     * Gets an identifier for the write model associated 
     * with this request.  It may have been switched from
     * the standard one to a different one.
     * This could be a URI or a {@link ModelName}
     */    
    public String  getNameForWriteModel(){
        return (String)getAttribute(ID_FOR_WRITE_MODEL);        
    }
    
    public ApplicationBean getAppBean(){
    	return getWebappDaoFactory().getApplicationDao().getApplicationBean();
    }

    /**
     * Gets the the ip of the client.
     * This will be X-forwarded-for header or, if that header is not
     * set, getRemoteAddr(). This still may not be the client's address
     * as they may be using a proxy. 
     * 
     */
    public String getClientAddr(){
        String xff = getHeader("x-forwarded-for");
        return ( xff == null || xff.trim().isEmpty() ) ? getRemoteAddr() : xff;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Map<String, String[]> getParameterMap() {        
        return _req.getParameterMap();        
    }
    
    @Override
    public String getParameter(String name) {        
        return _req.getParameter(name);        
    }

    @Override
    public String[] getParameterValues(String name) {
        return _req.getParameterValues(name);        
    }

	public OntModel getLanguageNeutralUnionFullModel() {
		return ModelAccess.on(this).getOntModel(ModelNames.FULL_UNION, LANGUAGE_NEUTRAL);
	}           
	
	public void setCollator(Collator collator) {
	    setAttribute("collator", collator);
	}
	
	public Collator getCollator() {
	    return (Collator) getAttribute("collator");
	}
	
    public WebappDaoFactory getLanguageNeutralWebappDaoFactory() {
		// It is also policy neutral, because that's how it was originally
		// implemented, and at least some of the client code expects it that
		// way.
    	return ModelAccess.on(this).getWebappDaoFactory(LANGUAGE_NEUTRAL, POLICY_NEUTRAL);
    }
    
    // ----------------------------------------------------------------------
	// Deal with parsed multipart requests.
	// ----------------------------------------------------------------------
    
	public boolean isMultipart() {
		return getAttribute(ATTRIBUTE_IS_MULTIPART) != null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<FileItem>> getFiles() {
		Map<String, List<FileItem>> map;
		map = (Map<String, List<FileItem>>) getAttribute(ATTRIBUTE_FILE_ITEM_MAP);
		if (map == null) {
			return Collections.emptyMap();
		} else {
			return map;
		}
	}

	/**
	 * There may be more than one file item with the given name. If the first
	 * one is empty (size is zero), keep looking for a non-empty one.
	 */
	public FileItem getFileItem(String name) {
		Map<String, List<FileItem>> map = getFiles();
		List<FileItem> items = map.get(name);
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
	 * If the uploaded file exceeded the maximum size, and if the strategy said
	 * to stash the exception, it will be stored as a request attribute.
	 */
	public boolean hasFileSizeException() {
		return getFileSizeException() != null;
	}
	
	/**
	 * Could be either FileSizeLimitExceededException or
	 * SizeLimitExceededException, so return their common ancestor.
	 */
	public FileUploadException getFileSizeException() {
		Object e = getAttribute(ATTRIBUTE_FILE_SIZE_EXCEPTION);
		if (e instanceof FileUploadException) {
			return (FileUploadException) e;
		} else {
			return null;
		}
	}

}
