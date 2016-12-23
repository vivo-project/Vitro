/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.util.JSONUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;


public class ShowClassHierarchyController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(ShowClassHierarchyController.class.getName());
    
    private static final String TEMPLATE_NAME = "siteAdmin-classHierarchy.ftl";
	private int MAXDEPTH = 7;

    private VClassDao vcDao = null;
    
    private int previous_posn = 0;
    
    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        
        String displayOption = "";
        
        if ( vreq.getParameter("displayOption") != null ) {
            displayOption = vreq.getParameter("displayOption");
        }
        else {
            displayOption = "asserted";
        }

        body.put("displayOption", displayOption);
        boolean inferred = ( displayOption.equals("inferred") );
        if ( inferred ) {
            body.put("pageTitle", "Inferred Class Hierarchy");
        }
        else {
            body.put("pageTitle", "Asserted Class Hierarchy");
        }

        if (!inferred) {
        	vcDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getVClassDao();
        } else {
        	vcDao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
        }
        String json = new String();

        String ontologyUri = vreq.getParameter("ontologyUri");
        String startClassUri = vreq.getParameter("vclassUri");

        List<VClass> roots = null;

        if (ontologyUri != null) {
            roots = vcDao.getOntologyRootClasses(ontologyUri);
        } else if (startClassUri != null) {
        	roots = new LinkedList<VClass>();
        	roots.add(vcDao.getVClassByURI(startClassUri));
        } else {    	
       		roots = vcDao.getRootClasses();
        }

        if (roots.isEmpty()) {
        	roots = new LinkedList<VClass>();
        	roots.add(vreq.getUnfilteredWebappDaoFactory().getVClassDao()
        			.getTopConcept());
        }
        sortForPickList(roots, vreq);
        int counter = 0;

        Iterator<VClass> rootIt = roots.iterator();
        if (!rootIt.hasNext()) {
            VClass vcw = new VClass();
            vcw.setName("<strong>No classes found.</strong>");
            json += addVClassDataToResultsList(vreq.getUnfilteredWebappDaoFactory(), vcw,0,ontologyUri,counter);
        } else {
            while (rootIt.hasNext()) {
                VClass root = (VClass) rootIt.next();
	            if (root != null) {
	                json += addChildren(vreq.getUnfilteredWebappDaoFactory(), 
	                        root, 0, ontologyUri, counter, vreq);
	                counter += 1;
                }
            }
            int length = json.length();
            if ( length > 0 ) {
                json += " }"; 
            }
        }
        body.put("jsonTree",json);
        
        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

    private String addChildren(WebappDaoFactory wadf, VClass parent, int position, 
            String ontologyUri, int counter, VitroRequest vreq) {
        String rowElts = addVClassDataToResultsList(wadf, parent, position, ontologyUri, counter);
    	int childShift = (rowElts.length() > 0) ? 1 : 0;  // if addVClassDataToResultsList filtered out the result, don't shift the children over 
        int length = rowElts.length();
        String leaves = "";
        leaves += rowElts;
        List<String> childURIstrs = vcDao.getSubClassURIs(parent.getURI());
        if ((childURIstrs.size()>0) && position<MAXDEPTH) {
            List<VClass> childClasses = new ArrayList<VClass>();
            Iterator<String> childURIstrIt = childURIstrs.iterator();
            while (childURIstrIt.hasNext()) {
                String URIstr = childURIstrIt.next();
                try {
                    VClass child = vcDao.getVClassByURI(URIstr);
                    if (!child.getURI().equals(OWL.Nothing.getURI())) {
                    	childClasses.add(child);
                    }
                } catch (Exception e) {}
            }
            sortForPickList(childClasses, vreq);
            Iterator<VClass> childClassIt = childClasses.iterator();
            while (childClassIt.hasNext()) {
                VClass child = (VClass) childClassIt.next();
                leaves += addChildren(wadf, child, position + childShift, ontologyUri, counter, vreq);
                if (!childClassIt.hasNext()) {
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

    private String addVClassDataToResultsList(WebappDaoFactory wadf, VClass vcw, int position, String ontologyUri, int counter) {
        String tempString = "";
        if (ontologyUri == null || ( (vcw.getNamespace()!=null) && (vcw.getNamespace().equals(ontologyUri)) ) ) {
            // first if statement ensures that the first class begins with correct format
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
            try {
                tempString += JSONUtils.quote("<a href='vclassEdit?uri=" + 
                        URLEncoder.encode(vcw.getURI(),"UTF-8") + "'>" + 
                        vcw.getPickListName() + "</a>") +", ";
            } catch (Exception e) {
                 tempString += JSONUtils.quote(((vcw.getPickListName() == null) 
                         ? "" : vcw.getPickListName())) + ", ";
            }

            String shortDef = ((vcw.getShortDef() == null) ? "" : vcw.getShortDef()) ;
            tempString += "\"data\": { \"shortDef\": " + JSONUtils.quote(shortDef) + ", ";

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
            tempString += "\"classGroup\": " + JSONUtils.quote(
                    (groupName == null) ? "" : groupName) + ", ";
            // Get ontology name
            OntologyDao ontDao = wadf.getOntologyDao();
            String ontName = vcw.getNamespace();
            Ontology ont = ontDao.getOntologyByURI(ontName);
            if (ont != null && ont.getName() != null) {
                ontName = ont.getName();
            }
            tempString += "\"ontology\": " + JSONUtils.quote(
                    (ontName == null) ? "" : ontName) + "}, \"children\": [";

            previous_posn = position;
        }
        return tempString;
    }

}
