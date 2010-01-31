/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;

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
    //             System.out.println("exception in utilBean");
    //         }

            Enumeration names = req.getParameterNames();
            String value = null;
            String name = null;
            while( names.hasMoreElements() ){
                name = (String)names.nextElement();
    //             System.out.println("parameter name: " + name);
                value = req.getParameter( name );
                if( value != null ){
    //                 System.out.println("*** current set " + name + " to " + value );
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
