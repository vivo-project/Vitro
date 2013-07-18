/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class ShowDataPropertyHierarchyController extends FreemarkerHttpServlet {

	private static final Log log = LogFactory.getLog(ShowDataPropertyHierarchyController.class.getName());
	
    private static final String TEMPLATE_NAME = "siteAdmin-objectPropHierarchy.ftl";
    private int MAXDEPTH = 5;

    private DataPropertyDao dpDao = null;
    private VClassDao vcDao = null;
    private PropertyGroupDao pgDao = null;
    private DatatypeDao dDao = null;

    private int previous_posn = 0;

    @Override
	protected Actions requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTIONS;
	}
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            String displayOption = "";
            
            if ( vreq.getParameter("displayOption") != null ) {
                displayOption = vreq.getParameter("displayOption");
            }
            else {
                displayOption = "hierarchy";
            }
            body.put("displayOption", displayOption);
            
            if ( displayOption.equals("all") ) {
                body.put("pageTitle", "All Data Properties");
            }
            else {
                body.put("pageTitle", "Data Property Hierarchy");
            }
            
            body.put("propertyType", "data");
            
            dpDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getDataPropertyDao();
            vcDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getVClassDao();
            pgDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getPropertyGroupDao();
            dDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getDatatypeDao();

            String json = new String();

            String ontologyUri = vreq.getParameter("ontologyUri");
            String startPropertyUri = vreq.getParameter("propertyUri");

            List<DataProperty> roots = null;

            if (startPropertyUri != null) {
        	    roots = new LinkedList<DataProperty>();
        	    roots.add(dpDao.getDataPropertyByURI(startPropertyUri));
            } else {
                roots = dpDao.getRootDataProperties();
                if (roots!=null){
                    Collections.sort(roots);
                }
            }

            int counter = 0;

            if (roots!=null) {
                Iterator<DataProperty> rootIt = roots.iterator();
                if (!rootIt.hasNext()) {
                    DataProperty dp = new DataProperty();
                    dp.setURI(ontologyUri+"fake");
                    String notFoundMessage = "<strong>No data properties found.</strong>"; 
                    dp.setName(notFoundMessage);
                    dp.setName(notFoundMessage);
                    json += addDataPropertyDataToResultsList(dp, 0, ontologyUri, counter);
                } else {
                    while (rootIt.hasNext()) {
                        DataProperty root = rootIt.next();
                        if ( (ontologyUri==null) || ( (ontologyUri!=null) && (root.getNamespace()!=null) && (ontologyUri.equals(root.getNamespace())) ) ) {
                    	    json += addChildren(root, 0, ontologyUri, counter);
                    	    counter += 1;
                	    }
                    }	
                    int length = json.length();
                    if ( length > 0 ) {
                        json += " }"; 
                    }
                }
            }

            body.put("jsonTree",json);
        
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

    private String addChildren(DataProperty parent, int position, String ontologyUri, int counter) {
    	if (parent == null) {
    		return "";
    	}
        String details = addDataPropertyDataToResultsList(parent, position, ontologyUri, counter);
        int length = details.length();
        String leaves = "";
        leaves += details;
        List childURIstrs = dpDao.getSubPropertyURIs(parent.getURI());
        if ((childURIstrs.size()>0) && position<MAXDEPTH) {
            List childProps = new ArrayList();
            Iterator childURIstrIt = childURIstrs.iterator();
            while (childURIstrIt.hasNext()) {
                String URIstr = (String) childURIstrIt.next();
                DataProperty child = (DataProperty) dpDao.getDataPropertyByURI(URIstr);
                childProps.add(child);
            }
            Collections.sort(childProps);
            Iterator childPropIt = childProps.iterator();
            while (childPropIt.hasNext()) {
                DataProperty child = (DataProperty) childPropIt.next();
                leaves += addChildren(child, position+1, ontologyUri, counter);
                if (!childPropIt.hasNext()) {
                    if ( ontologyUri == null ) {
                        leaves += " }] ";
                    }
                    else if ( ontologyUri != null && length > 0 ) {
                        // need this for when we show the classes associated with an ontology
                        String ending = leaves.substring(leaves.length() - 2, leaves.length());
                        if ( ending.equals("] ") ) {
                            leaves += "}]";
                        }
                        else if  ( ending.equals(" [") ){
                            leaves += "] ";
                        }
                        else {
                            leaves += "}]";
                        }
                    }
                }
            }
        }
        else {
            if ( ontologyUri == null ) {
                 leaves += "] ";
            }
            else if ( ontologyUri != null && length > 0 ) {
                 leaves += "] ";
            }
        }
        return leaves;
    }

    private String addDataPropertyDataToResultsList(DataProperty dp, int position, String ontologyUri, int counter) {
        String tempString = "";
        if (dp == null) {
        	return tempString;
        }
        if (ontologyUri == null || ( (dp.getNamespace()!=null) && (dp.getNamespace().equals(ontologyUri)) ) ) {
            if ( counter < 1 && position < 1 ) {
                 tempString += "{ \"name\": ";
            }
            else if ( position == previous_posn ) {
                        tempString += "}, { \"name\": ";
            } 
            else if ( position > previous_posn ) {
                tempString += " { \"name\": ";
            }
            else if ( position < previous_posn ) {
                tempString += "}, { \"name\": ";
            }

            String nameStr = dp.getPublicName()==null ? dp.getName()==null ? dp.getURI()==null ? "(no name)" : dp.getURI() : dp.getName() : dp.getPublicName();
            nameStr = nameStr.replace("\"","\\\"");
            nameStr = nameStr.replace("\'","\\\'");
            try {
                tempString += "\"<a href='datapropEdit?uri="+URLEncoder.encode(dp.getURI(),"UTF-8")+"'>" + nameStr + "</a>\", ";                 
            } catch (Exception e) {
                tempString += "\"" + nameStr + "\", "; 
                log.error("Unsupported: URLEncoder.encode() with UTF-8");
            }

            tempString += "\"data\": { \"internalName\": \"" + dp.getLocalNameWithPrefix() + "\", ";

            VClass tmp = null;
            try {
            	tempString += "\"domainVClass\": \"" + (((tmp = vcDao.getVClassByURI(dp.getDomainClassURI())) != null && (tmp.getLocalNameWithPrefix() == null)) ? "" : vcDao.getVClassByURI(dp.getDomainClassURI()).getLocalNameWithPrefix()) + "\", " ;
            } catch (NullPointerException e) {
            	tempString += "\"domainVClass\": \"\",";
            }
            try {
            	Datatype rangeDatatype = dDao.getDatatypeByURI(dp.getRangeDatatypeURI());
                String rangeDatatypeStr = (rangeDatatype==null)?dp.getRangeDatatypeURI():rangeDatatype.getName();
            	tempString += "\"rangeVClass\": \"" + ((rangeDatatypeStr != null) ? rangeDatatypeStr : "") + "\", " ; 
            } catch (NullPointerException e) {
            	tempString += "\"rangeVClass\": \"\",";
            }
            if (dp.getGroupURI() != null) {
                PropertyGroup pGroup = pgDao.getGroupByURI(dp.getGroupURI());
                tempString += "\"group\": \"" + ((pGroup == null) ? "unknown group" : pGroup.getName()) + "\" " ; 
            } else {
                tempString += "\"group\": \"unspecified\"";
            }
            tempString += "}, \"children\": [";
            
            previous_posn = position;
       }
        return tempString;
    }

    private class DataPropertyAlphaComparator implements Comparator {
        public int compare(Object o1, Object o2) {
        	return Collator.getInstance().compare( ((DataProperty)o1).getName(), ((DataProperty)o2).getName());
        }
    }
	
}
