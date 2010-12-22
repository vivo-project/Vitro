/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
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
public class IndexController extends FreemarkerHttpServlet {
	
	private static final Log log = LogFactory.getLog(IndexController.class);
	
//    public void doPost(HttpServletRequest request, HttpServletResponse response)
//    throws ServletException,IOException {
//        doGet(request, response);
//    }
//
//    public void doGet( HttpServletRequest request, HttpServletResponse response )
//    throws IOException, ServletException {
//        if (!LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.DBA)) {
//            String redirectURL=request.getContextPath() + Controllers.SITE_ADMIN + "?login=block";
//            response.sendRedirect(redirectURL);
//            return;
//        }
//        
//        long start = System.currentTimeMillis();
//        try {
//            IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
//            if( request.getParameter("update") != null ){
//                builder.doUpdateIndex();
//            }else{
//                builder.doIndexRebuild();
//            }
//            
//        } catch (IndexingException e) {
//            log.error("IndexController -- Error building index: " + e);
//        }
//        long delta = System.currentTimeMillis() - start;
//        String msg = "Search index complete. Elapsed time " + delta + " msec.";
//    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return "Full Search Index Rebuild";
    }
    
//    @Override
//    protected int requiresLoginLevel() {
//        // User must be logged in to view this page.
//        return LoginStatusBean.DBA;
//    }
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) { 
        // Due to requiresLoginLevel(), we don't get here unless logged in as DBA
        if (!LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.DBA)) {
            return new RedirectResponseValues(UrlBuilder.getUrl(Route.LOGIN));
        }
        Map<String, Object> body = new HashMap<String, Object>();
        
        try {
            IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
            if( vreq.getParameter("update") != null ){
                builder.doUpdateIndex();
            }else{
                builder.doIndexRebuild();
            }
            
        } catch (IndexingException e) {
        	log.error("Error rebuilding search index",e);
        	body.put("errorMessage", "There was an error while rebuilding the search index. " + e.getMessage());
        	return new ExceptionResponseValues(Template.ERROR_MESSAGE.toString(), body, e);            
        }
        
        body.put("message","Rebuilding of index started."); 
        return new TemplateResponseValues(Template.MESSAGE.toString(), body);
    }
}
