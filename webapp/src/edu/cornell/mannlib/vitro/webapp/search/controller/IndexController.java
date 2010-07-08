/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Accepts requests to rebuild or update the search index.  It uses
 * an IndexBuilder and finds that IndexBuilder from the servletContext using
 * the key "edu.cornel.mannlib.vitro.search.indexing.IndexBuilder"
 *
 * That IndexBuilder will be associated with a object that implements the IndexerIface.
 *
 * An example of the IndexerIface is LuceneIndexer.
 * An example of the IndexBuilder and LuceneIndexer getting setup is in LuceneSetup.
 *
 * @author bdc34
 *
 */
public class IndexController extends HttpServlet {
	
	private static final Log log = LogFactory.getLog(IndexController.class.getName());
	
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        
        Object obj = request.getSession().getAttribute("loginHandler");        
        LoginFormBean loginHandler = null;
        if( obj != null && obj instanceof LoginFormBean )
            loginHandler = ((LoginFormBean)obj);
        if( loginHandler == null ||
            ! "authenticated".equalsIgnoreCase(loginHandler.getLoginStatus()) ||
             Integer.parseInt(loginHandler.getLoginRole()) <= 5 ){       
            
            String redirectURL=request.getContextPath() + Controllers.SITE_ADMIN + "?login=block";
            response.sendRedirect(redirectURL);
            return;
        }
        
        try {
            IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
            if( request.getParameter("update") != null ){
                builder.doUpdateIndex();
            }else{
                builder.doIndexRebuild();
            }
            
        } catch (IndexingException e) {
            log.error("IndexController -- Error building index: " + e);
        }
    }
}
