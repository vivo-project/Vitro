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
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;

public class DatapropEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(DatapropEditController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

    	VitroRequest vreq = new VitroRequest(request);
    	
        final int NUM_COLS=15;

        String datapropURI = request.getParameter("uri");

        DataPropertyDao dpDao = vreq.getFullWebappDaoFactory().getDataPropertyDao();
        DataProperty dp = dpDao.getDataPropertyByURI(datapropURI);
        PropertyGroupDao pgDao = vreq.getFullWebappDaoFactory().getPropertyGroupDao();

        ArrayList results = new ArrayList();
        results.add("Data Property");
        results.add("ontology");
        results.add("display name");
        results.add("domain");
        results.add("range datatype");
        results.add("group");
        results.add("display tier");
        results.add("display limit");
        results.add("example");
        results.add("description");
        results.add("public description");
        results.add("display level");
        results.add("update level");
        results.add("custom entry form");
        results.add("URI");

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);

        results.add(dp.getLocalNameWithPrefix());
        String ontologyName = null;
        if (dp.getNamespace() != null) {
            Ontology ont = vreq.getFullWebappDaoFactory().getOntologyDao().getOntologyByURI(dp.getNamespace());
            if ( (ont != null) && (ont.getName() != null) ) {
                ontologyName = ont.getName();
            }
        }
        results.add(ontologyName==null ? "(not identified)" : ontologyName);
        results.add(dp.getPublicName() == null ? "(no public name)" : dp.getPublicName());

        // we support parents now, but not the simple getParent() style method
        //String parentPropertyStr = "<i>(datatype properties are not yet modeled in a property hierarchy)</i>"; // TODO - need multiple inheritance
        //results.add(parentPropertyStr);
        
        // TODO - need unionOf/intersectionOf-style domains for domain class
        String domainStr="";
        try {
            domainStr = (dp.getDomainClassURI() == null) ? "" : "<a href=\"vclassEdit?uri="+URLEncoder.encode(dp.getDomainClassURI(),"UTF-8")+"\">"+dp.getDomainClassURI()+"</a>";
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        results.add(domainStr);
        
        String rangeStr = (dp.getRangeDatatypeURI() == null) ? "<i>untyped</i> (rdfs:Literal)" : dp.getRangeDatatypeURI(); // TODO
        results.add(rangeStr);
        if (dp.getGroupURI() != null) {
            PropertyGroup pGroup = pgDao.getGroupByURI(dp.getGroupURI());
            if (pGroup != null) {
            	results.add(pGroup.getName());
            } else {
            	results.add(dp.getGroupURI());
            }
        } else {
            results.add("unspecified");
        }
        results.add(String.valueOf(dp.getDisplayTier()));
        results.add(String.valueOf(dp.getDisplayLimit()));
        String exampleStr = (dp.getExample() == null) ? "" : dp.getExample();
        results.add(exampleStr);
        String descriptionStr = (dp.getDescription() == null) ? "" : dp.getDescription();
        results.add(descriptionStr);
        String publicDescriptionStr = (dp.getPublicDescription() == null) ? "" : dp.getPublicDescription();
        results.add(publicDescriptionStr);        
        results.add(dp.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : dp.getHiddenFromDisplayBelowRoleLevel().getLabel());
        results.add(dp.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : dp.getProhibitedFromUpdateBelowRoleLevel().getLabel());
        results.add(dp.getCustomEntryForm() == null ? "unspecified" : dp.getCustomEntryForm());
        results.add(dp.getURI() == null ? "" : dp.getURI());
        request.setAttribute("results",results);
        request.setAttribute("columncount",NUM_COLS);
        request.setAttribute("suppressquery","true");

        boolean FORCE_NEW = true;
        
        EditProcessObject epo = super.createEpo(request, FORCE_NEW);
        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();
        // add the options
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        DataPropertyDao assertionsDpDao = (vreq.getAssertionsWebappDaoFactory() != null) 
            ? vreq.getAssertionsWebappDaoFactory().getDataPropertyDao()
            : vreq.getFullWebappDaoFactory().getDataPropertyDao();
        
        List superURIs = assertionsDpDao.getSuperPropertyURIs(dp.getURI(),false);
        List superProperties = new ArrayList();
        Iterator superURIit = superURIs.iterator();
        while (superURIit.hasNext()) {
            String superURI = (String) superURIit.next();
            if (superURI != null) {
                DataProperty superProperty = assertionsDpDao.getDataPropertyByURI(superURI);
                if (superProperty != null) {
                    superProperties.add(superProperty);
                }
            }
        }
        request.setAttribute("superproperties",superProperties);

        List subURIs = assertionsDpDao.getSubPropertyURIs(dp.getURI());
        List subProperties = new ArrayList();
        Iterator subURIit = subURIs.iterator();
        while (subURIit.hasNext()) {
            String subURI = (String) subURIit.next();
            DataProperty subProperty = dpDao.getDataPropertyByURI(subURI);
            if (subProperty != null) {
                subProperties.add(subProperty);
            }
        }
        request.setAttribute("subproperties",subProperties);
        
        List eqURIs = assertionsDpDao.getEquivalentPropertyURIs(dp.getURI());
        List eqProperties = new ArrayList();
        Iterator eqURIit = eqURIs.iterator();
        while (eqURIit.hasNext()) {
            String eqURI = (String) eqURIit.next();
            DataProperty eqProperty = dpDao.getDataPropertyByURI(eqURI);
            if (eqProperty != null) {
                eqProperties.add(eqProperty);
            }
        }
        request.setAttribute("equivalentProperties", eqProperties);
        
        ApplicationBean appBean = vreq.getAppBean();
        
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("datatypeProperty", dp);
        request.setAttribute("bodyJsp","/templates/edit/specific/dataprops_edit.jsp");
        request.setAttribute("title","Data Property Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+appBean.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("DatapropEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}