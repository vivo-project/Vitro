/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import org.apache.solr.client.solrj.SolrQuery;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Tests on various methods in the class.
 * @author Eliza Chan (elc2013@med.cornell.edu)
 *
 */
public class JSONReconcileServletTest extends AbstractTestClass {

	private HttpServletRequestStub request;
	private HttpServletResponseStub response;
	private JSONReconcileServlet reconcile;

	@Before
	public void setup() throws Exception {
		request = new HttpServletRequestStub();
		request.setRequestUrl(new URL("http://vivo.this.that/reconcile"));
		request.setMethod("POST");
		response = new HttpServletResponseStub();
		reconcile = new JSONReconcileServlet();
	}
	
	@Test
	public void getMetadata() {
		int serverPort = 8080;
		String defaultNamespace = "http://vivo.this.that/individual/";
		String defaultTypeList = null;
		String serverName = null;
		String schemaSpaceOutput = null;
		JSONObject jsonResult = null;
		try {
			jsonResult = reconcile.getMetadata(request, response, defaultNamespace, defaultTypeList, serverName, serverPort);
			schemaSpaceOutput = jsonResult.getString("schemaSpace");
		} catch (ServletException e) {
			System.err.println("JSONReconcileServletTest getMetadata ServletException: " + e);
		} catch (JSONException e) {
			System.err.println("JSONReconcileServletTest getMetadata JSONException: " + e);
		}
		Assert.assertNotNull("output should not be null", jsonResult);
		Assert.assertEquals("schemaSpaceOutput", defaultNamespace, schemaSpaceOutput);
	}
	
	@Test
	public void getQuery() {
		// contruct query
		int rowNum = 3;

		String nameStr = "Joe";
		String nameType = "http://xmlns.com/foaf/0.1/Person";

		ArrayList<String[]> propertiesList = new ArrayList<String[]>();
		String orgStr = "Something";
		String orgType = "http://xmlns.com/foaf/0.1/Organization";
		String[] properties = {orgType, orgStr};
		propertiesList.add(properties);

		// test getQuery
		SolrQuery solrQuery = reconcile.getQuery(nameStr, nameType, rowNum, propertiesList);
		String messagePrefix = "Query should contain the text: ";
		testAssertTrue(messagePrefix + orgStr, orgStr, solrQuery.toString());
		testAssertTrue(messagePrefix + nameStr, nameStr, solrQuery.toString());
		testAssertTrue(messagePrefix + orgType, orgType, solrQuery.toString());
		testAssertTrue(messagePrefix + nameType, orgType, solrQuery.toString());
	}
	
	private void testAssertTrue(String message, String inputStr, String resultStr) {
		try {
			String modStr = null;
			if (inputStr.contains(":") && inputStr.contains("/")) { 
				modStr = inputStr.replaceAll(":", "%3A").replaceAll("/", "%2F");
			} else {
				modStr = inputStr;
			}
		    Pattern regex = Pattern.compile(modStr, Pattern.CASE_INSENSITIVE);
		    Matcher regexMatcher = regex.matcher(resultStr);
		    Assert.assertTrue(message, regexMatcher.find());
		} catch (PatternSyntaxException e) {
		    // Syntax error in the regular expression
			System.err.println("JSONReconcileServletTest testAssertTrue PatternSyntaxException: " + e);
		}
	}
}
