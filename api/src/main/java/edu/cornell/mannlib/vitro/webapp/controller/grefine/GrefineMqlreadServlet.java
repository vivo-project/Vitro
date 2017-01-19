/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;

import org.apache.jena.rdf.model.Literal;

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
				
				ObjectNode qJson = getResult(vreq, req, resp);
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

	private ObjectNode getResult(VitroRequest vreq, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode resultAllJson = mapper.createObjectNode();

		// parse query
		ArrayList<String> subjectUriList = new ArrayList<String>();
		Map<String, ArrayNode> propertyUriMap = new HashMap<String, ArrayNode>();
		String query = vreq.getParameter("query");
		parseQuery(query, subjectUriList, propertyUriMap);

		// run SPARQL query to get the results
		ArrayNode resultAllJsonArr = mapper.createArrayNode();
		DataPropertyStatementDao dpsDao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyStatementDao();
		for (String subjectUri: subjectUriList) {
			ObjectNode subjectPropertyResultJson = mapper.createObjectNode();
			subjectPropertyResultJson.put("id", subjectUri);
			for (Map.Entry<String, ArrayNode> entry : propertyUriMap.entrySet()) {
				int limit = 200; // default
				String propertyUri = entry.getKey();
				ArrayNode propertyUriOptions = entry.getValue();
				for (int i=0; i<propertyUriOptions.size(); i++) {
					ObjectNode propertyUriOption = (ObjectNode) propertyUriOptions.get(i);
					limit = ((NumericNode)propertyUriOption.get("limit")).intValue();
				}
				List<Literal> literals = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
				// Make sure the subject has a value for this property
				if (literals.size() > 0) {
					int counter = 0;
					ArrayNode valueJsonArr = mapper.createArrayNode();
					for (Literal literal: literals) {
						if (counter <= limit) {
							String value = literal.getLexicalForm();
							valueJsonArr.add(value);
						}
						counter++;
					}
					subjectPropertyResultJson.put(propertyUri, valueJsonArr);
				}
			}
			resultAllJsonArr.add(subjectPropertyResultJson);
		}
		resultAllJson.put("result", resultAllJsonArr);

		// System.out.println(resultAllJson);
		return resultAllJson;
	}

	/**
	 * Construct json from query String
	 * @param query Query
	 * @param subjectUriList Subject URIs
	 * @param propertyUriMap Property maps
	 */
	private void parseQuery(String query, ArrayList<String> subjectUriList, Map<String, ArrayNode> propertyUriMap) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rawJson = mapper.readTree(query);
			JsonNode queryNode = rawJson.get("query");
			if (queryNode instanceof ArrayNode) {
				ArrayNode qJsonArr = (ArrayNode)queryNode;
				for (int i=0; i<qJsonArr.size(); i++) {
					JsonNode obj = qJsonArr.get(i);
					Iterator<String> jsonObjNames = obj.fieldNames();
					while (jsonObjNames.hasNext()) {
						String objName = jsonObjNames.next();
						if (objName.contains("http://")) { // most likely this is a propertyUri
							// e.g. http://weill.cornell.edu/vivo/ontology/wcmc#cwid
							Object propertyUriObj = obj.get(objName);
							if (propertyUriObj instanceof ArrayNode) {
								propertyUriMap.put(objName, (ArrayNode) propertyUriObj);
							}
						} else if ("id".equals(objName)) { // id
							Object idObj = obj.get(objName); // TODO: This is a String object but not sure what it is for
						} else if ("id|=".equals(objName)) { // list of subject uri
							Object subjectUriObj = obj.get(objName);
							if (subjectUriObj instanceof ArrayNode) {
								ArrayNode subjectUriUriArr = (ArrayNode) subjectUriObj;
								for (int k = 0; k < subjectUriUriArr.size(); k++) {
									// e.g. http://vivo.med.cornell.edu/individual/cwid-jsd2002
									Object subjectUriUriObj = subjectUriUriArr.get(k);
									if (subjectUriUriObj instanceof TextNode) {
										subjectUriList.add(((TextNode) subjectUriUriObj).asText());
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("GrefineMqlreadServlet parseQuery IOException: " + e);
		}
 	}

}
