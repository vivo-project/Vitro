/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;

public class VitroRequest extends HttpServletRequestWrapper {

    private static final String FROM_ENCODING = "ISO-8859-1";
    private static final String TO_ENCODING = "UTF-8";
    public static boolean convertParameterEncoding = true;
    //Attribute in case of special model editing such as display model editing
    public static final String SPECIAL_WRITE_MODEL = "specialWriteModel";
    
    public static boolean getConvertParameterEncoding() {
        return convertParameterEncoding;
    }

    public static void setConvertParameterEncoding(boolean cpe) {
        convertParameterEncoding = cpe;
    }

    private Map<String,String[]> convertedParameterMap;

    private HttpServletRequest _req;

    public VitroRequest(HttpServletRequest _req) {
        super(_req);
        this._req = _req;
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
    
    public void setJenaOntModel(OntModel ontModel) {
    	setAttribute("jenaOntModel", ontModel);
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
             return (WebappDaoFactory) webappDaoFactoryAttr;
        } else {
        	return (WebappDaoFactory) _req.getSession().getServletContext().getAttribute("assertionsWebappDaoFactory");	
        }
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
    
    
    
    public OntModel getJenaOntModel() {
    	Object ontModel = getAttribute("jenaOntModel");
    	if (ontModel instanceof OntModel) {
    		return (OntModel) ontModel;
    	}
    	OntModel jenaOntModel = (OntModel)_req.getSession().getAttribute( JenaBaseDao.JENA_ONT_MODEL_ATTRIBUTE_NAME );
    	if ( jenaOntModel == null ) {
    		jenaOntModel = (OntModel)_req.getSession().getServletContext().getAttribute( JenaBaseDao.JENA_ONT_MODEL_ATTRIBUTE_NAME );
    	}
    	return jenaOntModel;
    }
    
    public OntModel getAssertionsOntModel() {
    	OntModel jenaOntModel = (OntModel)_req.getSession().getAttribute( JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME );
    	if ( jenaOntModel == null ) {
    		jenaOntModel = (OntModel)_req.getSession().getServletContext().getAttribute( JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME );
    	}
    	return jenaOntModel;    	
    }
    
    public OntModel getInferenceOntModel() {
    	OntModel jenaOntModel = (OntModel)_req.getSession().getAttribute( JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME );
    	if ( jenaOntModel == null ) {
    		jenaOntModel = (OntModel)_req.getSession().getServletContext().getAttribute( JenaBaseDao.INFERENCE_ONT_MODEL_ATTRIBUTE_NAME );
    	}
    	return jenaOntModel;    	
    }

    public ApplicationBean getAppBean(){
        //return (ApplicationBean) getAttribute("appBean");
    	return getWebappDaoFactory().getApplicationDao().getApplicationBean();
    }
    public void setAppBean(ApplicationBean ab){
        setAttribute("appBean",ab);
    }

    /* These methods are overridden so that we might convert URL-encoded request parameters to UTF-8
     * Call static method setConvertParameterEncoding(false) to disable conversion.
     */
    @SuppressWarnings("unchecked")
	@Override
    public Map<String, String[]> getParameterMap() {
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || _req.getParameterMap() == null || this.getMethod().equals("POST") || !convertParameterEncoding) {
            return _req.getParameterMap();
        } else {
            if (convertedParameterMap == null){                
                convertedParameterMap = convertParameterMap( _req.getParameterMap() );
            }
            return convertedParameterMap;
        }
    }
    
    @Override
    public String getParameter(String name) {        
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || _req.getParameter(name) == null || this.getMethod().equals("POST") || !convertParameterEncoding)
            return _req.getParameter(name);
        else {
            Map<String, String[]> pmap = getParameterMap();
            String[] values = pmap.get(name);
            if( values == null )
                return null;
            else if( values.length == 0 )
                return null;
            else 
                return values[0];
        }
    }

    @Override
    public String[] getParameterValues(String name) {
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || this.getMethod().equals("POST") || !convertParameterEncoding)
            return _req.getParameterValues(name);
        else {
            Map<String, String[]> pmap = getParameterMap();
            if( pmap != null )
                return pmap.get(name);
            else
                return null;
        }
    }
    
    public static HashMap<String,String[]> convertParameterMap( Map<String, String[]> map ){
        if( map == null ) return null;
        
        HashMap<String,String[]> rMap = new HashMap<String,String[]>();
        Iterator<String> keyIt = map.keySet().iterator();
        while (keyIt.hasNext()) {        
            String key = keyIt.next();                
            rMap.put(key, convertValues( map.get(key) ));        
        }
        return rMap;
    }
    
    public static String[] convertValues(String[] in ){
        if( in == null ) return null;
        String[] rv = new String[ in.length ];        
        for (int i=0; i<in.length; i++) {
            rv[i] = convertValue( in[i] );
        }
        return rv;
    }
    
    public static String convertValue(String in ){
        if( in == null ) return null;
        try{            
            return new String(in.getBytes(FROM_ENCODING), TO_ENCODING);                    
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
            return null;
        }
    }

}
