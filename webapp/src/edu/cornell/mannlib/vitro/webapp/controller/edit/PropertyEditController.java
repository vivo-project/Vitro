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

public class PropertyEditController extends BaseEditController {

	private static final Log log = LogFactory.getLog(PropertyEditController.class.getName());
	
    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

        final int NUM_COLS=17;

        VitroRequest vreq = new VitroRequest(request);
        
        ObjectPropertyDao propDao = vreq.getFullWebappDaoFactory().getObjectPropertyDao();
        VClassDao vcDao = vreq.getFullWebappDaoFactory().getVClassDao();
        PropertyGroupDao pgDao = vreq.getFullWebappDaoFactory().getPropertyGroupDao();
        ObjectProperty p = (ObjectProperty)propDao.getObjectPropertyByURI(request.getParameter("uri"));
        request.setAttribute("property",p);

        ArrayList<String> results = new ArrayList<String>();
        results.add("Property");       // column 1
        results.add("parent property"); // column 2
        results.add("domain");         // column 3
        results.add("range");          // column 4
        results.add("display name");   // column 5
        results.add("group");          // column 6
        results.add("display tier");   // column 7
        results.add("example");        // column 8
        results.add("description");    // column 9
        results.add("public description"); // column 10
        results.add("display level"); //column 11
        results.add("update level"); // column 12
        results.add("custom entry form"); // column 13
        results.add("select from existing"); // column 14
        results.add("offer create new option"); // column 15
        results.add("force stub object deletion"); // column 16
        results.add("URI");            // column 17

        String displayName = (p.getDomainPublic()==null) ? p.getLocalName() : p.getDomainPublic();
        try {
            results.add("<a href=\"propertyEdit?uri="+URLEncoder.encode(p.getURI(),"UTF-8")+"\">"+displayName+"</a> <em>"+p.getLocalNameWithPrefix()+"</em>");// column 1
        } catch (UnsupportedEncodingException e) {
            log.error("Could not encode URI for property (domain public: "+p.getDomainPublic()+", local name with prefix: "+p.getLocalNameWithPrefix()+", URI: "+p.getURI()+").");
            results.add(displayName + "<em>"+p.getLocalNameWithPrefix()+"</em>"); // column 1
        }

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
        results.add(domainStr); // column 3
        
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
        results.add(rangeStr); // column 4
        
        results.add(p.getDomainPublic() == null ? "" : p.getDomainPublic()); // column 5
        if (p.getGroupURI() != null) {
            PropertyGroup pGroup = pgDao.getGroupByURI(p.getGroupURI());
            if (pGroup != null){
                results.add(pGroup.getName()); // column 6
            } else {
                results.add("unnamed group"); // column 6
            }
        } else {
            results.add("unspecified"); // column 6
        }
        results.add("domain: "+p.getDomainDisplayTier() + ", range: "+p.getRangeDisplayTier()); // column 7
        String exampleStr = (p.getExample() == null) ? "" : p.getExample();
        results.add(exampleStr); // column 8
        String descriptionStr = (p.getDescription() == null) ? "" : p.getDescription();
        results.add(descriptionStr); // column 9
        String publicDescriptionStr = (p.getPublicDescription() == null) ? "" : p.getPublicDescription();
        results.add(publicDescriptionStr); // column 10
        
        results.add(p.getHiddenFromDisplayBelowRoleLevel() == null ? "unspecified" : p.getHiddenFromDisplayBelowRoleLevel().getLabel()); // column 11
        results.add(p.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : p.getProhibitedFromUpdateBelowRoleLevel().getLabel()); // column 12
 
        results.add(p.getCustomEntryForm() == null ? "unspecified" : p.getCustomEntryForm()); // column 13
        results.add(p.getSelectFromExisting() ? "true" : "false"); // column 14
        results.add(p.getOfferCreateNewOption() ? "true" : "false"); // column 15
        results.add(p.getStubObjectRelation() ? "true" : "false"); // column 16
        results.add(p.getURI()); // column 17
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
        
        ObjectPropertyDao opDao;
        if (vreq.getAssertionsWebappDaoFactory() != null) {
        	opDao = vreq.getAssertionsWebappDaoFactory().getObjectPropertyDao();
        } else {
        	opDao = vreq.getFullWebappDaoFactory().getObjectPropertyDao();
        }
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
