/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.MultipartHttpServletRequest;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * Accepts requests to update a set of URIs in the search index. 
 */
@SuppressWarnings("serial")
public class SearchServiceController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory.getLog(SearchServiceController.class);

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
        return SimplePermission.MANAGE_SEARCH_INDEX.ACTIONS;
	}	

	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the help page.
	 */  
	@Override
	protected ResponseValues processRequest(VitroRequest req) {
		try {
			// Works by side effect: parse the multi-part request and stash FileItems in request			
			new MultipartHttpServletRequest( req );
			
            //figure out what action to perform
            String pathInfo = req.getPathInfo();
                        
            if( pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim()) ){
                return doHelpForm(req);
            }
            
            pathInfo = pathInfo.substring(1);  //get rid of leading slash
            
			if (VERBS.UPDATE_URIS_IN_SEARCH.verb.equals( pathInfo )) {
				return doUpdateUrisInSearch(req);
			} else {			
				return doHelpForm(req);
			}
		} catch (Exception e) {
			return new ExceptionResponseValues(e);
		}
	}


    public ResponseValues doUpdateUrisInSearch(HttpServletRequest req )
        throws IOException, ServletException {
    
       IndexBuilder builder = IndexBuilder.getBuilder(getServletContext());
       if( builder == null )
           throw new ServletException( "Could not get search index builder from context. Check smoke test");

        new UpdateUrisInIndex().doUpdateUris( req, builder);

        TemplateResponseValues trv = new TemplateResponseValues( "" );
        return trv;
    }

    
    public ResponseValues doHelpForm(HttpServletRequest req){
        return  new TemplateResponseValues( "searchService-help.ftl");
    }
    
    public enum VERBS{
        UPDATE_URIS_IN_SEARCH("updateUrisInSearch");
        
        public final String verb;
        VERBS(String verb){
            this.verb = verb;
        }
    }    
    
}
