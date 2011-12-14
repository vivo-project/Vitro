/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vedit.beans.ButtonForm;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class ClassHierarchyListingController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ClassHierarchyListingController.class.getName());

	private int MAXDEPTH = 7;
    private int NUM_COLS = 9;

    private VClassDao vcDao = null;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
    		return;
    	}
    	
        VitroRequest vrequest = new VitroRequest(request);

        try {
        boolean inferred = (vrequest.getParameter("inferred") != null);
        
        if (vrequest.getAssertionsWebappDaoFactory() != null && !inferred) {
        	vcDao = vrequest.getAssertionsWebappDaoFactory().getVClassDao();
        } else {
        	vcDao = vrequest.getFullWebappDaoFactory().getVClassDao();
        }

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");            // column 1
        results.add("class");         // column 2
        results.add("shortdef");      // column 3
        results.add("example");       // column 4
        results.add("group");         // column 5
        results.add("ontology");      // column 6
        results.add("display level"); // column 7
        results.add("update level");  // column 8
        results.add("XX");            // column 9

        String ontologyUri = request.getParameter("ontologyUri");
        String startClassUri = request.getParameter("vclassUri");

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
        	roots.add(vrequest.getFullWebappDaoFactory().getVClassDao()
        			.getTopConcept());
        }
        
        Collections.sort(roots);

        Iterator rootIt = roots.iterator();
        if (!rootIt.hasNext()) {
            VClass vcw = new VClass();
            vcw.setName("<strong>No classes found.</strong>");
            results.addAll(addVClassDataToResultsList(vrequest.getFullWebappDaoFactory(), vcw,0,ontologyUri));
        } else {
            while (rootIt.hasNext()) {
                VClass root = (VClass) rootIt.next();
	            if (root != null) {
	                ArrayList childResults = new ArrayList();
	                addChildren(vrequest.getFullWebappDaoFactory(), root, childResults, 0, ontologyUri);
	                results.addAll(childResults);
                }
            }
        }

        request.setAttribute("results",results);
        request.setAttribute("columncount",NUM_COLS);
        request.setAttribute("suppressquery","true");
        request.setAttribute("title", (inferred) ? "Inferred Class Hierarchy" : "Class Hierarchy");
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newClassParams=new HashMap<String,String>();
        String temp;
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	newClassParams.put("ontologyUri",temp);
        }
        ButtonForm newClassButton = new ButtonForm(Controllers.VCLASS_RETRY_URL,"buttonForm","Add new class",newClassParams);
        buttons.add(newClassButton);
        HashMap<String,String> allClassParams=new HashMap<String,String>();
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	allClassParams.put("ontologyUri",temp);
        }
        ButtonForm allClassButton = new ButtonForm("listVClassWebapps","buttonForm","All classes",allClassParams);
        buttons.add(allClassButton);
        if (!inferred) {
            HashMap<String,String> inferParams=new HashMap<String,String>();
            if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
            	inferParams.put("ontologyUri",temp);
            }
            inferParams.put("inferred", "1");
            ButtonForm inferButton = new ButtonForm("showClassHierarchy","buttonForm","Inferred class hierarchy",inferParams);
            buttons.add(inferButton);
        } else {
        	HashMap<String,String> inferParams=new HashMap<String,String>();
            if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
            	inferParams.put("ontologyUri",temp);
            }
            ButtonForm inferButton = new ButtonForm("showClassHierarchy","buttonForm","Asserted class hierarchy",inferParams);
            buttons.add(inferButton);
        }
        request.setAttribute("topButtons", buttons);
        /*
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.VCLASS_RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new class");
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

    private void addChildren(WebappDaoFactory wadf, VClass parent, ArrayList list, int position, String ontologyUri) {
    	List rowElts = addVClassDataToResultsList(wadf, parent, position, ontologyUri);
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
                addChildren(wadf, child, list, position + childShift, ontologyUri);
            }

        }
    }

    private List addVClassDataToResultsList(WebappDaoFactory wadf, VClass vcw, int position, String ontologyUri) {
        List results = new ArrayList();
        if (ontologyUri == null || ( (vcw.getNamespace()!=null) && (vcw.getNamespace().equals(ontologyUri)) ) ) {
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
