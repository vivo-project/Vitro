/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;

/**
 * This servlet is for servicing Google Refine's
 * "Add columns from VIVO" requests.
 * 
 * @author Eliza Chan (elc2013@med.cornell.edu)
 * 
 */
public class GrefinePropertyListServlet extends VitroHttpServlet {

	private int MAXDEPTH = 7;
	private int NUM_COLS = 9;
	private static String QUERY_PARAMETER_NAME = "term";
	public static final int MAX_QUERY_LENGTH = 500;
	private static final Log log = LogFactory.getLog(GrefinePropertyListServlet.class.getName());
	

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

				String callbackStr = (vreq.getParameter("callback") == null) ? ""
						: vreq.getParameter("callback");
				ServletOutputStream out = resp.getOutputStream();

		
				// *******
				// methodology adopted from DatatypePropertiesListingController and ClassHierarchyListingController
				// *******
				VClassDao vcDao = vreq.getFullWebappDaoFactory().getVClassDao();
				DataPropertyDao dao = vreq.getFullWebappDaoFactory().getDataPropertyDao();
				String topUri = vreq.getParameter("type");
				VClass topClass = vcDao.getVClassByURI(topUri);
				HashSet<String> propURIs = new HashSet<String>();
				HashMap<VClass, List<DataProperty>> classPropertiesMap = 
					populateClassPropertiesMap(vcDao, dao, topUri, propURIs);
				

				// Construct json String
				JSONObject completeJson = new JSONObject();
				JSONArray propertiesJsonArr = new JSONArray();
				if (classPropertiesMap.size() > 0) {
					for (Iterator<VClass> iter = classPropertiesMap.keySet().iterator(); iter.hasNext();) { // add results to schema
						VClass vc = (VClass) iter.next();
						System.out.println("vc uri: " + vc.getURI());
						System.out.println("vc name: " + vc.getName());	

						ArrayList<DataProperty> vcProps = (ArrayList<DataProperty>)classPropertiesMap.get(vc);
						for (DataProperty prop: vcProps) {
							String nameStr = prop.getPublicName()==null ? prop.getName()==null ? null : prop.getName() : prop.getPublicName();
								System.out.println("--- uri: " + prop.getURI());
								System.out.println("--- name: " + nameStr);
					        	// top level
								JSONObject propertiesItemJson = new JSONObject();
								JSONObject rootSchemaJson = new JSONObject();
								rootSchemaJson.put("id", vc.getURI());
								rootSchemaJson.put("name", vc.getName());
								rootSchemaJson.put("alias", new JSONArray());
								propertiesItemJson.put("schema", rootSchemaJson);
								// second level
								propertiesItemJson.put("id", prop.getURI());
								propertiesItemJson.put("name", nameStr);
								propertiesItemJson.put("alias", new JSONArray());

								JSONObject expectsJson = new JSONObject();
								expectsJson.put("id", prop.getURI());
								expectsJson.put("name", nameStr);
								expectsJson.put("alias", new JSONArray());
								propertiesItemJson.put("expects", expectsJson);
								
								propertiesJsonArr.put(propertiesItemJson);
						}
					}
				}
	            

				// get data properties from subclasses
				List<VClass> lvl2Classes = new ArrayList<VClass>();
				List roots = null;
				String requestType = vreq.getParameter("type");
				if (requestType != null) {
			       	roots = new LinkedList<VClass>();
		        	roots.add(vcDao.getVClassByURI(requestType));
				}

				if (roots != null) {
					String ontologyUri = null;
					Collections.sort(roots);
			        Iterator rootIt = roots.iterator();
			        if (rootIt.hasNext()) {
			            while (rootIt.hasNext()) {
			                VClass root = (VClass) rootIt.next();
				            if (root != null) {
				            	List<VClass> lvl2ChildClasses = new ArrayList<VClass>();
				                addChildren(vcDao, vreq.getFullWebappDaoFactory(), root, lvl2ChildClasses, 0, ontologyUri);
				                lvl2Classes.addAll(lvl2ChildClasses);
			                }
			            }
			        }
				}
				

