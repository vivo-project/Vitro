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
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class DatapropEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(DatapropEditController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

    	VitroRequest vreq = new VitroRequest(request);
    	
        final int NUM_COLS=18;

        String datapropURI = request.getParameter("uri");

        DataPropertyDao dpDao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyDao();
        DataPropertyDao dpDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getDataPropertyDao();
        VClassDao vcDao = vreq.getLanguageNeutralWebappDaoFactory().getVClassDao();
        VClassDao vcDaoWLang = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
        
        DataProperty dp = dpDao.getDataPropertyByURI(datapropURI);
        DataProperty pLangNeut = dpDaoLangNeut.getDataPropertyByURI(request.getParameter("uri"));
        
        PropertyGroupDao pgDao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();

        ArrayList results = new ArrayList();
        results.add("data property");         // column 1
        results.add("public display label");  // column 2
        results.add("property group");        // column 3
        results.add("ontology");              // column 4
        results.add("RDF local name");        // column 5
        results.add("domain class");          // column 6
        results.add("range datatype");        // column 7
        results.add("functional");            // column 8
        results.add("public description");    // column 9
        results.add("example");               // column 10
        results.add("editor description");    // column 11
        results.add("display level");         // column 12
        results.add("update level");          // column 13
        results.add("display tier");          // column 14
        results.add("display limit");         // column 15
        results.add("custom entry form");     // column 16
        results.add("URI");                   // column 17
        results.add("publish level");         // column 18

        results.add(dp.getPickListName()); // column 1
        results.add(dp.getPublicName() == null ? "(no public label)" : dp.getPublicName()); // column 2
        
        if (dp.getGroupURI() != null) {
            PropertyGroup pGroup = pgDao.getGroupByURI(dp.getGroupURI());
            if (pGroup != null) {
                results.add(pGroup.getName()); // column 3
            } else {
                results.add(dp.getGroupURI());
            }
        } else {
            results.add("(unspecified)");
        }
        
        String ontologyName = null;
        if (dp.getNamespace() != null) {
            Ontology ont = vreq.getUnfilteredWebappDaoFactory().getOntologyDao().getOntologyByURI(dp.getNamespace());
            if ( (ont != null) && (ont.getName() != null) ) {
                ontologyName = ont.getName();
            }
        }
        results.add(ontologyName==null ? "(not identified)" : ontologyName); // column 4

        results.add(dp.getLocalName()); // column 5

        // we support parents now, but not the simple getParent() style method
        //String parentPropertyStr = "<i>(datatype properties are not yet modeled in a property hierarchy)</i>"; // TODO - need multiple inheritance
        //results.add(parentPropertyStr);
        
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
        results.add(domainStr); // column 6

        String rangeStr = (dp.getRangeDatatypeURI() == null) ? "<i>untyped</i> (rdfs:Literal)" : dp.getRangeDatatypeURI();
        results.add(rangeStr); // column 7
        
        results.add(dp.getFunctional() ? "true" : "false"); // column 8
        
        String publicDescriptionStr = (dp.getPublicDescription() == null) ? "" : dp.getPublicDescription(); // column 9
        results.add(publicDescriptionStr);        
        String exampleStr = (dp.getExample() == null) ? "" : dp.getExample();  // column 10
        results.add(exampleStr);
        String descriptionStr = (dp.getDescription() == null) ? "" : dp.getDescription();  // column 11
        results.add(descriptionStr);
        
		results.add(dp.getHiddenFromDisplayBelowRoleLevel() == null ? "(unspecified)"
				: dp.getHiddenFromDisplayBelowRoleLevel().getDisplayLabel()); // column 12
		results.add(dp.getProhibitedFromUpdateBelowRoleLevel() == null ? "(unspecified)"
				: dp.getProhibitedFromUpdateBelowRoleLevel().getUpdateLabel()); // column 13
        results.add(String.valueOf(dp.getDisplayTier()));  // column 14
        results.add(String.valueOf(dp.getDisplayLimit()));  // column 15
        results.add(dp.getCustomEntryForm() == null ? "(unspecified)" : dp.getCustomEntryForm());  // column 16
        results.add(dp.getURI() == null ? "" : dp.getURI()); // column 17
		results.add(dp.getHiddenFromPublishBelowRoleLevel() == null ? "(unspecified)"
				: dp.getHiddenFromPublishBelowRoleLevel().getDisplayLabel()); // column 18
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

        DataPropertyDao assertionsDpDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getDataPropertyDao();
        
        List<DataProperty> superProps = getDataPropertiesForURIList(
                assertionsDpDao.getSuperPropertyURIs(dp.getURI(), false), assertionsDpDao);
        sortForPickList(superProps, vreq);
        request.setAttribute("superproperties", superProps);

        List<DataProperty> subProps = getDataPropertiesForURIList(
                assertionsDpDao.getSubPropertyURIs(dp.getURI()), assertionsDpDao);
        sortForPickList(subProps, vreq);
        request.setAttribute("subproperties", subProps);
        
        List<DataProperty> eqProps = getDataPropertiesForURIList(
                assertionsDpDao.getEquivalentPropertyURIs(dp.getURI()), assertionsDpDao);
        sortForPickList(eqProps, vreq);
        request.setAttribute("equivalentProperties", eqProps);
        
        ApplicationBean appBean = vreq.getAppBean();
        
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("datatypeProperty", dp);
        request.setAttribute("title","Data Property Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+appBean.getThemeDir()+"css/edit.css\"/>");

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/specific/dataprops_edit.jsp");
        } catch (Exception e) {
            log.error("DatapropEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }
    
    private List<DataProperty> getDataPropertiesForURIList(List<String> propertyURIs, 
            DataPropertyDao dpDao) {
        List<DataProperty> properties = new ArrayList<DataProperty>();
        for (String propertyURI : propertyURIs) {
            DataProperty property = dpDao.getDataPropertyByURI(propertyURI);
            if (property != null) {
                properties.add(property);
            }
        }
        return properties;
    }

}