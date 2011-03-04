/*
Copyright (c) 2011, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
 * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.TabEntitiesController.PageRecord;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;

/**
 * This servlet is for servicing JSON requests for Google Refine's
 * Reconciliation Service.
 * 
 * @author Eliza Chan (elc2013@med.cornell.edu)
 * 
 */
public class JSONReconcileServlet extends VitroHttpServlet {

	private static String VIVO_NAMESPACE = "http://vivo.med.cornell.edu/individual/";
	private static String QUERY_PARAMETER_NAME = "term";
	public static final int MAX_QUERY_LENGTH = 500;
	private static final Log log = LogFactory.getLog(JSONReconcileServlet.class.getName());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		VitroRequest vreq = new VitroRequest(req);

		try {
			if (vreq.getParameter("query") != null
					|| vreq.getParameter("queries") != null) {
				JSONObject qJson = getResult(vreq, req, resp);
				System.out.println("result: " + qJson.toString());
				String responseStr = (vreq.getParameter("callback") == null) ? qJson
						.toString() : vreq.getParameter("callback") + "("
						+ qJson.toString() + ")";
				resp.setContentType("application/json");
				ServletOutputStream out = resp.getOutputStream();
				out.print(responseStr);
			} else { // metadata
				JSONObject metaJson = getMetadata(req, resp);
				String callbackStr = (vreq.getParameter("callback") == null) ? ""
						: vreq.getParameter("callback");
				resp.setContentType("application/json");
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
			System.out.println();
			System.out.println("strArr: " + qStr + "\n");
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
								// System.out.println("before search: " + jsonLvl2.getString("query"));
								searchNoTypeMap.put(key, jsonLvl2);
							}
						}
					}
				}
			}
		} catch (JSONException ex) {
			System.err.println("JSONReconcileServlet JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet JSONException: "
					+ ex);
		}

		// Run index search
		JSONObject qJson = null;
		if (searchWithTypeMap.size() > 0) {
			qJson = runSearch(searchWithTypeMap, vreq);
		} else {
			qJson = runSearch(searchNoTypeMap, vreq);
		}
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
	private JSONObject getMetadata(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {
		JSONObject json = new JSONObject();
		try {
			json.put("name", "VIVO Reconciliation Service");
			json.put("identifierSpace", this.VIVO_NAMESPACE);
			json.put("schemaSpace", this.VIVO_NAMESPACE);

			JSONObject viewJson = new JSONObject();
			StringBuffer urlBuf = new StringBuffer();
			urlBuf.append("http://" + req.getServerName());
			if (req.getServerPort() == 8080) {
				urlBuf.append(":" + req.getServerPort());
			}
			if (req.getContextPath() != null) {
				urlBuf.append(req.getContextPath());
			}
			viewJson.put("url", urlBuf.toString() + "/individual?uri={{id}}");
			json.put("view", viewJson);

			HashMap<String, String> typeMap = new HashMap<String, String>();
			typeMap.put("http://vivoweb.org/ontology/core#Course", "Course");
			typeMap.put("http://vivoweb.org/ontology/core#Grant", "Grant");
			typeMap.put("http://vivoweb.org/ontology/core#GeographicRegion",
					"Location");
			typeMap.put("http://xmlns.com/foaf/0.1/Organization",
					"Organization");
			typeMap.put("http://xmlns.com/foaf/0.1/Person", "Person");
			typeMap.put("http://purl.org/ontology/bibo/Article", "Publication");

			JSONArray defaultTypesJsonArr = new JSONArray();
			for (Map.Entry<String, String> entry : typeMap.entrySet()) {
				String id = entry.getKey();
				String name = entry.getValue();
				JSONObject defaultTypesJson = new JSONObject();
				defaultTypesJson.put("id", id);
				defaultTypesJson.put("name", name);
				defaultTypesJsonArr.put(defaultTypesJson);
			}
			json.put("defaultTypes", defaultTypesJsonArr);
		} catch (JSONException ex) {
			throw new ServletException(
					"JSONReconcileServlet: Could not create metadata: " + ex);
		}
		return json;
	}

	private JSONObject runSearch(HashMap<String, JSONObject> currMap,
			VitroRequest vreq) throws ServletException {
		JSONObject qJson = new JSONObject();
		try {
			PortalFlag portalFlag = vreq.getPortalFlag();
			Analyzer analyzer = getAnalyzer(getServletContext());
			IndexSearcher searcherForRequest = LuceneIndexFactory
					.getIndexSearcher(getServletContext());

			for (Map.Entry<String, JSONObject> entry : currMap.entrySet()) {
				JSONObject resultAllJson = new JSONObject();
				String key = entry.getKey();
				JSONObject json = (JSONObject) entry.getValue();
				String queryVal = json.getString("query");

				// continue with properties list
				String searchType = null;
				int limit = 3; // default
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
						pvPair[0] = pid;
						pvPair[1] = v;
						propertiesList.add(pvPair);
					}
				}

				// begin search
				// System.out.println("queryVal: " + queryVal);
				JSONArray resultJsonArr = new JSONArray();
				Query query = getReconcileQuery(vreq, portalFlag, analyzer,
						queryVal, searchType, propertiesList);

				TopDocs topDocs = searcherForRequest.search(query, null, limit);
				if (topDocs != null && topDocs.scoreDocs != null) {
					int hitsLength = topDocs.scoreDocs.length;
					//System.out.println("hitsLength: " + hitsLength);
					if (hitsLength > 0) {
						for (int i = 0; i < topDocs.scoreDocs.length; i++) {
							JSONObject resultJson = new JSONObject();
							float score = topDocs.scoreDocs[i].score;
							resultJson.put("score", score);

							Document doc = searcherForRequest
									.doc(topDocs.scoreDocs[i].doc);
							String uri = doc.get(Entity2LuceneDoc.term.URI);
							IndividualDao iDao = vreq.getWebappDaoFactory()
									.getIndividualDao();
							Individual ind = iDao.getIndividualByURI(uri);
							if (ind != null) {
								String name = ind.getName();
								resultJson.put("id", uri);
								resultJson.put("name", name);
							}
							List fields = doc.getFields();
							JSONArray typesJsonArr = new JSONArray();
							for (int j = 0; j < fields.size(); j++) {
								Field field = (Field) fields.get(j);
								String fieldName = field.name();
								if ("type".equals(fieldName)) {
									// e.g.
									// http://vivoweb.org/ontology/core#FacultyMember
									String type = field.stringValue();
									int lastIndex2 = type.lastIndexOf('/') + 1;
									String typeName = type
											.substring(lastIndex2);
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
						}
					}
				}
				resultAllJson.put("result", resultJsonArr);
				qJson.put(key, resultAllJson);
			}

		} catch (JSONException ex) {
			System.err.println("JSONReconcileServlet JSONException: " + ex);
			throw new ServletException("JSONReconcileServlet JSONException: "
					+ ex);
		} catch (SearchException ex) {
			System.err.println("JSONReconcileServlet SearchException: " + ex);
			throw new ServletException("JSONReconcileServlet SearchException: "
					+ ex);
		} catch (IOException ex) {
			System.err.println("JSONReconcileServlet IOException: " + ex);
			throw new ServletException("JSONReconcileServlet IOException: "
					+ ex);
		}

		return qJson;
	}

	private Analyzer getAnalyzer(ServletContext servletContext)
			throws SearchException {
		Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
		if (obj == null || !(obj instanceof Analyzer))
			throw new SearchException("Could not get anlyzer");
		else
			return (Analyzer) obj;
	}

    private Query makeReconcileNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {

    	/* Original code
        String tokenizeParam = (String) request.getParameter("tokenize"); 
        boolean tokenize = "true".equals(tokenizeParam);
        
        // Note: Stemming is only relevant if we are tokenizing: an untokenized name
        // query will not be stemmed. So we don't look at the stem parameter until we get to
        // makeTokenizedNameQuery().
        if (tokenize) {
            return makeTokenizedNameQuery(querystr, analyzer, request);
        } else {
            return makeUntokenizedNameQuery(querystr);
        }
        */
    	
    	// modified code for reconciliation service
    	request.setAttribute("stem", true);
    	return makeTokenizedNameQuery(querystr, analyzer, request);
    }
	
    private Query makeTokenizedNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {
    	 
        String stemParam = (String) request.getParameter("stem"); 
        boolean stem = "true".equals(stemParam);
        String termName = stem ? VitroLuceneTermNames.NAME : VitroLuceneTermNames.NAMEUNSTEMMED;

        BooleanQuery boolQuery = new BooleanQuery();
        
        // Use the query parser to analyze the search term the same way the indexed text was analyzed.
        // For example, text is lowercased, and function words are stripped out.
        QueryParser parser = getQueryParser(termName, analyzer);
        
        // The wildcard query doesn't play well with stemming. Query term name:tales* doesn't match
        // "tales", which is indexed as "tale", while query term name:tales does. Obviously we need 
        // the wildcard for name:tal*, so the only way to get them all to match is use a disjunction 
        // of wildcard and non-wildcard queries. The query will look have only an implicit disjunction
        // operator: e.g., +(name:tales name:tales*)
        try {
            log.debug("Adding non-wildcard query for " + querystr);
            Query query = parser.parse(querystr);  
            boolQuery.add(query, BooleanClause.Occur.SHOULD);

            // Prevent ParseException here when adding * after a space.
            // If there's a space at the end, we don't need the wildcard query.
            if (! querystr.endsWith(" ")) {
                log.debug("Adding wildcard query for " + querystr);
                Query wildcardQuery = parser.parse(querystr + "*");            
                boolQuery.add(wildcardQuery, BooleanClause.Occur.SHOULD);
            }
            
            log.debug("Name query is: " + boolQuery.toString());
        } catch (ParseException e) {
            log.warn(e, e);
        }
        
        
        return boolQuery;
    }
	
    private Query makeUntokenizedNameQuery(String querystr) {
        
        querystr = querystr.toLowerCase();
        String termName = VitroLuceneTermNames.NAMELOWERCASE;
        BooleanQuery query = new BooleanQuery();
        log.debug("Adding wildcard query on unanalyzed name");
        query.add( 
                new WildcardQuery(new Term(termName, querystr + "*")),
                BooleanClause.Occur.MUST);   
        
        return query;
    }

    private QueryParser getQueryParser(String searchField, Analyzer analyzer){
        // searchField indicates which field to search against when there is no term
        // indicated in the query string.
        // The analyzer is needed so that we use the same analyzer on the search queries as
        // was used on the text that was indexed.
        QueryParser qp = new QueryParser(searchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        return qp;
    }

    private Query getReconcileQuery(VitroRequest request, PortalFlag portalState,
    		Analyzer analyzer, String querystr, String typeParam, ArrayList<String[]> propertiesList) throws SearchException{

    	// System.out.println("querystr: " + querystr);
    	Query query = null;
    	try {
    		if( querystr == null){
    			log.error("There was no Parameter '"+ QUERY_PARAMETER_NAME            
    					+"' in the request.");                
    			return null;
    		}else if( querystr.length() > MAX_QUERY_LENGTH ){
    			log.debug("The search was too long. The maximum " +
    					"query length is " + MAX_QUERY_LENGTH );
    			return null;
    		} 

    		
    		query = makeReconcileNameQuery(querystr, analyzer, request);
    		

    		// filter by type
    		if (typeParam != null) {
    			System.out.println("typeParam: " + typeParam);
    			BooleanQuery boolQuery = new BooleanQuery(); 
    			boolQuery.add(  new TermQuery(
    					new Term(VitroLuceneTermNames.RDFTYPE, 
    							typeParam)),
    							BooleanClause.Occur.MUST);
    			boolQuery.add(query, BooleanClause.Occur.MUST);
    			query = boolQuery;
    		}

			// if propertiesList has elements, add extra queries to query
			Iterator<String[]> it = propertiesList.iterator();
			while (it.hasNext()) {
				String[] pvPair = it.next();
				System.out.println("property val: " + pvPair[0] + " " + pvPair[1]);
				Query extraQuery = makeReconcileNameQuery(pvPair[1], analyzer, request);
				if (!"".equals(pvPair[0]) && pvPair[0] != null) {
					BooleanQuery boolQuery = new BooleanQuery();
					boolQuery.add(new TermQuery(new Term(
							VitroLuceneTermNames.RDFTYPE, pvPair[0])),
							BooleanClause.Occur.MUST);
					boolQuery.add(extraQuery, BooleanClause.Occur.MUST);
					extraQuery = boolQuery;
				}				
				((BooleanQuery)query).add(extraQuery, BooleanClause.Occur.MUST);
			}
		} catch (Exception ex) {
			throw new SearchException(ex.getMessage());
		}

    	return query;
    }

}
