/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VClassWebappsListingController extends BaseEditController {
    
    private int NUM_COLS = 9;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);

        //need to figure out how to structure the results object to put the classes underneath
        
        List<VClass> classes = null;
        
        if (request.getParameter("showPropertyRestrictions") != null) {
        	PropertyDao pdao = vrequest.getFullWebappDaoFactory().getObjectPropertyDao();
        	classes = pdao.getClassesWithRestrictionOnProperty(request.getParameter("propertyURI"));
        } else {
        	VClassDao vcdao = vrequest.getFullWebappDaoFactory().getVClassDao();
        	
        	if (request.getParameter("iffRoot") != null) {
                classes = vcdao.getRootClasses();
        	} else {
        		classes = vcdao.getAllVclasses();
        	}
        	
        }

        String ontologyURI = vrequest.getParameter("ontologyUri");
            
        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");
        results.add("Class");
        results.add("short definition");
        results.add("example");
        results.add("comments");
        results.add("group");
        results.add("ontology");
        results.add("display level");
        results.add("update level");        

        if (classes != null) {
            Collections.sort(classes);
            Iterator<VClass> classesIt = classes.iterator();
            while (classesIt.hasNext()) {
                VClass cls = (VClass) classesIt.next();
                if ( (ontologyURI==null) || ( (ontologyURI != null) && (cls.getNamespace()!=null) && (ontologyURI.equals(cls.getNamespace())) ) ) {
	                results.add("XX");
	                if (cls.getName() != null)
	                    try {
	                        //String className = (cls.getName()==null || cls.getName().length()==0) ? cls.getURI() : cls.getName();
	                        results.add("<a href=\"./vclassEdit?uri="+URLEncoder.encode(cls.getURI(),"UTF-8")+"\">"+cls.getLocalNameWithPrefix()+"</a>");
	                    } catch (Exception e) {
	                        results.add(cls.getLocalNameWithPrefix());
	                    }
	                else
	                    results.add("");
	                String shortDef = (cls.getShortDef()==null) ? "" : cls.getShortDef();
	                String example = (cls.getExample()==null) ? "" : cls.getExample();
	                StringBuffer commSb = new StringBuffer();
	                for (Iterator<String> commIt = vrequest.getFullWebappDaoFactory().getCommentsForResource(cls.getURI()).iterator(); commIt.hasNext();) { 
	                	commSb.append(commIt.next()).append(" ");
	                }
	                
	                // get group name
	                WebappDaoFactory wadf = vrequest.getFullWebappDaoFactory();
	                VClassGroupDao groupDao= wadf.getVClassGroupDao();
	                String groupURI = cls.getGroupURI();                
	                String groupName = "";
	                VClassGroup classGroup = null;
	                if(groupURI != null) { 
	                	classGroup = groupDao.getGroupByURI(groupURI);
	                	if (classGroup!=null) {
	                	    groupName = classGroup.getPublicName();
	                	}
	                }
	                
	                // TODO : lastModified
	                
	                // get ontology name
	                OntologyDao ontDao = wadf.getOntologyDao();
	                String ontName = null;
	                try {
	                	Ontology ont = ontDao.getOntologyByURI(cls.getNamespace());
	                	ontName = ont.getName();
	                } catch (Exception e) {}
	                ontName = (ontName == null) ? "" : ontName;
	                
	                results.add(shortDef);
	                results.add(example);
	                results.add(commSb.toString());
	                results.add(groupName);
	                results.add(ontName);
	                results.add(cls.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : cls.getHiddenFromDisplayBelowRoleLevel().getShorthand()); // column 8
	                results.add(cls.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : cls.getProhibitedFromUpdateBelowRoleLevel().getShorthand()); // column 9
               }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Classes");
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
        HashMap<String,String> hierClassParams=new HashMap<String,String>();
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	hierClassParams.put("ontologyUri",temp);
        }
        ButtonForm hierClassButton = new ButtonForm("showClassHierarchy","buttonForm","Class hierarchy",hierClassParams);
        buttons.add(hierClassButton);
        request.setAttribute("topButtons", buttons);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
   	
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

}
