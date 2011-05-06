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
//HttpServletRequest
public class VitroRequest extends HttpServletRequestWrapper {

    private static final String FROM_ENCODING = "ISO-8859-1";
    private static final String TO_ENCODING = "UTF-8";
    public static boolean convertParameterEncoding = true;

    public static boolean getConvertParameterEncoding() {
        return convertParameterEncoding;
    }

    public static void setConvertParameterEncoding(boolean cpe) {
        convertParameterEncoding = cpe;
    }

    private Map<String,String[]> convertedParameterMap;

    private HttpServletRequest _req;
    
    private boolean isSinglePortal;

    public VitroRequest(HttpServletRequest _req) {
        super(_req);
        if( _req == null )
            throw new IllegalArgumentException("Non-null HttpServletRequest needed" +
                    "to construct a VitroRequest");
        //if( _req.getAttribute("VitroRequestPrep.setup") ==null )
          //  throw new IllegalArgumentException("Cannot construct a VitroRequest if the HttpServletRequest " +
            //      "has not been setup correctly by the VitroRequestPrep filter");
        this._req = _req;
    }

    
    public void setWebappDaoFactory( WebappDaoFactory wdf){
        setAttribute("webappDaoFactory",wdf);
    }
    
    /** gets WebappDaoFactory with appropriate filtering for the request */
    public WebappDaoFactory getWebappDaoFactory(){
    	return (WebappDaoFactory) getAttribute("webappDaoFactory");
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
    
    public OntModel getJenaOntModel() {
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
    //@Override
    public Map getParameterMap() {
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || _req.getParameterMap() == null || this.getMethod().equals("POST") || !convertParameterEncoding) {
            return _req.getParameterMap();
        } else {
            if (convertedParameterMap == null){                
                convertedParameterMap = convertParameterMap( _req.getParameterMap() );
            }
            return convertedParameterMap;
        }
    }
    
    //@Override
    public String getParameter(String name) {        
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || _req.getParameter(name) == null || this.getMethod().equals("POST") || !convertParameterEncoding)
            return _req.getParameter(name);
        else {
            Map pmap = getParameterMap();
            String[] values = (String[])pmap.get(name);
            if( values == null )
                return null;
            else if( values.length == 0 )
                return null;
            else 
                return values[0];
        }
    }

    //@Override
    public String[] getParameterValues(String name) {
        if ((_req.getAttribute("convertParameterEncoding") != null && !((Boolean)_req.getAttribute("convertParameterEncoding"))) || this.getMethod().equals("POST") || !convertParameterEncoding)
            return _req.getParameterValues(name);
        else {
            Map pmap = getParameterMap();
            if( pmap != null )
                return (String[])pmap.get(name);
            else
                return null;
        }
    }
    
