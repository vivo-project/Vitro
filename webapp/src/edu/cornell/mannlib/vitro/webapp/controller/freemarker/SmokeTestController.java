/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.smoketest.SolrContextChecker;
import edu.cornell.mannlib.vitro.webapp.utils.smoketest.TestResult;

/**
 * The controller responsible for checking statuses of various
 * services across the web application. (Ex: Solr Server, etc.)
 * TODO: This is just an initial test implementation and will continue
 * to change.
 */
public class SmokeTestController extends FreemarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(SmokeTestController.class.getName());
    
	private static final String TEMPLATE_NAME = "smokeTest.ftl";
	
	@Override
	protected ResponseValues processRequest(VitroRequest vreq){
		
		SolrContextChecker solrContextChecker = new SolrContextChecker();
		TestResult result = solrContextChecker.test(vreq);
		
        Map<String, Object> body = new HashMap<String, Object>();
        
        body.put("SolrContextChecker", result);
		
		
		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}
}
