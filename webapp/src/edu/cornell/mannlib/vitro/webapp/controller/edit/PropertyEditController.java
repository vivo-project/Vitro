/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;

public class PropertyEditController extends BaseEditController {

	private static final Log log = LogFactory.getLog(PropertyEditController.class.getName());
	
    public void doPost (HttpServletRequest request, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.EDIT_ONTOLOGY.ACTIONS)) {
        	return;
        }

        final int NUM_COLS=24;

        VitroRequest vreq = new VitroRequest(request);
        
        ObjectPropertyDao propDao = vreq.getUnfilteredWebappDaoFactory().getObjectPropertyDao();
        VClassDao vcDao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
        PropertyGroupDao pgDao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();
        DataPropertyDao dpDao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyDao();
        ObjectProperty p = (ObjectProperty)propDao.getObjectPropertyByURI(request.getParameter("uri"));
        request.setAttribute("property",p);

        ArrayList<String> results = new ArrayList<String>();
        results.add("property");              // column 1
        results.add("parent property");       // column 2
        results.add("property group");        // column 3
        results.add("ontology");              // column 4
        results.add("RDF local name");        // column 5
        results.add("public display label");  // column 6
        results.add("domain class");          // column 7
        results.add("range class");           // column 8
        results.add("transitive");            // column 9
        results.add("symmetric");             // column 10
        results.add("functional");            // column 11
        results.add("inverse functional");    // column 12
        results.add("public description");    // column 13
        results.add("example");               // column 14
        results.add("editor description");    // column 15
        results.add("display level");         // column 16
        results.add("update level");          // column 17
        results.add("display tier");          // column 18
        results.add("collate by subclass");   // column 19
        results.add("custom entry form");     // column 20
        results.add("select from existing");  // column 21
        results.add("offer create new");      // column 22
        results.add("sort direction");        // column 23
        results.add("URI");                   // column 24

        results.add(p.getLocalNameWithPrefix()); // column 1
        
        String parentPropertyStr = "";
        if (p.getParentURI() != null) {
        	ObjectProperty parent = propDao.getObjectPropertyByURI(p.getParentURI());
        	if (parent != null && parent.getURI() != null) {
        		try {
        			parentPropertyStr = "<a href=\"propertyEdit?uri="+URLEncoder.encode(parent.getURI(),"UTF-8")+"\">"+parent.getLocalNameWithPrefix()+"</a>";
        		} catch (UnsupportedEncodingException e) {
        		    log.error(e, e);
        		}
        	}
    	} 
        results.add(parentPropertyStr); // column 2
        
        if (p.getGroupURI() != null) {
            PropertyGroup pGroup = pgDao.getGroupByURI(p.getGroupURI());
            if (pGroup != null){
                results.add(pGroup.getName()); // column 3
            } else {
                results.add("(unnamed group)"); // column 3
            }
        } else {
            results.add("(unspecified)"); // column 3
        }
        
        String ontologyName = null;
        if (p.getNamespace() != null) {
            Ontology ont = vreq.getUnfilteredWebappDaoFactory().getOntologyDao().getOntologyByURI(p.getNamespace());
            if ( (ont != null) && (ont.getName() != null) ) {
                ontologyName = ont.getName();
            }
        }
        results.add(ontologyName==null ? "(not identified)" : ontologyName); // column 4
        
        results.add(p.getLocalName());  // column 5
        
        results.add(p.getDomainPublic() == null ? "(no public label)" : p.getDomainPublic()); // column 6
        
        String domainStr = ""; 
        if (p.getDomainVClassURI() != null) {
        	VClass domainClass = vcDao.getVClassByURI(p.getDomainVClassURI());
        	if (domainClass != null && domainClass.getURI() != null && domainClass.getLocalNameWithPrefix() != null) {
        		try {
        			if (domainClass.isAnonymous()) {
        				domainStr = domainClass.getLocalNameWithPrefix();
        			} else {
        				domainStr = "<a href=\"vclassEdit?uri="+URLEncoder.encode(domainClass.getURI(),"UTF-8")+"\">"+domainClass.getLocalNameWithPrefix()+"</a>";
        			}
        		} catch (UnsupportedEncodingException e) {
        		    log.error(e, e);
        		}
        	}
        }
        results.add(domainStr); // column 7
        
        String rangeStr = ""; 
        if (p.getRangeVClassURI() != null) {
        	VClass rangeClass = vcDao.getVClassByURI(p.getRangeVClassURI());
        	if (rangeClass != null && rangeClass.getURI() != null && rangeClass.getLocalNameWithPrefix() != null) {
        		try {
        			if (rangeClass.isAnonymous()) {
        				rangeStr = rangeClass.getLocalNameWithPrefix();
        			} else {
        				rangeStr = "<a href=\"vclassEdit?uri="+URLEncoder.encode(rangeClass.getURI(),"UTF-8")+"\">"+rangeClass.getLocalNameWithPrefix()+"</a>";
        			}
        		} catch (UnsupportedEncodingException e) {
        		    log.error(e, e);
        		}
        	}
        }
        results.add(rangeStr); // column 8
        
        results.add(p.getTransitive() ? "true" : "false");        // column 9
        results.add(p.getSymmetric() ? "true" : "false");         // column 10
        results.add(p.getFunctional() ? "true" : "false");        // column 11
        results.add(p.getInverseFunctional() ? "true" : "false"); // column 12
        
        String publicDescriptionStr = (p.getPublicDescription() == null) ? "" : p.getPublicDescription();
        results.add(publicDescriptionStr);     // column 13
        String exampleStr = (p.getExample() == null) ? "" : p.getExample();
        results.add(exampleStr);               // column 14
        String descriptionStr = (p.getDescription() == null) ? "" : p.getDescription();
        results.add(descriptionStr);           // column 15
        
        results.add(p.getHiddenFromDisplayBelowRoleLevel() == null ? "(unspecified)" : p.getHiddenFromDisplayBelowRoleLevel().getLabel()); // column 16
        results.add(p.getProhibitedFromUpdateBelowRoleLevel() == null ? "(unspecified)" : p.getProhibitedFromUpdateBelowRoleLevel().getLabel()); // column 17
        
        results.add("property: "+p.getDomainDisplayTier() + ", inverse: "+p.getRangeDisplayTier()); // column 18
        
        results.add(p.getCollateBySubclass() ? "true" : "false"); // column 19
 
        results.add(p.getCustomEntryForm() == null ? "(unspecified)" : p.getCustomEntryForm()); // column 20
        results.add(p.getSelectFromExisting() ? "true" : "false");   // column 21
        results.add(p.getOfferCreateNewOption() ? "true" : "false"); // column 22
        
        /*
        String datapropStr = ""; 
        if (p.getObjectIndividualSortPropertyURI() != null) {
            DataProperty dProp = dpDao.getDataPropertyByURI(p.getObjectIndividualSortPropertyURI());
            if (dProp != null && dProp.getURI() != null && dProp.getLocalNameWithPrefix() != null) {
                try {
                    datapropStr = "<a href=\"datapropEdit?uri="+URLEncoder.encode(dProp.getURI(),"UTF-8")+"\">"+dProp.getLocalNameWithPrefix()+"</a>";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            results.add(datapropStr); // column 16
        } else {
            results.add("name (rdfs:label)"); // column 16
        }
        */
        results.add(p.getDomainEntitySortDirection() == null ? "ascending" : p.getDomainEntitySortDirection()); // column 23

        results.add(p.getURI()); // column 24
        request.setAttribute("results",results);
        request.setAttribute("columncount",NUM_COLS);
        request.setAttribute("suppressquery","true");


        boolean FORCE_NEW = true;

        EditProcessObject epo = super.createEpo(request, FORCE_NEW);
        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        // superproperties and subproperties
        
        ObjectPropertyDao opDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getObjectPropertyDao();
        List superURIs = opDao.getSuperPropertyURIs(p.getURI(),false);
        List superProperties = new ArrayList();
        Iterator superURIit = superURIs.iterator();
        while (superURIit.hasNext()) {
            String superURI = (String) superURIit.next();
            if (superURI != null) {
                ObjectProperty superProperty = opDao.getObjectPropertyByURI(superURI);
                if (superProperty != null) {
                    superProperties.add(superProperty);
                }
            }
        }
        request.setAttribute("superproperties",superProperties);

        List subURIs = opDao.getSubPropertyURIs(p.getURI());
        List subProperties = new ArrayList();
        Iterator subURIit = subURIs.iterator();
        while (subURIit.hasNext()) {
            String subURI = (String) subURIit.next();
            ObjectProperty subProperty = opDao.getObjectPropertyByURI(subURI);
            if (subProperty != null) {
                subProperties.add(subProperty);
            }
        }
        request.setAttribute("subproperties",subProperties);
        
        List eqURIs = opDao.getEquivalentPropertyURIs(p.getURI());
        List eqProperties = new ArrayList();
        Iterator eqURIit = eqURIs.iterator();
        while (eqURIit.hasNext()) {
            String eqURI = (String) eqURIit.next();
            ObjectProperty eqProperty = opDao.getObjectPropertyByURI(eqURI);
            if (eqProperty != null) {
                eqProperties.add(eqProperty);
            }
        }
        request.setAttribute("equivalentProperties", eqProperties);
        
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("propertyWebapp", p);
        request.setAttribute("bodyJsp","/templates/edit/specific/props_edit.jsp");
        request.setAttribute("title","Object Property Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("PropertyEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}
