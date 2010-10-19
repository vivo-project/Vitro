/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class MiscWebUtils {
    /**
         * Takes each http request parameter in req that has a name that matches
         * a key of a property in properties, and updates the value in current.
         *
         */
	
	private static final Log log = LogFactory.getLog(MiscWebUtils.class.getName());
	
        public static Properties loadPropertiesFromRequest(Properties current, ServletRequest req){
            if(req == null || current == null){
                log.error("UtilBean.loadPropertiesFromRequest() exiting because of null input");
                return current;
            }
    //         try{
    //             current.store(System.out,"header from store");
    //         }catch( Exception ex){
    //             log.debug("exception in utilBean");
    //         }

            Enumeration names = req.getParameterNames();
            String value = null;
            String name = null;
            while( names.hasMoreElements() ){
                name = (String)names.nextElement();
    //             log.debug("parameter name: " + name);
                value = req.getParameter( name );
                if( value != null ){
    //                 log.debug("*** current set " + name + " to " + value );
                        current.setProperty(name,value);
                }

            }
            return current;
        }
        
    /**
     * Gets an attribute from the request, if it is not null, and of Class String
     * print it to req.out, otherwise throw an exception.
     *
     * @param req
     * @param attribute
     */
    public static String writeAttribute(HttpServletRequest request,  String attribute)
    throws JspException{
        Object contentObj=request.getAttribute(attribute);
        if(contentObj == null )
            throw new JspException("Attribute " + attribute + " in request attributes was null.");
        if( ! (contentObj instanceof String) ){
            String className = contentObj.getClass().getName();
            throw new JspException("Class of "+attribute+" is " + className + ", it should be String");
        }
        return (String) contentObj;
    }

	public static String getCustomShortView(HttpServletRequest request) {	
		Individual object = ((ObjectPropertyStatement) request
				.getAttribute("opStmt")).getObject();
		return getCustomShortView(object, request);
	}

	// Get custom short view from either the object's class or one of its
    // superclasses. This is needed because the inference update happens asynchronously, 
    // so when a new property has been added and the page is reloaded, the custom short view
    // from a superclass may not have been inferred yet.

	public static String getCustomShortView(Individual individual, HttpServletRequest request) {
	if( individual == null ) return null;

        VitroRequest vreq = new VitroRequest(request);
        VClassDao vcDao = vreq.getWebappDaoFactory().getVClassDao();	
       log.debug("searching for custom short view for " + individual.getURI()); 

	String customShortView = null;
        List<VClass> vclasses = individual.getVClasses(true); // get directly
        // asserted vclasses
        Set<String> superClasses = new HashSet<String>();
        
        // First try directly asserted classes, there is no useful decision
        // mechanism for the case where two directly asserted classes
        // have a custom short view.
        // RY If we're getting the custom short view with reference to an object property.
        // should we use the property's getRangeVClass() method instead?
        for (VClass vclass : vclasses) {
	    log.debug( vclass.getURI() );
            // Use this class's custom short view, if there is one
            customShortView = vclass.getCustomShortView();
            if (customShortView != null) {
		log.debug( customShortView );
                return customShortView;
            }
            // Otherwise, add superclass to list of vclasses to check for custom
            // short views
            String vclassUri = vclass.getURI();
            superClasses.addAll(vcDao.getAllSuperClassURIs(vclassUri));
        }
        
        // Next try super classes. There is no useful decision mechanism for
        // the case where two super classes have a custom short view.
	log.debug("checking superclasses for custom short view");
        for (String superClassUri : superClasses) {
            VClass vc = vcDao.getVClassByURI(superClassUri);
            customShortView = vc.getCustomShortView();
		log.debug(vc.getURI());
            if (customShortView != null) {
		log.debug(customShortView);
                return customShortView;
            }
        }

        return null;	    
	}
	
    /**
     * returns a table of the req attributes
     * @param req
     * @return
     */
    public static String getRequestAttributes( HttpServletRequest req){
        String val = "<table>";
        Enumeration names = req.getAttributeNames();
        while(names.hasMoreElements() ){
            String name = (String)names.nextElement();
            val += "\n\t<tr><td>" + name + "</td><td><pre>";
            String value = null;
            try{
                Object obj = req.getAttribute(name);
                value = (obj instanceof Model || obj instanceof ModelCom) ? "[Jena model object]" :
                	(obj == null) ? "[null]" : 
                		StringEscapeUtils.escapeHtml(obj.toString());
            }catch(Exception ex){
                value = "unable to get value" ;
            }  catch (Error er){
                value="unable to get value";
            } catch (Throwable th){
                value = "unable to get value";
            }
            val += value + "</pre></td></tr>\n";
        }
        return val + "</table>";
    }

    public static String getRequestParam( HttpServletRequest req){
        String val = "<table>";
        Enumeration names = req.getParameterNames();
        while(names.hasMoreElements() ){

            String name = (String)names.nextElement();
            val += "\n\t<tr><td><h3>" + name + "</h3><td><pre>";
            String value = null;
            try{
                Object obj = req.getParameter(name);
                value = (obj == null) ? "[null]" : 
                	StringEscapeUtils.escapeHtml(obj.toString());
            }catch(Exception ex){
                value = "unable to get value" ;
            }  catch (Error er){
                value="unable to get value";
            } catch (Throwable th){
                value = "unable to get value";
            }
            val += value + "</pre><td></tr>\n";
        }
        return val + "</table>";
    }

    public static String getSessionAttributes(HttpServletRequest req){
        String val = "<table>";
        Enumeration names = req.getSession().getAttributeNames();
        while(names.hasMoreElements() ){
            String name = (String)names.nextElement();
            val += "\n\t<tr><td><h3>" + name + "</h3><td><pre>";
            String value = null;
            try{
                Object obj = req.getSession().getAttribute(name);
                value = (obj instanceof Model || obj instanceof ModelCom) ? "[Jena model object]" :
                	(obj == null) ? "[null]" : 
                		StringEscapeUtils.escapeHtml(obj.toString());
            }catch(Exception ex){
                value = "unable to get value" ;
            }  catch (Error er){
                value="unable to get value";
            } catch (Throwable th){
                value = "unable to get value";
            }
            val += value + "</pre></td></tr>\n";
        }
        return val + "</table>";
    }

    public static String getReqInfo( HttpServletRequest req){
        String rv = null;
        try{
            rv =
         "<table>"
        +"<tr><td><h2>Request Attributes:</h2></td></tr>"
        +"<tr><td>" + getRequestAttributes(req) + "</td></tr>"
        +"<tr><td>--------------------------</td></tr>"
        +"<tr><td><h2>Request Parameters:</h2></td></tr>"
        +"<tr><td>" + getRequestParam(req) + "</td></tr>"
        +"<tr><td>---------------------------</td></td>"
        +"<tr><td><h2>Session Attributes:</h2></td></tr>"
        +"<tr><td>"+ getSessionAttributes(req) +"</td></tr>"
        + "</table>";
        }catch(Throwable t){
            rv = "MiscWebUtils.getRequestParam() Error :\n"+t;
            t.printStackTrace();
        }
        return rv;
    }


    public static void debugPrintHeaders(HttpServletRequest req){
	    Enumeration hnames = req.getHeaderNames();
	    while( hnames.hasMoreElements() ){
	    	String name = (String) hnames.nextElement();
	    	log.debug("header " + name);
	    	String value = req.getHeader(name);
	    	log.debug("    " + value);
	    	Enumeration values = req.getHeaders(name);
	    	if( values == null ){
	    		log.debug("    enumeration was null");            		
	    	}else{
	    		log.debug("    enumeration values");
	    		while( values.hasMoreElements() ){
	    			String val = (String) values.nextElement();
	    			log.debug("    " + value);
	    		}
	    	}            
	    }
    }
    
    /**
        This isfrom org.json.simple.JSONObject

     * " => \" , \ => \\
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		if(s==null)
			return null;
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				if(ch>='\u0000' && ch<='\u001F'){
					String ss=Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0;k<4-ss.length();k++){
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}//for
		return sb.toString();
	}
}
