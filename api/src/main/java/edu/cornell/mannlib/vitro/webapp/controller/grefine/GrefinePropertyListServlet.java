/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.grefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This servlet is for servicing Google Refine's
 * "Add columns from VIVO" requests.
 *
 * @author Eliza Chan (elc2013@med.cornell.edu)
 *
 */
@WebServlet(name = "Google Refine Property List Service", urlPatterns = {"/get_properties_of_type"} )
public class GrefinePropertyListServlet extends VitroHttpServlet {

	private int MAXDEPTH = 7;
	public static final int MAX_QUERY_LENGTH = 500;
	private static final long serialVersionUID = 1L;
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


				VClassDao vcDao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
				DataPropertyDao dao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyDao();
				String topUri = vreq.getParameter("type");
				VClass topClass = vcDao.getVClassByURI(topUri);
				HashSet<String> propURIs = new HashSet<String>();
				HashMap<VClass, List<DataProperty>> classPropertiesMap =
					populateClassPropertiesMap(vcDao, dao, topUri, propURIs);


				// Construct json String
				ObjectNode completeJson = JsonNodeFactory.instance.objectNode();
				ArrayNode propertiesJsonArr = JsonNodeFactory.instance.arrayNode();
				if (classPropertiesMap.size() > 0) {
					for (VClass vc : classPropertiesMap.keySet()) { // add results to schema
						//System.out.println("vc uri: " + vc.getURI());
						//System.out.println("vc name: " + vc.getName());

						ArrayList<DataProperty> vcProps = (ArrayList<DataProperty>) classPropertiesMap.get(vc);
						for (DataProperty prop : vcProps) {
							String nameStr = prop.getPublicName() == null ? prop.getName() : prop.getPublicName();
							//System.out.println("--- uri: " + prop.getURI());
							//System.out.println("--- name: " + nameStr);
							// top level
							ObjectNode propertiesItemJson = JsonNodeFactory.instance.objectNode();
							ObjectNode rootSchemaJson = JsonNodeFactory.instance.objectNode();
							rootSchemaJson.put("id", vc.getURI());
							rootSchemaJson.put("name", vc.getName());
							rootSchemaJson.put("alias", JsonNodeFactory.instance.arrayNode());
							propertiesItemJson.put("schema", rootSchemaJson);
							// second level
							propertiesItemJson.put("id", prop.getURI());
							propertiesItemJson.put("name", nameStr);
							propertiesItemJson.put("alias", JsonNodeFactory.instance.arrayNode());

							ObjectNode expectsJson = JsonNodeFactory.instance.objectNode();
							expectsJson.put("id", prop.getURI());
							expectsJson.put("name", nameStr);
							expectsJson.put("alias", JsonNodeFactory.instance.arrayNode());
							propertiesItemJson.put("expects", expectsJson);

							propertiesJsonArr.add(propertiesItemJson);
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
				                addChildren(vcDao, vreq.getUnfilteredWebappDaoFactory(), root, lvl2ChildClasses, 0, ontologyUri);
				                lvl2Classes.addAll(lvl2ChildClasses);
			                }
			            }
			        }
				}


				for (VClass lvl2Class: lvl2Classes) {
					HashMap<VClass, List<DataProperty>> lvl2ClassPropertiesMap =
						populateClassPropertiesMap(vcDao, dao, lvl2Class.getURI(), propURIs);
					if (lvl2ClassPropertiesMap.size() > 0) {
						for (VClass vc : lvl2ClassPropertiesMap.keySet()) { // add results to schema
							ArrayList<DataProperty> vcProps = (ArrayList<DataProperty>) lvl2ClassPropertiesMap.get(vc);
							for (DataProperty prop : vcProps) {
								String nameStr = prop.getPublicName() == null ? prop.getName() : prop.getPublicName();
								// top level
								ObjectNode propertiesItemJson = JsonNodeFactory.instance.objectNode();

								ObjectNode rootSchemaJson = JsonNodeFactory.instance.objectNode();
								rootSchemaJson.put("id", topClass.getURI());
								rootSchemaJson.put("name", topClass.getName());
								rootSchemaJson.put("alias", JsonNodeFactory.instance.arrayNode());
								propertiesItemJson.put("schema", rootSchemaJson);

								// second level
								propertiesItemJson.put("id", vc.getURI());
								propertiesItemJson.put("name", vc.getName());
								propertiesItemJson.put("alias", JsonNodeFactory.instance.arrayNode());

								propertiesItemJson.put("id2", prop.getURI());
								propertiesItemJson.put("name2", nameStr);
								propertiesItemJson.put("alias2", JsonNodeFactory.instance.arrayNode());

								ObjectNode expectsJson = JsonNodeFactory.instance.objectNode();
								expectsJson.put("id", prop.getURI());
								expectsJson.put("name", nameStr);
								expectsJson.put("alias", JsonNodeFactory.instance.arrayNode());
								propertiesItemJson.put("expects", expectsJson);

								propertiesJsonArr.add(propertiesItemJson);
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
		for (DataProperty dp : dataProps) {
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
        		String nameStr = prop.getPublicName()==null ? prop.getName() : prop.getPublicName();
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
				for (Object childURIstr : childURIstrs) {
					String URIstr = (String) childURIstr;
					try {
						VClass child = (VClass) vcDao.getVClassByURI(URIstr);
						if (!child.getURI().equals(OWL.Nothing.getURI())) {
							childClasses.add(child);
						}
					} catch (Exception e) {
					}
				}
	            Collections.sort(childClasses);
				for (Object childClass : childClasses) {
					VClass child = (VClass) childClass;
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

}

