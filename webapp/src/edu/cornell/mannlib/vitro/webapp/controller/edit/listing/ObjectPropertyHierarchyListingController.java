/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.ButtonForm;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class ObjectPropertyHierarchyListingController extends BaseEditController {

	private static final Log log = LogFactory.getLog(ObjectPropertyHierarchyListingController.class.getName());
	
    private int MAXDEPTH = 5;
    private int NUM_COLS = 9;

    private ObjectPropertyDao opDao = null;
    private VClassDao vcDao = null;
    private PropertyGroupDao pgDao = null;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
    		return;
    	}
    	
        VitroRequest vrequest = new VitroRequest(request);
        try {

        opDao = vrequest.getAssertionsWebappDaoFactory().getObjectPropertyDao();
        vcDao = vrequest.getAssertionsWebappDaoFactory().getVClassDao();
        pgDao = vrequest.getAssertionsWebappDaoFactory().getPropertyGroupDao();

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");            // column 1
        results.add("property");      // column 2
        results.add("domain vclass"); // column 3
        results.add("range vclass");  // column 4
        results.add("group");         // column 5
        results.add("display tier");  // column 6
        results.add("display level"); // column 7
        results.add("update level");  // column 8
        results.add("XX");            // column 10

        String ontologyUri = request.getParameter("ontologyUri");
        String startPropertyUri = request.getParameter("propertyUri");

        List<ObjectProperty> roots = null;

        if (startPropertyUri != null) {
        	roots = new LinkedList<ObjectProperty>();
        	ObjectProperty op = opDao.getObjectPropertyByURI(startPropertyUri);
        	if (op == null) {
        		op = new ObjectProperty();
        		op.setURI(startPropertyUri);
        	}
        	roots.add(op);
        } else {
            roots = opDao.getRootObjectProperties();
            if (roots!=null){
                Collections.sort(roots, new ObjectPropertyAlphaComparator()); // sorts by domain public
            }
        }

        if (roots != null) {
            Iterator<ObjectProperty> rootIt = roots.iterator();
            if (!rootIt.hasNext()) {
                ObjectProperty op = new ObjectProperty();
                op.setURI(ontologyUri+"fake");
                String notFoundMessage = "<strong>No object properties found.</strong>"; 
                op.setDomainPublic(notFoundMessage);
                results.addAll(addObjectPropertyDataToResultsList(op,0,ontologyUri));
            } else {
                while (rootIt.hasNext()) {
                    ObjectProperty root = rootIt.next();
                    if ( (ontologyUri==null) || 
                    		( (ontologyUri != null) 
                    		  && (root.getNamespace() != null) 
                    		  && (ontologyUri.equals(root.getNamespace())) ) ) {
                    	ArrayList childResults = new ArrayList();
                    	addChildren(root, childResults, 0, ontologyUri);
                    	results.addAll(childResults);
                	}
                }	
            }
        }
        
        request.setAttribute("results",results);
        request.setAttribute("columncount",NUM_COLS);
        request.setAttribute("suppressquery","true");
        request.setAttribute("title", "Object Property Hierarchy");
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newPropParams=new HashMap<String,String>();
        newPropParams.put("controller", "Property");
        ButtonForm newPropButton = new ButtonForm(Controllers.RETRY_URL,"buttonForm","Add new object property",newPropParams);
        buttons.add(newPropButton);
        HashMap<String,String> allPropParams=new HashMap<String,String>();
        String temp;
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	allPropParams.put("ontologyUri",temp);
        }
        ButtonForm allPropButton = new ButtonForm("listPropertyWebapps","buttonForm","show all object properties",allPropParams);
        buttons.add(allPropButton);
        request.setAttribute("topButtons", buttons);
        /*
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new object property");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Property");
        */
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private void addChildren(ObjectProperty parent, ArrayList list, int position, String ontologyUri) {
        list.addAll(addObjectPropertyDataToResultsList(parent, position, ontologyUri));
        List childURIstrs = opDao.getSubPropertyURIs(parent.getURI());
        if ((childURIstrs.size()>0) && position<MAXDEPTH) {
            List childProps = new ArrayList();
            Iterator childURIstrIt = childURIstrs.iterator();
            while (childURIstrIt.hasNext()) {
                String URIstr = (String) childURIstrIt.next();
                ObjectProperty child = (ObjectProperty) opDao.getObjectPropertyByURI(URIstr);
                childProps.add(child);
            }
            Collections.sort(childProps);
            Iterator childPropIt = childProps.iterator();
            while (childPropIt.hasNext()) {
                ObjectProperty child = (ObjectProperty) childPropIt.next();
                addChildren(child, list, position+1,ontologyUri);
            }

        }
    }

    private List addObjectPropertyDataToResultsList(ObjectProperty op, int position, String ontologyUri) {
        List results = new ArrayList();
        if (ontologyUri == null || ( (op.getNamespace()!=null) && (op.getNamespace().equals(ontologyUri)) ) ) {
            for (int i=0; i<position; i++) {
                results.add("@@entities");  // column 1
            }
            if (position==0)
                results.add("XX"); // column 1
            Integer numCols = (NUM_COLS-1)-position;

            String hyperlink = null;
            try {
            	hyperlink =  "<a href=\"propertyEdit?uri="+URLEncoder.encode(op.getURI(),"UTF-8")+"\">"+getDisplayLabel(op)+"</a>";
            } catch (UnsupportedEncodingException uee) {
            	log.error("Unsupported: URLEncoder.encode() with UTF-8");
            }
            
            numCols = addColToResults( ((hyperlink != null) ? hyperlink : getDisplayLabel(op)) 
            		+ "<br/><span style='font-style:italic; color:\"grey\";'>"+op.getLocalNameWithPrefix()+"</span>", results, numCols); // column 2
            
            VClass tmp = null;
            try {
            	numCols = addColToResults((((tmp = vcDao.getVClassByURI(op.getDomainVClassURI())) != null && (tmp.getLocalNameWithPrefix() == null)) ? "" : vcDao.getVClassByURI(op.getDomainVClassURI()).getLocalNameWithPrefix()), results, numCols); // column 3
            } catch (NullPointerException e) {
            	numCols = addColToResults("",results,numCols);
            }
            try {
            	numCols = addColToResults((((tmp = vcDao.getVClassByURI(op.getRangeVClassURI())) != null && (tmp.getLocalNameWithPrefix() == null)) ? "" : vcDao.getVClassByURI(op.getRangeVClassURI()).getLocalNameWithPrefix()), results, numCols); // column 4
            } catch (NullPointerException e) {
            	numCols = addColToResults("",results,numCols);
            }
            if (op.getGroupURI() != null) {
                PropertyGroup pGroup = pgDao.getGroupByURI(op.getGroupURI());
                numCols = addColToResults(((pGroup == null) ? "unspecified" : pGroup.getName()), results, numCols); // column 5
            } else {
                numCols = addColToResults("unspecified", results, numCols);
            }
            Integer displayTier = op.getDomainDisplayTierInteger();
            numCols = addColToResults(
            		(displayTier == null) 
            		        ? "" 
            		        : Integer.toString(displayTier, BASE_10), 
            		 results, numCols); // column 6
            numCols = addColToResults(
            		(op.getHiddenFromDisplayBelowRoleLevel() == null) 
            		        ? "unspecified" 
            		        : op.getHiddenFromDisplayBelowRoleLevel()
            		                .getShorthand(), 
            		 results, numCols); // column 7
            numCols = addColToResults(
            		(op.getProhibitedFromUpdateBelowRoleLevel() == null)
            		        ? "unspecified" 
            		        : op.getProhibitedFromUpdateBelowRoleLevel()
            		                .getShorthand(), 
            		 results, numCols); // column 8
            results.add("XX"); // column 9
        }
        return results;
    }

    private Integer addColToResults (String value, List results, Integer colIndex) {
        if (colIndex>0) {
            results.add(value);
        }
        return colIndex-1;
    }

    public static class ObjectPropertyAlphaComparator implements Comparator<ObjectProperty> {
        public int compare(ObjectProperty op1, ObjectProperty op2) {
        	if (op1 == null) {
        		return 1;
        	} else if (op2 == null) {
        		return -1;
        	}
        	String propLabel1 = getDisplayLabel(op1);
        	String propLabel2 = getDisplayLabel(op2);
        	if (propLabel1 == null) {
        		return 1;
        	} else if (propLabel2 == null) {
        		return -1;
        	} else {
        		return Collator.getInstance().compare( propLabel1, propLabel2 );
        	}
        }
    }
    
    /*
     * should never be null
     */
    public static String getDisplayLabel(ObjectProperty op) {
    	String domainPublic = op.getDomainPublic();
    	String displayLabel = (domainPublic != null && domainPublic.length() > 0)  
			? domainPublic 
			: op.getLocalName();
		return (displayLabel != null) ? displayLabel : "[object property]" ;
    }

}