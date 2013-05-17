/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource.ModelName;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

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
        Object o = getAttribute("rdfService");
        if (o instanceof RDFService) {
            return (RDFService) o;
        } else {
            RDFService rdfService = RDFServiceUtils.getRDFService(this);
            setAttribute("rdfService", rdfService);
            return rdfService;
        }
    }
    
    public RDFService getUnfilteredRDFService() {
        Object o = getAttribute("unfilteredRDFService");
        if (o instanceof RDFService) {
            return (RDFService) o;
        } else {
            RDFService rdfService = RDFServiceUtils.getRDFService(this);
            setAttribute("unfilteredRDFService", rdfService);
            return rdfService;
        }
    }
    
    public void setRDFService(RDFService rdfService) {
        setAttribute("rdfService", rdfService);
    }
    
    public void setUnfilteredRDFService(RDFService rdfService) {
        setAttribute("unfilteredRDFService", rdfService);
    }
    
    public void setWebappDaoFactory( WebappDaoFactory wdf){
        setAttribute("webappDaoFactory",wdf);
    }
    
    /** gets WebappDaoFactory with appropriate filtering for the request */
    public WebappDaoFactory getWebappDaoFactory(){
    	return (WebappDaoFactory) getAttribute("webappDaoFactory");
    }
    
    public void setUnfilteredWebappDaoFactory(WebappDaoFactory wdf) {
    	setAttribute("unfilteredWebappDaoFactory", wdf);
    }
    
    /** Gets a WebappDaoFactory with request-specific dataset but no filtering. 
     * Use this for any servlets that need to bypass filtering.
     * @return
     */
    public WebappDaoFactory getUnfilteredWebappDaoFactory() {
    	return (WebappDaoFactory) getAttribute("unfilteredWebappDaoFactory");
    }
    
    public void setFullWebappDaoFactory(WebappDaoFactory wdf) {
    	setAttribute("fullWebappDaoFactory", wdf);
    }
    
    public Dataset getDataset() {
    	return (Dataset) getAttribute("dataset");
    }
    
    public void setDataset(Dataset dataset) {
    	setAttribute("dataset", dataset);
    }
    
    /** gets assertions + inferences WebappDaoFactory with no filtering **/
    public WebappDaoFactory getFullWebappDaoFactory() {
    	Object webappDaoFactoryAttr = _req.getAttribute("fullWebappDaoFactory");
    	if (webappDaoFactoryAttr instanceof WebappDaoFactory) {
    		return (WebappDaoFactory) webappDaoFactoryAttr;
    	} else {
	        webappDaoFactoryAttr = _req.getSession().getAttribute("webappDaoFactory");
	        if (webappDaoFactoryAttr instanceof WebappDaoFactory) {
	             return (WebappDaoFactory) webappDaoFactoryAttr;
	        } else {
	        	return (WebappDaoFactory) _req.getSession().getServletContext().getAttribute("webappDaoFactory");	
	        }
    	}
    }
    
    /** gets assertions-only WebappDaoFactory with no filtering */
    public WebappDaoFactory getAssertionsWebappDaoFactory() {
    	Object webappDaoFactoryAttr = _req.getSession().getAttribute("assertionsWebappDaoFactory");
        if (webappDaoFactoryAttr instanceof WebappDaoFactory) {
             log.debug("Returning assertionsWebappDaoFactory from session");
             return (WebappDaoFactory) webappDaoFactoryAttr;
        } else {
            webappDaoFactoryAttr = getAttribute("assertionsWebappDaoFactory");
            if (webappDaoFactoryAttr instanceof WebappDaoFactory) {
                log.debug("returning assertionsWebappDaoFactory from request attribute");
                return (WebappDaoFactory) webappDaoFactoryAttr;     
            } else {
                log.debug("Returning assertionsWebappDaoFactory from context");
                return (WebappDaoFactory) _req.getSession().getServletContext().getAttribute("assertionsWebappDaoFactory");
            }
        		
        }
    }
    
    /** gets assertions-only WebappDaoFactory with no filtering */
    public void setAssertionsWebappDaoFactory(WebappDaoFactory wadf) {
        setAttribute("assertionsWebappDaoFactory", wadf); 
    }
    
    /** gets inferences-only WebappDaoFactory with no filtering */
    public WebappDaoFactory getDeductionsWebappDaoFactory() {
    	Object webappDaoFactoryAttr = _req.getSession().getAttribute("deductionsWebappDaoFactory");
        if (webappDaoFactoryAttr instanceof WebappDaoFactory) {
             return (WebappDaoFactory) webappDaoFactoryAttr;
        } else {
        	return (WebappDaoFactory) _req.getSession().getServletContext().getAttribute("deductionsWebappDaoFactory");	
        }
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
    
    public void setJenaOntModel(OntModel ontModel) {
    	ModelAccess.on(this).setJenaOntModel(ontModel);
    }
    
    public OntModel getJenaOntModel() {
    	return ModelAccess.on(this).getJenaOntModel();
    }
    
    public OntModel getAssertionsOntModel() {
        return ModelAccess.on(this).getBaseOntModel();
    }
    
    public OntModel getInferenceOntModel() {
    	return ModelAccess.on(this).getInferenceOntModel();
    }

    public OntModel getDisplayModel(){
    	return ModelAccess.on(this).getDisplayModel();
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
            
    
}
