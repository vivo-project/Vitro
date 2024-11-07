/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;

/**
 * This servlet is for servicing JSON requests from Google Refine's
 * Reconciliation Service.
 *
 * @author Eliza Chan (elc2013@med.cornell.edu)
 *
 */
@WebServlet(name = "JSON Reconcile Service", urlPatterns = {"/reconcile"} )
public class JSONReconcileServlet extends VitroHttpServlet {

	private static final String INTERNAL_QUERY_NAME = "A0B1C2";

	private static final String PARAM_QUERY = "term";
    private static final long serialVersionUID = 1L;
    private static String QUERY_PARAMETER_NAME = "term";
    private static final String PARAM_RDFTYPE = "type";
	public static final int MAX_QUERY_LENGTH = 500;
	private static final int DEFAULT_MAX_HIT_COUNT = 1000;
	private static final Log log = LogFactory.getLog(JSONReconcileServlet.class.getName());

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
		resp.setCharacterEncoding("UTF-8");
		VitroRequest vreq = new VitroRequest(req);

		try {
			if (vreq.getParameter("query") != null
					|| vreq.getParameter("queries") != null) {
				ObjectNode qJson = getResult(vreq, req, resp);
				log.debug("result: " + qJson.toString());
				String responseStr = (vreq.getParameter("callback") == null) ? qJson
						.toString() : vreq.getParameter("callback") + "("
						+ qJson.toString() + ")";
				// System.out.println("JSONReconcileServlet result: " + responseStr);
				PrintWriter out = resp.getWriter();
				out.print(responseStr);
			} else { // metadata
				String defaultNamespace = null;
				String defaultTypeList = null;
				String serverName = null;
				int serverPort = req.getServerPort();

				if (vreq.getWebappDaoFactory() != null) {
					defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
				}
				defaultTypeList = ConfigurationProperties.getBean(req).getProperty("Vitro.reconcile.defaultTypeList");
				serverName = req.getServerName();
				ObjectNode metaJson = getMetadata(req, resp, defaultNamespace, defaultTypeList, serverName, serverPort);
				String callbackStr = (vreq.getParameter("callback") == null) ? ""
						: vreq.getParameter("callback");
				PrintWriter out = resp.getWriter();
				out.print(callbackStr + "(" + metaJson.toString() + ")");
			}
		} catch (Exception ex) {
			log.warn(ex, ex);
		}
	}

	private ObjectNode getResult(VitroRequest vreq, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {

		HashMap<String, JsonNode> searchWithTypeMap = new HashMap<String, JsonNode>();
		HashMap<String, JsonNode> searchNoTypeMap = new HashMap<String, JsonNode>();
		ArrayList<String> queries = new ArrayList<String>();
		Object qObj = vreq.getParameter("queries");

		if (qObj == null) {
			qObj = vreq.getParameter("query");
			if (qObj instanceof String) {
				if (!((String)qObj).contains("{")) {
					qObj = "{ \"query\" : \"" + qObj + "\" }";
				}
			}
		}

		if (qObj != null && qObj instanceof String) {
			// e.g.
			// {"q0":{"query":"Cathleen","type":"http://xmlns.com/foaf/0.1/Person","type_strict":"should"},
			// "q1":{"query":"Geoffrey","type":"http://xmlns.com/foaf/0.1/Person","type_strict":"should"},
			// "q2":{"query":"Dina","type":"http://xmlns.com/foaf/0.1/Person","type_strict":"should"}}
			String qStr = (String) qObj;
			queries.add(qStr);
			// System.out.println("JSONReconcileServlet query: " + qStr);
			log.debug("\nquery: " + qStr + "\n");
		}

		try {
			for (String queryStr : queries) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(queryStr);

				if (json.has("query")) { // single query
					if (json.has("type")) {
						searchWithTypeMap.put(INTERNAL_QUERY_NAME, json);
					} else {
						// user did not specify a type
						searchNoTypeMap.put(INTERNAL_QUERY_NAME, json);
					}
				} else { // multiple queries
					for (Iterator<String> iter = json.fieldNames(); iter.hasNext(); ) {
						ArrayList<JsonNode> jsonList = new ArrayList<JsonNode>();
						String key = iter.next();
						JsonNode jsonLvl2 = json.get(key);
						if (jsonLvl2.has("query")) {
							if (jsonLvl2.has("type")) {
								searchWithTypeMap.put(key, jsonLvl2);
							} else {
								// user did not specify a type
								searchNoTypeMap.put(key, jsonLvl2);
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			log.error("JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet JSONException: "
					+ ex);
		}

		// Run index search
		ObjectNode qJson = null;
		if (searchWithTypeMap.size() > 0) {
			qJson = runSearch(searchWithTypeMap);
		} else {
			qJson = runSearch(searchNoTypeMap);
		}
		// TODO: the domain of the id value should be the same as that of the reconcile service
		// and not the namespace. e.g. if the reconcile servlet domain is http://mac123456:8080/vivo, id
		// should be http://mac123456:8080/vivo/individual/jsmith
		return qJson;
	}

	/**
	 * Returns a default JSON response.
	 *
	 * @param req Servlet Request
	 * @param resp Servlet Response
	 */
	protected ObjectNode getMetadata(HttpServletRequest req, HttpServletResponse resp, String defaultNamespace,
									 String defaultTypeList, String serverName, int serverPort) throws ServletException {

		ObjectNode json = JsonNodeFactory.instance.objectNode();

		json.put("name", "VIVO Reconciliation Service");
		if (defaultNamespace != null) {
			json.put("identifierSpace", defaultNamespace);
			json.put("schemaSpace", defaultNamespace);
		}
		ObjectNode viewJson = JsonNodeFactory.instance.objectNode();
		StringBuilder urlBuf = new StringBuilder();
		urlBuf.append("http://").append(serverName);
		if (serverPort == 8080) {
			urlBuf.append(":").append(serverPort);
		}
		if (ContextPath.getPath(req) != null) {
			urlBuf.append(ContextPath.getPath(req));
		}
		viewJson.put("url", urlBuf.toString() + "/individual?uri={{id}}");
		json.put("view", viewJson);

		// parse defaultTypeList from runtime.properties
		if (defaultTypeList != null) {
			String[] splitList = defaultTypeList.split(";");
			String[][] idNameArray = new String[splitList.length][splitList.length];
			for(int i = 0; i<splitList.length; i++) {
				idNameArray[i] = splitList[i].split(",");
			}
			// process and add to json defaultTypes
			ArrayNode defaultTypesJsonArr = JsonNodeFactory.instance.arrayNode();
			for (String[] anIdNameArray : idNameArray) {
				ObjectNode defaultTypesJson = JsonNodeFactory.instance.objectNode();
				defaultTypesJson.put("id", anIdNameArray[0].trim());
				defaultTypesJson.put("name", anIdNameArray[1].trim());
				defaultTypesJsonArr.add(defaultTypesJson);
			}
			json.put("defaultTypes", defaultTypesJsonArr);
		}

		return json;
	}

	private ObjectNode runSearch(HashMap<String, JsonNode> currMap) throws ServletException {
		ObjectNode qJson = JsonNodeFactory.instance.objectNode();

		try {

			for (Map.Entry<String, JsonNode> entry : currMap.entrySet()) {
				ObjectNode resultAllJson = JsonNodeFactory.instance.objectNode();
				String key = entry.getKey();
				JsonNode json = entry.getValue();
				String queryVal = json.get("query").asText();

				// System.out.println("query: " + json.toString());

				// continue with properties list
				String searchType = null;
				int limit = 10; // default
				String typeStrict = "should"; // default
				ArrayList<String[]> propertiesList = new ArrayList<String[]>();

				if (json.has("type")) {
					searchType = json.get("type").asText();
				}
				if (json.has("limit")) {
					limit = json.get("limit").asInt();
				}
				if (json.has("type_strict")) { // Not sure what this variable
												// represents. Skip for now.
					typeStrict = json.get("type_strict").asText();
				}
				if (json.has("properties")) {
					JsonNode propertiesNode = json.get("properties");
					if (propertiesNode instanceof ArrayNode) {
						ArrayNode properties = (ArrayNode)propertiesNode;
						for (int i = 0; i < properties.size(); i++) {
							String[] pvPair = new String[2];
							JsonNode jsonProperty = properties.get(i);
							String pid = JacksonUtils.getString(jsonProperty, "pid");
							String v = JacksonUtils.getString(jsonProperty, "v");
							if (pid != null && v != null) {
								pvPair[0] = pid;
								pvPair[1] = v;
								propertiesList.add(pvPair);
							}
						}
					}
				}

				// begin search
				ArrayNode resultJsonArr = JsonNodeFactory.instance.arrayNode();

	            SearchQuery query = getQuery(queryVal, searchType, limit, propertiesList);
	            SearchResponse queryResponse = null;
	            if (query != null) {
	                SearchEngine search = ApplicationUtils.instance().getSearchEngine();
	            	queryResponse = search.query(query);
	            } else {
	            	log.error("Query for a search was null");
	            }

	            SearchResultDocumentList docs = null;
	            if (queryResponse != null) {
	            	docs = queryResponse.getResults();
	            } else {
	            	log.error("Query response for a search was null");
	            }

	            if (docs != null) {

	                List<SearchResult> results = new ArrayList<SearchResult>();
	                for (SearchResultDocument doc : docs) {
	                    try {
	                        String uri = doc.getStringValue(VitroSearchTermNames.URI);
	                        String name = doc.getStringValue(VitroSearchTermNames.NAME_RAW);

	                        SearchResult result = new SearchResult(name, uri);

	                        // populate result for Google Refine
							ObjectNode resultJson = JsonNodeFactory.instance.objectNode();
							Object score = doc.getFirstValue("score");
							if (score instanceof Double) {
								resultJson.put("score", (Double)score);
							} else if (score instanceof Float) {
								resultJson.put("score", (Float)score);
							}
							String modUri = result.getUri().replace("#", "%23");
							resultJson.put("id", modUri);
							resultJson.put("name", result.getLabel());

							Collection<Object> rdfTypes = doc.getFieldValues(VitroSearchTermNames.RDFTYPE);
							ArrayNode typesJsonArr = JsonNodeFactory.instance.arrayNode();
							if (rdfTypes != null) {

								for (Object rdfType : rdfTypes) {

									// e.g.
									// http://aims.fao.org/aos/geopolitical.owl#area
									String type = (String) rdfType;
									int lastIndex2 = type.lastIndexOf('/') + 1;
									String typeName = type.substring(lastIndex2);
									typeName = typeName.replace("#", ":");
									ObjectNode typesJson = JsonNodeFactory.instance.objectNode();
									typesJson.put("id", type);
									typesJson.put("name", typeName);
									typesJsonArr.add(typesJson);

								}
							}

							resultJson.put("type", typesJsonArr);
							resultJson.put("match", false);
							resultJsonArr.add(resultJson);

						} catch (Exception e) {
							log.error("problem getting usable individuals from search "
									+ "hits" + e.getMessage());
						}
					}
	            } else {
	            	log.error("Docs for a search was null");
	            }
				resultAllJson.put("result", resultJsonArr);
				qJson.put(key, resultAllJson);
				// System.out.println("results: " + qJson.toString());
			}

		} catch (SearchEngineException ex) {
			log.error("JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet SearchEngineException: "
					+ ex);
		}

		if (qJson.has(INTERNAL_QUERY_NAME)) {
			return (ObjectNode)qJson.get(INTERNAL_QUERY_NAME);
		}

		return qJson;
	}

    protected SearchQuery getQuery(String queryStr, String searchType, int limit, ArrayList<String[]> propertiesList) {

        if ( queryStr == null) {
            log.error("There was no parameter '"+ PARAM_QUERY
                +"' in the request.");
            return null;
        } else if( queryStr.length() > MAX_QUERY_LENGTH ) {
            log.debug("The search was too long. The maximum " +
                    "query length is " + MAX_QUERY_LENGTH );
            return null;
        }

        /// original
        ///SearchQuery query = new SearchQuery();

        /// test
        SearchQuery query = ApplicationUtils.instance().getSearchEngine().createQuery(queryStr.toLowerCase());

        // original code:
        // query.setStart(0).setRows(DEFAULT_MAX_HIT_COUNT);
        // Google Refine specific:
        query.setStart(0).setRows(limit);

        // TODO: works better without using tokenizeNameQuery(), need to investigate a bit more
        /// comment out original: query.setQuery(queryStr);

        // Filter by type
        // e.g. http://xmlns.com/foaf/0.1/Person
        if (searchType != null) {
        	query.addFilterQuery(VitroSearchTermNames.RDFTYPE + ":\"" + searchType + "\"");
        }

        // Added score to original code:
        query.addFields(VitroSearchTermNames.NAME_RAW, VitroSearchTermNames.URI, "*", "score"); // fields to retrieve

		// if propertiesList has elements, add extra queries to query
		for (String[] pvPair : propertiesList) {
			query.addFilterQueries(tokenizeNameQuery(pvPair[1]), VitroSearchTermNames.RDFTYPE + ":\"" + pvPair[0] + "\"");
		}

        // Can't sort on multivalued field, so we sort the results in Java when we get them.
        // query.addSortField(VitroSearchTermNames.NAME_LOWERCASE, Order.ASC);

        return query;
    }



    private String tokenizeNameQuery(String queryStr) {
        /* We currently have no use case for a tokenized, unstemmed autocomplete search field, so the option
         * has been disabled. If needed in the future, will need to add a new field and field type which
         * is like AC_NAME_STEMMED but doesn't include the stemmer.
        String stemParam = (String) request.getParameter("stem");
        boolean stem = "true".equals(stemParam);
        if (stem) {
            String acTermName = VitroSearchTermNames.AC_NAME_STEMMED;
            String nonAcTermName = VitroSearchTermNames.NAME_STEMMED;
        } else {
            String acTermName = VitroSearchTermNames.AC_NAME_UNSTEMMED;
            String nonAcTermName = VitroSearchTermNames.NAME_UNSTEMMED;
        }
        */

        String acTermName = VitroSearchTermNames.AC_NAME_STEMMED;
        String nonAcTermName = VitroSearchTermNames.NAME_STEMMED;
        String acQueryStr;

        if (queryStr.endsWith(" ")) {
            acQueryStr = makeTermQuery(nonAcTermName, queryStr, true);
        } else {
            int indexOfLastWord = queryStr.lastIndexOf(" ") + 1;
            List<String> terms = new ArrayList<String>(2);

            String allButLastWord = queryStr.substring(0, indexOfLastWord);
            if (StringUtils.isNotBlank(allButLastWord)) {
                terms.add(makeTermQuery(nonAcTermName, allButLastWord, true));
            }

            String lastWord = queryStr.substring(indexOfLastWord);
            if (StringUtils.isNotBlank(lastWord)) {
                terms.add(makeTermQuery(acTermName, lastWord, false));
            }

            acQueryStr = StringUtils.join(terms, " AND ");
        }

        log.debug("Tokenized name query string = " + acQueryStr);
        return acQueryStr;
    }

    private String makeTermQuery(String term, String queryStr, boolean mayContainWhitespace) {
        if (mayContainWhitespace) {
            queryStr = "\"" + escapeWhitespaceInQueryString(queryStr) + "\"";
        }
        return term + ":" + queryStr;
    }

    private String escapeWhitespaceInQueryString(String queryStr) {
        // The search engine wants whitespace to be escaped with a backslash
        return queryStr.replaceAll("\\s+", "\\\\ ");
    }

    public class SearchResult implements Comparable<SearchResult> {
        private String label;
        private String uri;

        SearchResult(String label, String uri) {
            this.label = label;
            this.uri = uri;
        }

        public String getLabel() {
            return label;
        }

        public String getUri() {
            return uri;
        }

        Map<String, String> toMap() {
            Map<String, String> map = new HashMap<String, String>();
            map.put("label", label);
            map.put("uri", uri);
            return map;
        }

        public int compareTo(SearchResult o) throws ClassCastException {
            if ( !(o instanceof SearchResult) ) {
                throw new ClassCastException("Error in SearchResult.compareTo(): expected SearchResult object.");
            }
            SearchResult sr = (SearchResult) o;
            return label.compareToIgnoreCase(sr.getLabel());
        }
    }

}