    public static HashMap<String,String[]> convertParameterMap( Map map ){
        if( map == null ) return null;
        
        HashMap<String,String[]> rMap = new HashMap<String,String[]>();
        Iterator keyIt = map.keySet().iterator();
        while (keyIt.hasNext()) {        
            String key =(String) keyIt.next();                
            rMap.put(key, convertValues( (String[])map.get(key) ));        
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

    /* *********** delegated methods *********** */
//    public Object getAttribute(String name) {
//        return _req.getAttribute(name);
//    }
//
//    public Enumeration getAttributeNames() {
//        return _req.getAttributeNames();
//    }
//
//    public String getAuthType() {
//        return _req.getAuthType();
//    }
//
//    public String getCharacterEncoding() {
//        return _req.getCharacterEncoding();
//    }
//
//    public int getContentLength() {
//        return _req.getContentLength();
//    }
//
//    public String getContentType() {
//        return _req.getContentType();
//    }
//
//    public String getContextPath() {
//        return _req.getContextPath();
//    }
//
//    public Cookie[] getCookies() {
//        return _req.getCookies();
//    }
//
//    public long getDateHeader(String name) {
//        return _req.getDateHeader(name);
//    }
//
//    public String getHeader(String name) {
//        return _req.getHeader(name);
//    }
//
//    public Enumeration getHeaderNames() {
//        return _req.getHeaderNames();
//    }
//
//    public Enumeration getHeaders(String name) {
//        return _req.getHeaders(name);
//    }
//
//    public ServletInputStream getInputStream() throws IOException {
//        return _req.getInputStream();
//    }
//
//    public int getIntHeader(String name) {
//        return _req.getIntHeader(name);
//    }
//
//    public String getLocalAddr() {
//        return _req.getLocalAddr();
//    }
//
//    public Locale getLocale() {
//        return _req.getLocale();
//    }
//
//    public Enumeration getLocales() {
//        return _req.getLocales();
//    }
//
//    public String getLocalName() {
//        return _req.getLocalName();
//    }
//
//    public int getLocalPort() {
//        return _req.getLocalPort();
//    }
//
//    public String getMethod() {
//        return _req.getMethod();
//    }
//
//    public Enumeration getParameterNames() {
//        return _req.getParameterNames();
//    }
//
//    public String getPathInfo() {
//        return _req.getPathInfo();
//    }
//
//    public String getPathTranslated() {
//        return _req.getPathTranslated();
//    }
//
//    public String getProtocol() {
//        return _req.getProtocol();
//    }
//
//    public String getQueryString() {
//        return _req.getQueryString();
//    }
//
//    public BufferedReader getReader() throws IOException {
//        return _req.getReader();
//    }
//
//    @Deprecated
//    public String getRealPath(String path) {
//        return _req.getRealPath(path);
//    }
//
//    public String getRemoteAddr() {
//        return _req.getRemoteAddr();
//    }
//
//    public String getRemoteHost() {
//        return _req.getRemoteHost();
//    }
//
//    public int getRemotePort() {
//        return _req.getRemotePort();
//    }
//
//    public String getRemoteUser() {
//        return _req.getRemoteUser();
//    }
//
//    public RequestDispatcher getRequestDispatcher(String path) {
//        return _req.getRequestDispatcher(path);
//    }
//
//    public String getRequestedSessionId() {
//        return _req.getRequestedSessionId();
//    }
//
//    public String getRequestURI() {
//        return _req.getRequestURI();
//    }
//
//    public StringBuffer getRequestURL() {
//        return _req.getRequestURL();
//    }
//
//    public String getScheme() {
//        return _req.getScheme();
//    }
//
//    public String getServerName() {
//        return _req.getServerName();
//    }
//
//    public int getServerPort() {
//        return _req.getServerPort();
//    }
//
//    public String getServletPath() {
//        return _req.getServletPath();
//    }
//
//    public HttpSession getSession() {
//        return _req.getSession();
//    }
//
//    public HttpSession getSession(boolean create) {
//        return _req.getSession(create);
//    }
//
//    public Principal getUserPrincipal() {
//        return _req.getUserPrincipal();
//    }
//
//    public boolean isRequestedSessionIdFromCookie() {
//        return _req.isRequestedSessionIdFromCookie();
//    }
//
//    @Deprecated
//    public boolean isRequestedSessionIdFromUrl() {
//        return _req.isRequestedSessionIdFromUrl();
//    }
//
//    public boolean isRequestedSessionIdFromURL() {
//        return _req.isRequestedSessionIdFromURL();
//    }
//
//    public boolean isRequestedSessionIdValid() {
//        return _req.isRequestedSessionIdValid();
//    }
//
//    public boolean isSecure() {
//        return _req.isSecure();
//    }
//
//    public boolean isUserInRole(String role) {
//        return _req.isUserInRole(role);
//    }
//
//    public void removeAttribute(String name) {
//        _req.removeAttribute(name);
//    }
//
//    public void setAttribute(String name, Object o) {
//        _req.setAttribute(name, o);
//    }
//
//    public void setCharacterEncoding(String env)
//            throws UnsupportedEncodingException {
//        _req.setCharacterEncoding(env);
//    }


}
