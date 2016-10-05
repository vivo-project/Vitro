/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class PropertyEditController extends BaseEditController {

	private static final Log log = LogFactory.getLog(PropertyEditController.class.getName());
	
    @Override
	public void doPost (HttpServletRequest request, HttpServletResponse response) {
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

        final int NUM_COLS=26;

        VitroRequest vreq = new VitroRequest(request);
        
        ObjectPropertyDao propDao = vreq.getUnfilteredWebappDaoFactory().getObjectPropertyDao();
        ObjectPropertyDao propDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getObjectPropertyDao();
        VClassDao vcDao = vreq.getLanguageNeutralWebappDaoFactory().getVClassDao();
        VClassDao vcDaoWLang = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
        PropertyGroupDao pgDao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();
        ObjectProperty p = propDao.getObjectPropertyByURI(request.getParameter("uri"));
        ObjectProperty pLangNeut = propDaoLangNeut.getObjectPropertyByURI(request.getParameter("uri"));
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
        results.add("display limit");         // column 15
        results.add("collate by subclass");   // column 19
        results.add("custom entry form");     // column 20
        results.add("select from existing");  // column 21
        results.add("offer create new");      // column 22
        results.add("sort direction");        // column 23
        results.add("URI");                   // column 24
        results.add("publish level");         // column 25

        results.add(p.getPickListName()); // column 1
        
        String parentPropertyStr = "";
        if (p.getParentURI() != null) {
        	ObjectProperty parent = propDao.getObjectPropertyByURI(p.getParentURI());
        	if (parent != null && parent.getURI() != null) {
        		try {
        			parentPropertyStr = "<a href=\"propertyEdit?uri="+URLEncoder.encode(parent.getURI(),"UTF-8")+"\">"+parent.getPickListName()+"</a>";
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
        if (pLangNeut.getDomainVClassURI() != null) {
        	VClass domainClass = vcDao.getVClassByURI(pLangNeut.getDomainVClassURI());
        	VClass domainWLang = vcDaoWLang.getVClassByURI(pLangNeut.getDomainVClassURI()); 
        	if (domainClass != null && domainClass.getURI() != null && domainClass.getPickListName() != null) {
        		try {
        			if (domainClass.isAnonymous()) {
        				domainStr = domainClass.getPickListName();
        			} else {
        				domainStr = "<a href=\"vclassEdit?uri="+URLEncoder.encode(domainClass.getURI(),"UTF-8")+"\">"+domainWLang.getPickListName()+"</a>";
        			}
        		} catch (UnsupportedEncodingException e) {
        		    log.error(e, e);
        		}
        	}
        }
        results.add(domainStr); // column 7
        
        String rangeStr = ""; 
        if (pLangNeut.getRangeVClassURI() != null) {
        	VClass rangeClass = vcDao.getVClassByURI(pLangNeut.getRangeVClassURI());
        	VClass rangeWLang = vcDaoWLang.getVClassByURI(pLangNeut.getRangeVClassURI()); 
        	if (rangeClass != null && rangeClass.getURI() != null && rangeClass.getPickListName() != null) {
        		try {
        			if (rangeClass.isAnonymous()) {
        				rangeStr = rangeClass.getPickListName();
        			} else {
        				rangeStr = "<a href=\"vclassEdit?uri="+URLEncoder.encode(rangeClass.getURI(),"UTF-8")+"\">"+rangeWLang.getPickListName()+"</a>";
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
        
		results.add(p.getHiddenFromDisplayBelowRoleLevel() == null ? "(unspecified)"
				: p.getHiddenFromDisplayBelowRoleLevel().getDisplayLabel()); // column 16
		results.add(p.getProhibitedFromUpdateBelowRoleLevel() == null ? "(unspecified)"
				: p.getProhibitedFromUpdateBelowRoleLevel().getUpdateLabel()); // column 17
        
        results.add("property: "+p.getDomainDisplayTier() + ", inverse: "+p.getRangeDisplayTier()); // column 18
        results.add("property: "+p.getDomainDisplayLimitInteger() + ", inverse: "+p.getRangeDisplayLimit());
        results.add(p.getCollateBySubclass() ? "true" : "false"); // column 19
 
        results.add(p.getCustomEntryForm() == null ? "(unspecified)" : p.getCustomEntryForm()); // column 20
        results.add(p.getSelectFromExisting() ? "true" : "false");   // column 21
        results.add(p.getOfferCreateNewOption() ? "true" : "false"); // column 22
        
        results.add(p.getDomainEntitySortDirection() == null ? "ascending" : p.getDomainEntitySortDirection()); // column 23

        results.add(p.getURI()); // column 24
		results.add(p.getHiddenFromPublishBelowRoleLevel() == null ? "(unspecified)"
				: p.getHiddenFromPublishBelowRoleLevel().getDisplayLabel()); // column 25
        request.setAttribute("results",results);
        request.setAttribute("columncount",NUM_COLS);
        request.setAttribute("suppressquery","true");


        EditProcessObject epo = super.createEpo(request, FORCE_NEW);
        FormObject foo = new FormObject();
        HashMap<String, List<Option>> OptionMap = new HashMap<>();
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        // superproperties and subproperties
        
        ObjectPropertyDao opDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getObjectPropertyDao();
        List<ObjectProperty> superProps = getObjectPropertiesForURIList(
                opDao.getSuperPropertyURIs(p.getURI(), false), opDao);
        sortForPickList(superProps, vreq);
        request.setAttribute("superproperties", superProps);

        List<ObjectProperty> subProps = getObjectPropertiesForURIList(
                opDao.getSubPropertyURIs(p.getURI()), opDao);
        sortForPickList(subProps, vreq);
        request.setAttribute("subproperties", subProps);
        
        // equivalent properties and faux properties 
        
        List<ObjectProperty> eqProps = getObjectPropertiesForURIList(
                opDao.getEquivalentPropertyURIs(p.getURI()), opDao);
        sortForPickList(eqProps, vreq);
        request.setAttribute("equivalentProperties", eqProps);
        
        List<FauxProperty> fauxProps = vreq.getUnfilteredAssertionsWebappDaoFactory().getFauxPropertyDao().
        		getFauxPropertiesForBaseUri(p.getURI());
        sortForPickList(fauxProps, vreq);
        request.setAttribute("fauxproperties", fauxProps);
        
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("propertyWebapp", p);
        request.setAttribute("title","Object Property Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/specific/props_edit.jsp");
        } catch (Exception e) {
            log.error("PropertyEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }
    
    private List<ObjectProperty> getObjectPropertiesForURIList(List<String> propertyURIs, 
            ObjectPropertyDao opDao) {
        List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
        for (String propertyURI : propertyURIs) {
            ObjectProperty property = opDao.getObjectPropertyByURI(propertyURI);
            if (property != null) {
                properties.add(property);
            }
        }
        return properties;
    }

}
