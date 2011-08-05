/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;

import com.hp.hpl.jena.rdf.model.Literal;

/**
 * This servlet is for servicing Google Refine's
 * "Add columns from VIVO"'s search for individual properties.
 * e.g. search for Joe Smith's email in VIVO and download to Google Refine under a new email column.
 * 
 * @author Eliza Chan (elc2013@med.cornell.edu)
 * 
 */
public class GrefineMqlreadServlet extends VitroHttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(GrefineMqlreadServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//resp.setContentType("application/json");
		super.doPost(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		resp.setContentType("application/json");
		VitroRequest vreq = new VitroRequest(req);
		try {
			if (vreq.getParameter("query") != null) {
				
				JSONObject qJson = getResult(vreq, req, resp);
				log.debug("result: " + qJson.toString());
				String responseStr = (vreq.getParameter("callback") == null) ? qJson
						.toString() : vreq.getParameter("callback") + "("
						+ qJson.toString() + ")";
				// System.out.println(responseStr);
				ServletOutputStream out = resp.getOutputStream();
				out.print(responseStr);
			}

		} catch (Exception ex) {
			log.warn(ex, ex);
		}
	}

	private JSONObject getResult(VitroRequest vreq, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {
		
		JSONObject resultAllJson = new JSONObject();

		try {
			// parse query
			ArrayList<String> subjectUriList = new ArrayList<String>();
			Map<String, JSONArray> propertyUriMap = new HashMap<String, JSONArray>();
			String query = vreq.getParameter("query");
			parseQuery(query, subjectUriList, propertyUriMap);
			
			// run SPARQL query to get the results
			JSONArray resultAllJsonArr = new JSONArray();
	        DataPropertyStatementDao dpsDao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyStatementDao();
	        for (String subjectUri: subjectUriList) {
	        	JSONObject subjectPropertyResultJson = new JSONObject();
	        	subjectPropertyResultJson.put("id", subjectUri);
	        	for (Map.Entry<String, JSONArray> entry : propertyUriMap.entrySet()) {
	        		int limit = 200; // default
	        		String propertyUri = entry.getKey();
	        		JSONArray propertyUriOptions = entry.getValue();
	        		for (int i=0; i<propertyUriOptions.length(); i++) {
	        			JSONObject propertyUriOption = (JSONObject)propertyUriOptions.get(i);
	        			limit = ((Integer)propertyUriOption.get("limit")).intValue();
	        		}
	        		List<Literal> literals = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
	                // Make sure the subject has a value for this property 
	                if (literals.size() > 0) {
	                	int counter = 0;
	                	JSONArray valueJsonArr = new JSONArray();
	                	for (Literal literal: literals) {
	                		if (counter <= limit) {
	                			String value = literal.getLexicalForm();
	                			valueJsonArr.put(value);
	                		}
	                		counter++;
	                	}
	                	subjectPropertyResultJson.put(propertyUri, valueJsonArr);
	                }
	        	}
	        	resultAllJsonArr.put(subjectPropertyResultJson);
	        }
	        resultAllJson.put("result", resultAllJsonArr);

		} catch (JSONException e) {
			log.error("GrefineMqlreadServlet getResult JSONException: " + e);
		}

		// System.out.println(resultAllJson);
		return resultAllJson;
	}

	/**
	 * Construct json from query String
	 * @param query
	 * @param subjectUriList
	 * @param propertyUriMap
	 */
	private void parseQuery(String query, ArrayList<String> subjectUriList, Map<String, JSONArray> propertyUriMap) {
		try {
			JSONObject rawJson = new JSONObject(query);
			JSONArray qJsonArr = rawJson.getJSONArray("query");
			for (int i=0; i<qJsonArr.length(); i++) {
				Object obj = qJsonArr.get(i);

				if (obj instanceof JSONObject) {
					JSONObject jsonObj = (JSONObject)obj;
					JSONArray jsonObjNames = jsonObj.names();
					for (int j=0; j<jsonObjNames.length(); j++) {
						String objName = (String)jsonObjNames.get(j);
						if (objName.contains("http://")) { // most likely this is a propertyUri
							// e.g. http://weill.cornell.edu/vivo/ontology/wcmc#cwid
							Object propertyUriObj = jsonObj.get(objName);
							if (propertyUriObj instanceof JSONArray) {
								propertyUriMap.put(objName, (JSONArray)propertyUriObj);
							}
						} else if ("id".equals(objName)) { // id
							Object idObj = jsonObj.get(objName); // TODO: This is a String object but not sure what it is for
						} else if ("id|=".equals(objName)) { // list of subject uri
							Object subjectUriObj = jsonObj.get(objName);
							if (subjectUriObj instanceof JSONArray) {
								JSONArray subjectUriUriArr = (JSONArray)subjectUriObj;
								for (int k=0; k<subjectUriUriArr.length(); k++) {
									// e.g. http://vivo.med.cornell.edu/individual/cwid-jsd2002
									Object subjectUriUriObj = subjectUriUriArr.get(k);
									if (subjectUriUriObj instanceof String) {
										subjectUriList.add((String)subjectUriUriObj);
									}
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			log.error("GrefineMqlreadServlet parseQuery JSONException: " + e);
		}
 	}

}
