/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

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
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class DataPropertyHierarchyListingController extends BaseEditController {

	private static final Log log = LogFactory.getLog(DataPropertyHierarchyListingController.class.getName());
	
    private int MAXDEPTH = 5;
    private int NUM_COLS = 9;

    private DataPropertyDao dpDao = null;
    private VClassDao vcDao = null;
    private PropertyGroupDao pgDao = null;
    private DatatypeDao dDao = null;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
    		return;
    	}
    	
        VitroRequest vrequest = new VitroRequest(request);
        try {

        dpDao = vrequest.getAssertionsWebappDaoFactory().getDataPropertyDao();
        vcDao = vrequest.getAssertionsWebappDaoFactory().getVClassDao();
        pgDao = vrequest.getAssertionsWebappDaoFactory().getPropertyGroupDao();
        dDao = vrequest.getAssertionsWebappDaoFactory().getDatatypeDao();

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");            // column 1
        results.add("property");      // column 2
        results.add("domain vclass"); // column 3
        results.add("range vclass");  // column 4
        results.add("group");         // column 5
        results.add("display tier");  // column 6
        results.add("display level"); // column 7
        results.add("update level");  // column 8
        results.add("XX");            // column 9

        String ontologyUri = request.getParameter("ontologyUri");
        String startPropertyUri = request.getParameter("propertyUri");

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

        if (roots!=null) {
            Iterator<DataProperty> rootIt = roots.iterator();
            if (!rootIt.hasNext()) {
                DataProperty dp = new DataProperty();
                dp.setURI(ontologyUri+"fake");
                String notFoundMessage = "<strong>No data properties found.</strong>"; 
                dp.setName(notFoundMessage);
                dp.setName(notFoundMessage);
                results.addAll(addDataPropertyDataToResultsList(dp,0,ontologyUri));
            } else {
                while (rootIt.hasNext()) {
                    DataProperty root = rootIt.next();
                    if ( (ontologyUri==null) || ( (ontologyUri!=null) && (root.getNamespace()!=null) && (ontologyUri.equals(root.getNamespace())) ) ) {
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
        request.setAttribute("title", "Data Property Hierarchy");
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newPropParams=new HashMap<String,String>();
        newPropParams.put("controller", "Dataprop");
        
        ButtonForm newPropButton = new ButtonForm(Controllers.RETRY_URL,"buttonForm","Add new data property",newPropParams);
        buttons.add(newPropButton);
        HashMap<String,String> allPropParams=new HashMap<String,String>();
   
        String temp;
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	allPropParams.put("ontologyUri",temp);
        }
        ButtonForm allPropButton = new ButtonForm("listDatatypeProperties","buttonForm","show all data properties",allPropParams);
        buttons.add(allPropButton);
        request.setAttribute("topButtons", buttons);
        /*
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new data property");
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

    private void addChildren(DataProperty parent, ArrayList list, int position, String ontologyUri) {
    	if (parent == null) {
    		return;
    	}
        list.addAll(addDataPropertyDataToResultsList(parent, position, ontologyUri));
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
                addChildren(child, list, position+1,ontologyUri);
            }

        }
    }

    private List addDataPropertyDataToResultsList(DataProperty dp, int position, String ontologyUri) {
        List results = new ArrayList();
        if (dp == null) {
        	return results;
        }
        if (ontologyUri == null || ( (dp.getNamespace()!=null) && (dp.getNamespace().equals(ontologyUri)) ) ) {
            //if (position==1)
            //  position=2;
            for (int i=0; i<position; i++) {
                results.add("@@entities");  // column 1
            }
            if (position==0)
                results.add("XX"); // column 1
            Integer numCols = (NUM_COLS-1)-position;

            String nameStr = dp.getPublicName()==null ? dp.getName()==null ? dp.getURI()==null ? "(no name)" : dp.getURI() : dp.getName() : dp.getPublicName();
            try {
                numCols=addColToResults("<a href=\"datapropEdit?uri="+URLEncoder.encode(dp.getURI(),"UTF-8")+"\">"+nameStr+"</a> <span style='font-style:italic; color:\"grey\";'>"+dp.getLocalNameWithPrefix()+"</span>",results,numCols); // column 2
            } catch (Exception e) {
                numCols=addColToResults(nameStr + " <i>" + dp.getLocalNameWithPrefix() + "</i>",results,numCols); // column 2
            }
            VClass tmp = null;
            try {
            	numCols = addColToResults((((tmp = vcDao.getVClassByURI(dp.getDomainClassURI())) != null && (tmp.getLocalNameWithPrefix() == null)) ? "" : vcDao.getVClassByURI(dp.getDomainClassURI()).getLocalNameWithPrefix()), results, numCols); // column 3
            } catch (NullPointerException e) {
            	numCols = addColToResults("-",results,numCols);
            }
            try {
            	Datatype rangeDatatype = dDao.getDatatypeByURI(dp.getRangeDatatypeURI());
                String rangeDatatypeStr = (rangeDatatype==null)?dp.getRangeDatatypeURI():rangeDatatype.getName();
            	numCols = addColToResults( (rangeDatatypeStr != null) ? rangeDatatypeStr : "-", results, numCols); // column 4
            } catch (NullPointerException e) {
            	numCols = addColToResults("-",results,numCols);
            }
            if (dp.getGroupURI() != null) {
                PropertyGroup pGroup = pgDao.getGroupByURI(dp.getGroupURI());
                numCols = addColToResults(((pGroup == null) ? "unspecified" : pGroup.getName()), results, numCols); // column 5
            } else {
                numCols = addColToResults("unspecified", results, numCols);
            }
            Integer displayTier = dp.getDisplayTier();
            String displayTierStr = (displayTier < 0) 
                    ? "" 
                    : Integer.toString(displayTier);  
            numCols = addColToResults(displayTierStr, results, numCols);
            numCols = addColToResults(
            		(dp.getHiddenFromDisplayBelowRoleLevel() == null) 
            		        ? "unspecified" 
            		        : dp.getHiddenFromDisplayBelowRoleLevel()
            		                .getShorthand(), results, numCols); // column 7
            numCols = addColToResults(
            		(dp.getProhibitedFromUpdateBelowRoleLevel() == null)
            		        ? "unspecified" 
            		        : dp.getProhibitedFromUpdateBelowRoleLevel()
            		                .getShorthand(), results, numCols); // column 8
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

    private class DataPropertyAlphaComparator implements Comparator {
        public int compare(Object o1, Object o2) {
        	return Collator.getInstance().compare( ((DataProperty)o1).getName(), ((DataProperty)o2).getName());
        }
    }
	
}
