/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.smoketest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrServer;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A sample class implementing SmokeTestsRunner interface that 
 * prints out to a webpage the status of SolrServer i.e whether it 
 * is up and running or not.
 * TODO: This is just an initial test implementation and will continue
 * to change.
 */
public class SolrContextChecker implements SmokeTest {

	@Override
	public TestResult test(VitroRequest vreq) {
	
		HttpSession session = vreq.getSession();
		ServletContext context = (ServletContext)session.getServletContext();
		
		//get the index details about SolrServer from the context
		SolrServer server = (SolrServer) context.getAttribute("vitro.local.solr.server");
		
		TestResult testResult;
		
		if(server != null){
			 testResult = new TestResult("Solr Server is up and running!", true);
		}else{
			testResult = null;
		}
	
		return testResult;
	}
	
	@Override
	public String getName(){
		return SolrContextChecker.class.getName();
	}

}