				for (VClass lvl2Class: lvl2Classes) {
					HashMap<VClass, List<DataProperty>> lvl2ClassPropertiesMap = 
						populateClassPropertiesMap(vcDao, dao, lvl2Class.getURI(), propURIs);	
					if (lvl2ClassPropertiesMap.size() > 0) {
						for (Iterator<VClass> iter = lvl2ClassPropertiesMap.keySet().iterator(); iter.hasNext();) { // add results to schema
							VClass vc = (VClass) iter.next();
							ArrayList<DataProperty> vcProps = (ArrayList<DataProperty>)lvl2ClassPropertiesMap.get(vc);
							for (DataProperty prop: vcProps) {
								String nameStr = prop.getPublicName()==null ? prop.getName()==null ? null : prop.getName() : prop.getPublicName();
						        	// top level
									JSONObject propertiesItemJson = new JSONObject();
									
									JSONObject rootSchemaJson = new JSONObject();
									rootSchemaJson.put("id", topClass.getURI());
									rootSchemaJson.put("name", topClass.getName());
									rootSchemaJson.put("alias", new JSONArray());
									propertiesItemJson.put("schema", rootSchemaJson);

									// second level
									propertiesItemJson.put("id", vc.getURI());
									propertiesItemJson.put("name", vc.getName());
									propertiesItemJson.put("alias", new JSONArray());
																		
									propertiesItemJson.put("id2", prop.getURI());
									propertiesItemJson.put("name2", nameStr);
									propertiesItemJson.put("alias2", new JSONArray());
									
									JSONObject expectsJson = new JSONObject();
									expectsJson.put("id", prop.getURI());
									expectsJson.put("name", nameStr);
									expectsJson.put("alias", new JSONArray());
									propertiesItemJson.put("expects", expectsJson);
									
									propertiesJsonArr.put(propertiesItemJson);
							}
						}
						
					}
				}

