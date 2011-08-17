/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * This servlet is for servicing JSON requests from Google Refine's
 * Reconciliation Service.
 * 
 * @author Eliza Chan (elc2013@med.cornell.edu)
 * 
 */
public class JSONReconcileServlet extends VitroHttpServlet {

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
		VitroRequest vreq = new VitroRequest(req);

		try {
			if (vreq.getParameter("query") != null
					|| vreq.getParameter("queries") != null) {
				JSONObject qJson = getResult(vreq, req, resp);
				log.debug("result: " + qJson.toString());
				String responseStr = (vreq.getParameter("callback") == null) ? qJson
						.toString() : vreq.getParameter("callback") + "("
						+ qJson.toString() + ")";
				// System.out.println("JSONReconcileServlet result: " + responseStr);
				ServletOutputStream out = resp.getOutputStream();
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
				JSONObject metaJson = getMetadata(req, resp, defaultNamespace, defaultTypeList, serverName, serverPort);
				String callbackStr = (vreq.getParameter("callback") == null) ? ""
						: vreq.getParameter("callback");
				ServletOutputStream out = resp.getOutputStream();
				out.print(callbackStr + "(" + metaJson.toString() + ")");
			}
		} catch (Exception ex) {
			log.warn(ex, ex);
		}
	}

	private JSONObject getResult(VitroRequest vreq, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {

		HashMap<String, JSONObject> searchWithTypeMap = new HashMap<String, JSONObject>();
		HashMap<String, JSONObject> searchNoTypeMap = new HashMap<String, JSONObject>();
		ArrayList<String> queries = new ArrayList<String>();
		Object qObj = vreq.getParameter("queries");

		if (qObj == null) {
			qObj = vreq.getParameter("query");
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
			for (int i = 0; i < queries.size(); i++) {
				String queryStr = (String) queries.get(i);
				JSONObject json = new JSONObject(queryStr);

				if (json.has("query")) { // single query
					if (json.has("type")) {
						searchWithTypeMap.put("query", json);
					} else {
						// user did not specify a type
						searchNoTypeMap.put("query", json);
					}
				} else { // multiple queries
					for (Iterator<String> iter = json.keys(); iter.hasNext();) {
						ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
						String key = (String) iter.next();
						Object obj = json.get(key);
						JSONObject jsonLvl2 = (JSONObject) obj;
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
		} catch (JSONException ex) {
			log.error("JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet JSONException: "
					+ ex);
		}

		// Run index search
		JSONObject qJson = null;
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
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 */
	protected JSONObject getMetadata(HttpServletRequest req, HttpServletResponse resp, String defaultNamespace,
			String defaultTypeList, String serverName, int serverPort) throws ServletException {

		JSONObject json = new JSONObject();
		try {
			json.put("name", "VIVO Reconciliation Service");
			if (defaultNamespace != null) {
				json.put("identifierSpace", defaultNamespace);
				json.put("schemaSpace", defaultNamespace);
			}
			JSONObject viewJson = new JSONObject();
			StringBuffer urlBuf = new StringBuffer();
			urlBuf.append("http://" + serverName);
			if (serverPort == 8080) {
				urlBuf.append(":" + serverPort);
			}
			if (req.getContextPath() != null) {
				urlBuf.append(req.getContextPath());
			}
			viewJson.put("url", urlBuf.toString() + "/individual?uri={{id}}");
			json.put("view", viewJson);

			// parse defaultTypeList from deploy.properties
			if (defaultTypeList != null) {
				String[] splitList = defaultTypeList.split(";");
				String[][] idNameArray = new String[splitList.length][splitList.length];
				for(int i = 0; i<splitList.length; i++) {
					idNameArray[i] = splitList[i].split(",");
				}
				// process and add to json defaultTypes
				JSONArray defaultTypesJsonArr = new JSONArray();
				for (int i = 0; i<idNameArray.length; i++) {
					JSONObject defaultTypesJson = new JSONObject();
					defaultTypesJson.put("id", idNameArray[i][0].trim());
					defaultTypesJson.put("name", idNameArray[i][1].trim());
					defaultTypesJsonArr.put(defaultTypesJson);
				}
				json.put("defaultTypes", defaultTypesJsonArr);
			}
		} catch (JSONException ex) {
			throw new ServletException(
					"JSONReconcileServlet: Could not create metadata: " + ex);
		}

		return json;
	}

	private JSONObject runSearch(HashMap<String, JSONObject> currMap) throws ServletException {
		JSONObject qJson = new JSONObject();
		
		try {

			for (Map.Entry<String, JSONObject> entry : currMap.entrySet()) {
				JSONObject resultAllJson = new JSONObject();
				String key = entry.getKey();
				JSONObject json = (JSONObject) entry.getValue();
				String queryVal = json.getString("query");

				// System.out.println("query: " + json.toString());
				
				// continue with properties list
				String searchType = null;
				int limit = 10; // default
				String typeStrict = "should"; // default
				ArrayList<String[]> propertiesList = new ArrayList<String[]>();

				if (json.has("type")) {
					searchType = json.getString("type");
				}
				if (json.has("limit")) {
					limit = json.getInt("limit");
				}
				if (json.has("type_strict")) { // Not sure what this variable
												// represents. Skip for now.
					typeStrict = json.getString("type_strict");
				}
				if (json.has("properties")) {
					JSONArray properties = json.getJSONArray("properties");
					for (int i = 0; i < properties.length(); i++) {
						String[] pvPair = new String[2];
						JSONObject jsonProperty = properties.getJSONObject(i);
						String pid = jsonProperty.getString("pid");
						String v = jsonProperty.getString("v");
						if (pid != null && v != null) {
							pvPair[0] = pid;
							pvPair[1] = v;
							propertiesList.add(pvPair);
						}
					}
				}

				// begin search
				JSONArray resultJsonArr = new JSONArray();

				// Solr
	            SolrQuery query = getQuery(queryVal, searchType, limit, propertiesList);
	            QueryResponse queryResponse = null;
	            if (query != null) {
	            	SolrServer solr = SolrSetup.getSolrServer(getServletContext());
	            	queryResponse = solr.query(query);
	            } else {
	            	log.error("Query for a search was null");                
	            }

	            SolrDocumentList docs = null;
	            if (queryResponse != null) {
	            	docs = queryResponse.getResults();
	            } else {
	            	log.error("Query response for a search was null");                
	            }

	            if (docs != null) {
	            	
	                List<SearchResult> results = new ArrayList<SearchResult>();
	                for (SolrDocument doc : docs) {
	                    try {                         
	                        String uri = doc.get(VitroSearchTermNames.URI).toString();
	                        // RY 7/1/2011
	                        // Comment was: VitroSearchTermNames.NAME_RAW is a multivalued field, so doc.get() returns a list.
	                        // Changed to: VitroSearchTermNames.NAME_RAW is a multivalued field, so doc.get() could return a list
	                        // But in fact: I'm no longer seeing any lists returned for individuals with multiple labels. Not sure
	                        // if this is new behavior or what. ???
	                        Object nameRaw = doc.get(VitroSearchTermNames.NAME_RAW);
	                        String name = null;
	                        if (nameRaw instanceof List<?>) {
	                            @SuppressWarnings("unchecked")
	                            List<String> nameRawList = (List<String>) nameRaw;
	                            name = nameRawList.get(0);
	                        } else {
	                            name = (String) nameRaw;
	                        }
	                        
	                        SearchResult result = new SearchResult(name, uri);
	                        
	                        // populate result for Google Refine
							JSONObject resultJson = new JSONObject();
							resultJson.put("score", doc.getFieldValue("score"));
							String modUri = result.getUri().replace("#", "%23");
							resultJson.put("id", modUri);
							resultJson.put("name", result.getLabel());
							
							Collection<Object> rdfTypes = doc.getFieldValues(VitroSearchTermNames.RDFTYPE);
							JSONArray typesJsonArr = new JSONArray();
							if (rdfTypes != null) {

								for (Object rdfType : rdfTypes) {

									// e.g.
									// http://aims.fao.org/aos/geopolitical.owl#area
									String type = (String) rdfType;
									int lastIndex2 = type.lastIndexOf('/') + 1;
									String typeName = type.substring(lastIndex2);
									typeName = typeName.replace("#", ":");
									JSONObject typesJson = new JSONObject();
									typesJson.put("id", type);
									typesJson.put("name", typeName);
									typesJsonArr.put(typesJson);

								}
							}

							resultJson.put("type", typesJsonArr);
							resultJson.put("match", "false");
							resultJsonArr.put(resultJson);

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

		} catch (JSONException ex) {
			log.error("JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet JSONException: "
					+ ex);
		} catch (SolrServerException ex) {
			log.error("JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet SolrServerException: "
					+ ex);
		}

		return qJson;
	}

    protected SolrQuery getQuery(String queryStr, String searchType, int limit, ArrayList<String[]> propertiesList) {
        
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
        ///SolrQuery query = new SolrQuery();
        
        /// test
        SolrQuery query = new SolrQuery(queryStr.toLowerCase());

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
        query.setFields(VitroSearchTermNames.NAME_RAW, VitroSearchTermNames.URI, "*", "score"); // fields to retrieve
 
		// if propertiesList has elements, add extra queries to query
		Iterator<String[]> it = propertiesList.iterator();
		while (it.hasNext()) {
			String[] pvPair = it.next();
			query.addFilterQuery(tokenizeNameQuery(pvPair[1]), VitroSearchTermNames.RDFTYPE + ":\"" + pvPair[0] + "\"");
		}       

        // Can't sort on multivalued field, so we sort the results in Java when we get them.
        // query.setSortField(VitroSearchTermNames.NAME_LOWERCASE, SolrQuery.ORDER.asc);
        
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
        // Solr wants whitespace to be escaped with a backslash
        return queryStr.replaceAll("\\s+", "\\\\ ");
    }

    public class SearchResult implements Comparable<Object> {
        private String label;
        private String uri;
        
        SearchResult(String label, String uri) {
            this.label = label;
            this.uri = uri;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getJsonLabel() {
            return JSONObject.quote(label);
        }
        
        public String getUri() {
            return uri;
        }
        
        public String getJsonUri() {
            return JSONObject.quote(uri);
        }
        
        Map<String, String> toMap() {
            Map<String, String> map = new HashMap<String, String>();
            map.put("label", label);
            map.put("uri", uri);
            return map;
        }

        public int compareTo(Object o) throws ClassCastException {
            if ( !(o instanceof SearchResult) ) {
                throw new ClassCastException("Error in SearchResult.compareTo(): expected SearchResult object.");
            }
            SearchResult sr = (SearchResult) o;
            return label.compareToIgnoreCase(sr.getLabel());
        }
    }

}
