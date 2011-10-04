/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class PropertyWebappsListingController extends BaseEditController {
    private static Log log = LogFactory.getLog( PropertyWebappsListingController.class );
    private int NUM_COLS = 9;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);

        String noResultsMsgStr = "No object properties found";

        String ontologyUri = request.getParameter("ontologyUri");

        ObjectPropertyDao dao = vrequest.getFullWebappDaoFactory().getObjectPropertyDao();
        PropertyInstanceDao piDao = vrequest.getFullWebappDaoFactory().getPropertyInstanceDao();
        VClassDao vcDao = vrequest.getFullWebappDaoFactory().getVClassDao();
        PropertyGroupDao pgDao = vrequest.getFullWebappDaoFactory().getPropertyGroupDao();

        String vclassURI = request.getParameter("vclassUri");
        
        List props = new ArrayList();
        if (request.getParameter("propsForClass") != null) {
            noResultsMsgStr = "There are no properties that apply to this class.";
            
            // incomplete list of classes to check, but better than before
            List<String> superclassURIs = vcDao.getAllSuperClassURIs(vclassURI);
            superclassURIs.add(vclassURI);
            superclassURIs.addAll(vcDao.getEquivalentClassURIs(vclassURI));
            
            Map<String, PropertyInstance> propInstMap = new HashMap<String, PropertyInstance>();
            for (String classURI : superclassURIs) {
            	Collection<PropertyInstance> propInsts = piDao.getAllPropInstByVClass(classURI);
            	for (PropertyInstance propInst : propInsts) {
            		propInstMap.put(propInst.getPropertyURI(), propInst);
            	}
            }
            List<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
            propInsts.addAll(propInstMap.values());
            Collections.sort(propInsts);
            
            Iterator propInstIt = propInsts.iterator();
            HashSet propURIs = new HashSet();
            while (propInstIt.hasNext()) {
                PropertyInstance pi = (PropertyInstance) propInstIt.next();
                if (!(propURIs.contains(pi.getPropertyURI()))) {
                    propURIs.add(pi.getPropertyURI());
                    ObjectProperty prop = (ObjectProperty) dao.getObjectPropertyByURI(pi.getPropertyURI());
                    if (prop != null) {
                        props.add(prop);
                    }
                }
            }
        } else {
            props = (request.getParameter("iffRoot")!=null)
            ? dao.getRootObjectProperties()
            : dao.getAllObjectProperties();
        }
        
        OntologyDao oDao = vrequest.getFullWebappDaoFactory().getOntologyDao();
        HashMap<String,String> ontologyHash = new HashMap<String,String>();

        Iterator propIt = props.iterator();
        List<ObjectProperty> scratch = new ArrayList();
        while (propIt.hasNext()) {
            ObjectProperty p = (ObjectProperty) propIt.next();
            if (p.getNamespace()!=null) {
                if( !ontologyHash.containsKey( p.getNamespace() )){
                    Ontology o = (Ontology)oDao.getOntologyByURI(p.getNamespace());
                    if (o==null) {
                        if (!VitroVocabulary.vitroURI.equals(p.getNamespace())) {
                            log.debug("doGet(): no ontology object found for the namespace "+p.getNamespace());
                        }
                    } else {
                        ontologyHash.put(p.getNamespace(), o.getName() == null ? p.getNamespace() : o.getName());
                    }
                }
                if (ontologyUri != null && p.getNamespace().equals(ontologyUri)) {
                    scratch.add(p);
                }
            }
        }

        if (ontologyUri != null) {
            props = scratch;
        }

        if (props != null) {
        	Collections.sort(props, new ObjectPropertyHierarchyListingController.ObjectPropertyAlphaComparator());
        }

        ArrayList results = new ArrayList();
        results.add("XX");                   // column 1
        results.add("property public name"); // column 2
        results.add("prefix + local name");  // column 3
        results.add("domain");        // column 4
        results.add("range");         // column 5
        results.add("group");         // column 6
        results.add("display tier");  // column 7
        results.add("display level"); // column 8
        results.add("update level");  // column 9

        if (props != null) {
            if (props.size()==0) {
                results.add("XX");
                results.add("<strong>"+noResultsMsgStr+"</strong>");
                results.add("");
                results.add("");
                results.add("");
                results.add("");
                results.add("");
                results.add("");
                results.add("");
            } else {
                Iterator propsIt = props.iterator();
                while (propsIt.hasNext()) {
                    ObjectProperty prop = (ObjectProperty) propsIt.next();
                    results.add("XX");
                    
                    String propNameStr = ObjectPropertyHierarchyListingController.getDisplayLabel(prop);
                    try {
                        results.add("<a href=\"./propertyEdit?uri="+URLEncoder.encode(prop.getURI(),"UTF-8")+"\">" + propNameStr + "</a>"); // column 1
                    } catch (Exception e) {
                        results.add(propNameStr); // column 2
                    }
                    
                    results.add(prop.getLocalNameWithPrefix()); // column 3
                    
                    VClass vc = (prop.getDomainVClassURI() != null) ?
                        vcDao.getVClassByURI(prop.getDomainVClassURI()) : null;
                    String domainStr = (vc != null) ? vc.getLocalNameWithPrefix() : ""; 
                    results.add(domainStr); // column 4
                    
                    vc = (prop.getRangeVClassURI() != null) ?
                        vcDao.getVClassByURI(prop.getRangeVClassURI()) : null;
                    String rangeStr = (vc != null) ? vc.getLocalNameWithPrefix() : ""; 
                    results.add(rangeStr); // column 5
                    
                    if (prop.getGroupURI() != null) {
                        PropertyGroup pGroup = pgDao.getGroupByURI(prop.getGroupURI());
                        results.add(pGroup == null ? "unknown group" : pGroup.getName()); // column 6
                    } else {
                        results.add("unspecified");
                    }
                    if (prop.getDomainDisplayTierInteger() != null) {
                    	results.add(Integer.toString(prop.getDomainDisplayTierInteger(), BASE_10)); // column 7
                    } else {
                    	results.add(""); // column 7
                    }
                    results.add(prop.getHiddenFromDisplayBelowRoleLevel()  == null ? "(unspecified)" : prop.getHiddenFromDisplayBelowRoleLevel().getShorthand()); // column 8
                    results.add(prop.getProhibitedFromUpdateBelowRoleLevel() == null ? "(unspecified)" : prop.getProhibitedFromUpdateBelowRoleLevel().getShorthand()); // column 9
                }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Object Properties");
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newPropParams=new HashMap<String,String>();
        newPropParams.put("controller", "Property");
        ButtonForm newPropButton = new ButtonForm(Controllers.RETRY_URL,"buttonForm","Add new object property",newPropParams);
        buttons.add(newPropButton);
        HashMap<String,String> rootPropParams=new HashMap<String,String>();
        rootPropParams.put("iffRoot", "true");
        String temp;
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	rootPropParams.put("ontologyUri",temp);
        }
        ButtonForm rootPropButton = new ButtonForm("showObjectPropertyHierarchy","buttonForm","root properties",rootPropParams);
        buttons.add(rootPropButton);
        request.setAttribute("topButtons", buttons);
        
        /* original way of adding 1 button
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new object property");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Property");
        */
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