				completeJson.put("properties", propertiesJsonArr);
				out.print(callbackStr + "(" + completeJson.toString() + ")");
				


		} catch (Exception ex) {
			log.warn(ex, ex);
		}
	}
	

	private HashMap<VClass, List<DataProperty>> populateClassPropertiesMap (
			VClassDao vcDao, 
			DataPropertyDao dao, 
			String uri, 
			HashSet<String> propURIs) {

		HashMap<VClass, List<DataProperty>> classPropertiesMap = new HashMap<VClass, List<DataProperty>>();
		List<DataProperty> props = new ArrayList<DataProperty>();
		VClass topVc = vcDao.getVClassByURI(uri);
	        Collection <DataProperty> dataProps = dao.getDataPropertiesForVClass(uri);
	        Iterator<DataProperty> dataPropIt = dataProps.iterator();
	        while (dataPropIt.hasNext()) {
	            DataProperty dp = dataPropIt.next();
	            if (!(propURIs.contains(dp.getURI()))) {
	                propURIs.add(dp.getURI());
	                DataProperty prop = dao.getDataPropertyByURI(dp.getURI());
	                if (prop != null) {
	                    props.add(prop);
	                }
	            }
	        }
        
    	
        if (props.size() > 0) {

        	Collections.sort(props);
        	for (DataProperty prop: props) {
        		String nameStr = prop.getPublicName()==null ? prop.getName()==null ? null : prop.getName() : prop.getPublicName();
				if (nameStr != null) {
            		if (prop.getDomainClassURI() != null) {
            			VClass vc = vcDao.getVClassByURI(prop.getDomainClassURI());
            			if (classPropertiesMap.get(vc) != null) {
            				ArrayList<DataProperty> existingList = (ArrayList<DataProperty>)classPropertiesMap.get(vc);
            				existingList.add(prop);
            			} else {
            				ArrayList<DataProperty> newList = new ArrayList<DataProperty>();
            				newList.add(prop);
            				classPropertiesMap.put(vc, newList);
            			}

            		} else { // some properties have no domain, belong to top vc by default
            			if (classPropertiesMap.get(topVc) != null) {
            				ArrayList<DataProperty> existingList = (ArrayList<DataProperty>)classPropertiesMap.get(topVc);
            				existingList.add(prop);
            			} else {
            				ArrayList<DataProperty> newList = new ArrayList<DataProperty>();
            				newList.add(prop);
            				classPropertiesMap.put(topVc, newList);
            			}
            		}							
				}
        	}
        }
        return classPropertiesMap;
	}

	    private void addChildren(VClassDao vcDao, WebappDaoFactory wadf, VClass parent, List<VClass> list, int position, String ontologyUri) {
	    	List<VClass> rowElts = addVClassDataToResultsList(wadf, parent, position, ontologyUri);
	    	int childShift = (rowElts.size() > 0) ? 1 : 0;  // if addVClassDataToResultsList filtered out the result, don't shift the children over 
	    	list.addAll(rowElts);
	        List childURIstrs = vcDao.getSubClassURIs(parent.getURI());
	        if ((childURIstrs.size()>0) && position<MAXDEPTH) {
	            List childClasses = new ArrayList();
	            Iterator childURIstrIt = childURIstrs.iterator();
	            while (childURIstrIt.hasNext()) {
	                String URIstr = (String) childURIstrIt.next();
	                try {
		                VClass child = (VClass) vcDao.getVClassByURI(URIstr);
		                if (!child.getURI().equals(OWL.Nothing.getURI())) {
		                	childClasses.add(child);
		                }
	                } catch (Exception e) {}
	            }
	            Collections.sort(childClasses);
	            Iterator childClassIt = childClasses.iterator();
	            while (childClassIt.hasNext()) {
	                VClass child = (VClass) childClassIt.next();
	                addChildren(vcDao, wadf, child, list, position + childShift, ontologyUri);
	            }

	        }

	    }

	    private List<VClass> addVClassDataToResultsList(WebappDaoFactory wadf, VClass vcw, int position, String ontologyUri) {
	    	List<VClass> results = new ArrayList<VClass>();
	        if (ontologyUri == null || ( (vcw.getNamespace()!=null) && (vcw.getNamespace().equals(ontologyUri)) ) ) {
	        	results.add(vcw);

/*
	        	for (int i=0; i<position; i++) {
	                results.add("@@entities");
	            }
	            if (position==0)
	                results.add("XX"); // column 1
	            Integer numCols = (NUM_COLS-1)-position;

	            try {
	                numCols = addColToResults(((vcw.getLocalNameWithPrefix() == null) ? "" : "<a href=\"vclassEdit?uri="+URLEncoder.encode(vcw.getURI(),"UTF-8")+"\">"+vcw.getLocalNameWithPrefix()+"</a>"), results, numCols);
	            } catch (Exception e) {
	                numCols = addColToResults(((vcw.getLocalNameWithPrefix() == null) ? "" : vcw.getLocalNameWithPrefix()), results, numCols); // column 2
	            }
	            numCols = addColToResults(((vcw.getShortDef() == null) ? "" : vcw.getShortDef()), results, numCols); // column 3
	            numCols = addColToResults(((vcw.getExample() == null) ? "" : vcw.getExample()), results, numCols); // column 4
	            
	            // Get group name if it exists
	            VClassGroupDao groupDao= wadf.getVClassGroupDao();
	            String groupURI = vcw.getGroupURI();
	            String groupName = null;
	            VClassGroup classGroup = null;
	            if(groupURI != null) { 
	            	classGroup = groupDao.getGroupByURI(groupURI);
	            	if (classGroup != null) {
	            		groupName = classGroup.getPublicName();
	            	}
	            }
	            numCols = addColToResults(((groupName == null) ? "" : groupName), results, numCols); // column 5
	      
	            // Get ontology name
				String ontName = null;
				try {
	            	OntologyDao ontDao = wadf.getOntologyDao();
	            	Ontology ont = ontDao.getOntologyByURI(vcw.getNamespace());
	            	ontName = ont.getName();
				} catch (Exception e) {}
	            numCols = addColToResults(((ontName == null) ? "" : ontName), results, numCols); // column 6
	            
	            numCols = addColToResults(vcw.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : vcw.getHiddenFromDisplayBelowRoleLevel().getShorthand(), results, numCols); // column 7
	            numCols = addColToResults(vcw.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : vcw.getProhibitedFromUpdateBelowRoleLevel().getShorthand(), results, numCols); // column 8

	            results.add("XX"); // column 9
*/
	        }
	        return results;
	    }

	    private Integer addColToResults (String value, List results, Integer colIndex) {
	        if (colIndex>0) {
	            results.add(value);
	        }
	        return colIndex-1;
	    }

	protected JSONObject getResult(VitroRequest vreq, HttpServletRequest req,
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

	private JSONObject runSearch(HashMap<String, JSONObject> currMap,
			VitroRequest vreq) throws ServletException {
		JSONObject qJson = new JSONObject();
		try {
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
				JSONArray resultJsonArr = new JSONArray();
				Query query = getReconcileQuery(vreq, analyzer,
						queryVal, searchType, propertiesList);

				TopDocs topDocs = searcherForRequest.search(query, null, limit);
				if (topDocs != null && topDocs.scoreDocs != null) {
					int hitsLength = topDocs.scoreDocs.length;
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
								// encode # to %23
								String modUri = uri.replace("#", "%23");
								resultJson.put("id", modUri);
								resultJson.put("name", name);
							}
							List fields = doc.getFields();
							JSONArray typesJsonArr = new JSONArray();
							for (int j = 0; j < fields.size(); j++) {
								Field field = (Field) fields.get(j);
								String fieldName = field.name();
								if ("type".equals(fieldName)) {
									// e.g. http://aims.fao.org/aos/geopolitical.owl#area
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
        String termName = stem ? VitroLuceneTermNames.NAME_STEMMED : VitroLuceneTermNames.NAME_UNSTEMMED;

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
        String termName = VitroLuceneTermNames.NAME_LOWERCASE;
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

    private Query getReconcileQuery(VitroRequest request, Analyzer analyzer, 
    				String querystr, String typeParam, ArrayList<String[]> propertiesList) throws SearchException{

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

