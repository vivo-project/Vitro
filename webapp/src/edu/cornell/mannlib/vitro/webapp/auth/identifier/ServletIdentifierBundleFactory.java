/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class is intended to provide access to a IdentifierBundleFactory in the
 * servlet context.
 *
 * @author bdc34
 *
 */
public class ServletIdentifierBundleFactory extends ArrayList<IdentifierBundleFactory> implements IdentifierBundleFactory {
    public static String IDENTIFIER_BUNDLE_FACTORY = "IdentifierBundleFactory";
    public static String IDENTIFIER_BUNDLE = "IdentifierBundle";

    /* ****************** static utility methods *************************/

    /**
     * Use this method to get an IdentifierBundleFactory for the servlet.
     * @param sc
     * @return
     */
    public static ServletIdentifierBundleFactory getIdentifierBundleFactory(ServletContext sc){
        if( sc != null ){
            Object obj = sc.getAttribute(IDENTIFIER_BUNDLE_FACTORY);
            if( obj != null && obj instanceof ServletIdentifierBundleFactory ){
                return (ServletIdentifierBundleFactory)obj;
            }else{
                ServletIdentifierBundleFactory sibf = new ServletIdentifierBundleFactory();
                sc.setAttribute(IDENTIFIER_BUNDLE_FACTORY, sibf);
                return sibf;
            }
        }else{
            return null;
        }        
    }

    /**
     * Gets IdentifierBundle for a request.
     * Session may be null.
     */
    public static IdentifierBundle getIdBundleForRequest(ServletRequest request, HttpSession session, ServletContext sc){
        if( request == null ) return null;
        IdentifierBundle ib = (IdentifierBundle)request.getAttribute(IDENTIFIER_BUNDLE);
        if( ib != null ) return ib;

        IdentifierBundleFactory ibf = getIdentifierBundleFactory(sc);
        ib = ibf.getIdentifierBundle(request,session,sc);
        request.setAttribute(IDENTIFIER_BUNDLE, ib);
        return ib;
    }

    public static IdentifierBundle getExistingIdBundle(ServletRequest request){
        if( request == null ) return null;
        IdentifierBundle ib = (IdentifierBundle)request.getAttribute(IDENTIFIER_BUNDLE);
        if( ib != null ) return ib;
        else
            return null;
    }
    
    public static void addIdentifierBundleFactory(ServletContext sc, IdentifierBundleFactory ibfToAdd){
        ServletIdentifierBundleFactory serverIbf = getIdentifierBundleFactory(sc);
        serverIbf.add( ibfToAdd );
    }

    /**
     * Consider using getIdBundleForRequest instead of this method.
     */
    public IdentifierBundle getIdentifierBundle(ServletRequest request, HttpSession session, ServletContext context) {
        IdentifierBundle ib = new ArrayIdentifierBundle();
        for(IdentifierBundleFactory ibf : this){
            if( ibf != null ){
                IdentifierBundle obj = ibf.getIdentifierBundle(request,session, context);
                if( obj != null )
                    ib.addAll( obj );
            }
        }
        return ib;
    }
}
