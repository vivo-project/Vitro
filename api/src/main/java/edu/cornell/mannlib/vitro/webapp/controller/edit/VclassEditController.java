/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_ONLY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class VclassEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(VclassEditController.class.getName());
	private static final int NUM_COLS = 14;

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);

        EditProcessObject epo = super.createEpo(request, FORCE_NEW);
        request.setAttribute("epoKey", epo.getKey());

        VClassDao vcwDao = ModelAccess.on(getServletContext()).getWebappDaoFactory(ASSERTIONS_ONLY).getVClassDao();
        VClass vcl = (VClass)vcwDao.getVClassByURI(request.getParameter("uri"));
        
        if (vcl == null) {
        	vcl = request.getUnfilteredWebappDaoFactory()
        	        .getVClassDao().getTopConcept();
        }

        request.setAttribute("VClass",vcl);
        
        ArrayList results = new ArrayList();
        results.add("class");                // 1
        results.add("class label");          // 2
        results.add("class group");          // 3
        results.add("ontology");             // 4
        results.add("RDF local name");       // 5
        results.add("short definition");     // 6
        results.add("example");              // 7
        results.add("editor description");   // 8
        //results.add("curator comments"); 
        results.add("display level");        // 9
        results.add("update level");         // 10
        results.add("display rank");         // 11
        results.add("custom entry form");    // 12
        results.add("URI");                  // 13
        results.add("publish level");        // 14
        
        String ontologyName = null;
        if (vcl.getNamespace() != null) {
            Ontology ont = request.getUnfilteredWebappDaoFactory().getOntologyDao().getOntologyByURI(vcl.getNamespace());
            if ( (ont != null) && (ont.getName() != null) ) {
                ontologyName = ont.getName();
            }
        }

        WebappDaoFactory wadf = request.getUnfilteredWebappDaoFactory();
        String groupURI = vcl.getGroupURI();
        String groupName = "none";
        if(groupURI != null) { 
            VClassGroupDao groupDao= wadf.getVClassGroupDao();
            VClassGroup classGroup = groupDao.getGroupByURI(groupURI);
            if (classGroup != null) {
                groupName = classGroup.getPublicName();
            }
        }

        String shortDef = (vcl.getShortDef()==null) ? "" : vcl.getShortDef();
        String example = (vcl.getExample()==null) ? "" : vcl.getExample();
        String description = (vcl.getDescription()==null) ? "" : vcl.getDescription();
        
        boolean foundComment = false;
        StringBuffer commSb = null;
        for (Iterator<String> commIt = request.getUnfilteredWebappDaoFactory().getCommentsForResource(vcl.getURI()).iterator(); commIt.hasNext();) { 
            if (commSb==null) {
                commSb = new StringBuffer();
                foundComment=true;
            }
            commSb.append(commIt.next()).append(" ");
        }
        if (!foundComment) {
            commSb = new StringBuffer("no comments yet");
        }
               
		String hiddenFromDisplay = (vcl.getHiddenFromDisplayBelowRoleLevel() == null ? "(unspecified)"
				: vcl.getHiddenFromDisplayBelowRoleLevel().getDisplayLabel());
		String ProhibitedFromUpdate = (vcl
				.getProhibitedFromUpdateBelowRoleLevel() == null ? "(unspecified)"
				: vcl.getProhibitedFromUpdateBelowRoleLevel().getUpdateLabel());
		String hiddenFromPublish = (vcl.getHiddenFromPublishBelowRoleLevel() == null ? "(unspecified)"
				: vcl.getHiddenFromPublishBelowRoleLevel().getDisplayLabel());

        String customEntryForm = (vcl.getCustomEntryForm() == null ? "(unspecified)" : vcl.getCustomEntryForm());
        
       //String lastModified = "<i>not implemented yet</i>"; // TODO
        
        String uri = (vcl.getURI() == null) ? "" : vcl.getURI();
        
        results.add(vcl.getPickListName());                                // 1
        results.add(vcl.getName() == null ? "(no public label)" : vcl.getName()); // 2
        results.add(groupName);                                                   // 3
        results.add(ontologyName==null ? "(not identified)" : ontologyName);      // 4
        results.add(vcl.getLocalName());     // 5
        results.add(shortDef);               // 6
        results.add(example);                // 7
        results.add(description);            // 8
        //results.add(commSb.toString());    // 
        results.add(hiddenFromDisplay);      // 9
        results.add(ProhibitedFromUpdate);   // 10
        results.add(String.valueOf(vcl.getDisplayRank())); // 11
        results.add(customEntryForm);        // 12
        results.add(uri);                    // 13
        results.add(hiddenFromPublish);      // 14
        request.setAttribute("results", results);
        request.setAttribute("columncount", NUM_COLS);
        request.setAttribute("suppressquery", "true");

        epo.setDataAccessObject(vcl);
        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();

        HashMap formSelect = new HashMap(); // tells the JSP what select lists are populated, and thus should be displayed
        request.setAttribute("formSelect",formSelect);

        // if supported, we want to show only the asserted superclasses and subclasses.
        VClassDao vcDao = ModelAccess.on(getServletContext()).getWebappDaoFactory(ASSERTIONS_ONLY).getVClassDao();
        VClassDao displayVcDao = ModelAccess.on(getServletContext()).getWebappDaoFactory().getVClassDao();
        
        List<VClass> superVClasses = getVClassesForURIList(
                vcDao.getSuperClassURIs(vcl.getURI(),false), displayVcDao);
        sortForPickList(superVClasses, request);
        request.setAttribute("superclasses",superVClasses);

        List<VClass> subVClasses = getVClassesForURIList(
                vcDao.getSubClassURIs(vcl.getURI()), displayVcDao);
        sortForPickList(subVClasses, request);
        request.setAttribute("subclasses",subVClasses);
            
        List<VClass> djVClasses = getVClassesForURIList(
                vcDao.getDisjointWithClassURIs(vcl.getURI()), displayVcDao);
        sortForPickList(djVClasses, request);
        request.setAttribute("disjointClasses",djVClasses);

        List<VClass> eqVClasses = getVClassesForURIList(
                vcDao.getEquivalentClassURIs(vcl.getURI()), displayVcDao);
        sortForPickList(eqVClasses, request);
        request.setAttribute("equivalentClasses",eqVClasses);
   
        // add the options
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        boolean instantiable = (vcl.getURI().equals(OWL.Nothing.getURI())) ? false : true;
        
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("vclassWebapp", vcl);
        request.setAttribute("instantiable", instantiable);
        request.setAttribute("title","Class Control Panel");
        //request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+request.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/specific/classes_edit.jsp");
        } catch (Exception e) {
            log.error("VclassEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }
    
    private List<VClass> getVClassesForURIList(List<String> vclassURIs, VClassDao vcDao) {
        List<VClass> vclasses = new ArrayList<VClass>();
        Iterator<String> urIt = vclassURIs.iterator();
        while (urIt.hasNext()) {
            String vclassURI = urIt.next();
            VClass vclass = vcDao.getVClassByURI(vclassURI);
            if (vclass != null) {
                vclasses.add(vclass);
            }
        }
        return vclasses;
    }

}