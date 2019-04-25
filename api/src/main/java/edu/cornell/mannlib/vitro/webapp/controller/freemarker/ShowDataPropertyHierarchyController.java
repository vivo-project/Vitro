/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ShowDataPropertyHierarchyController", urlPatterns = {"/showDataPropertyHierarchy"} )
public class ShowDataPropertyHierarchyController extends FreemarkerHttpServlet {

	private static final Log log = LogFactory.getLog(ShowDataPropertyHierarchyController.class.getName());

    private static final String TEMPLATE_NAME = "siteAdmin-objectPropHierarchy.ftl";
    private int MAXDEPTH = 5;

    private DataPropertyDao dpDao = null;
    private DataPropertyDao dpDaoLangNeut = null;
    private VClassDao vcDao = null;
    private VClassDao vcDaoLangNeut = null;
    private PropertyGroupDao pgDao = null;
    private DatatypeDao dDao = null;

    private int previous_posn = 0;

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            String displayOption = "";

            if ( vreq.getParameter("displayOption") != null ) {
                displayOption = vreq.getParameter("displayOption");
            }
            else {
                displayOption = "hierarchy";
            }
            body.put("displayOption", displayOption);

            if ( displayOption.equals("all") ) {
                body.put("pageTitle", "All Data Properties");
            }
            else {
                body.put("pageTitle", "Data Property Hierarchy");
            }

            body.put("propertyType", "data");

            dpDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getDataPropertyDao();
            dpDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getDataPropertyDao();
            vcDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getVClassDao();
            vcDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getVClassDao();
            pgDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getPropertyGroupDao();
            dDao = vreq.getUnfilteredAssertionsWebappDaoFactory().getDatatypeDao();

            StringBuilder json = new StringBuilder();

            String ontologyUri = vreq.getParameter("ontologyUri");
            String startPropertyUri = vreq.getParameter("propertyUri");

            List<DataProperty> roots = null;

            if (startPropertyUri != null) {
        	    roots = new LinkedList<DataProperty>();
        	    roots.add(dpDao.getDataPropertyByURI(startPropertyUri));
            } else {
                roots = dpDao.getRootDataProperties();
                if (roots!=null){
                    sortForPickList(roots, vreq);
                }
            }

            int counter = 0;

            if (roots!=null) {
                Iterator<DataProperty> rootIt = roots.iterator();
                if (!rootIt.hasNext()) {
                    DataProperty dp = new DataProperty();
                    dp.setURI(ontologyUri + "fake");
                    String notFoundMessage = "<strong>No data properties found.</strong>";
                    dp.setName(notFoundMessage);
                    dp.setName(notFoundMessage);
                    json.append(addDataPropertyDataToResultsList(dp, 0, ontologyUri, counter));
                } else {
                    while (rootIt.hasNext()) {
                        DataProperty root = rootIt.next();
                        if ( (ontologyUri==null) || ( (ontologyUri!=null) && (root.getNamespace()!=null) && (ontologyUri.equals(root.getNamespace())) ) ) {
                    	    json.append(addChildren(root, 0, ontologyUri, counter, vreq));
                    	    counter += 1;
                	    }
                    }
                    int length = json.length();
                    if ( length > 0 ) {
                        json.append(" }");
                    }
                }
            }

            body.put("jsonTree", json.toString());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

    private String addChildren(DataProperty parent, int position, String ontologyUri,
            int counter, VitroRequest vreq) {
    	if (parent == null) {
    		return "";
    	}
        String details = addDataPropertyDataToResultsList(parent, position, ontologyUri, counter);
        int length = details.length();
        StringBuilder leaves = new StringBuilder();
        leaves.append(details);
        List<String> childURIstrs = dpDao.getSubPropertyURIs(parent.getURI());
        if ( (childURIstrs.size() > 0) && (position < MAXDEPTH) ) {
            List<DataProperty> childProps = new ArrayList<DataProperty>();
            for (String URIstr : childURIstrs) {
                DataProperty child = dpDao.getDataPropertyByURI(URIstr);
                childProps.add(child);
            }
            sortForPickList(childProps, vreq);
            Iterator<DataProperty> childPropIt = childProps.iterator();
            while (childPropIt.hasNext()) {
                DataProperty child = childPropIt.next();
                leaves.append(addChildren(child, position + 1, ontologyUri, counter, vreq));
                if (!childPropIt.hasNext()) {
                    if ( ontologyUri == null ) {
                        leaves.append(" }] ");
                    }
                    else if ( ontologyUri != null && length > 0 ) {
                        // need this for when we show the classes associated with an ontology
                        String ending = leaves.substring(leaves.length() - 2, leaves.length());
                        switch (ending) {
                            case "] ":
                                leaves.append("}]");
                                break;
                            case " [":
                                leaves.append("] ");
                                break;
                            default:
                                leaves.append("}]");
                                break;
                        }
                    }
                }
            }
        }
        else {
            if ( ontologyUri == null ) {
                 leaves.append("] ");
            }
            else if ( ontologyUri != null && length > 0 ) {
                 leaves.append("] ");
            }
        }
        return leaves.toString();
    }

    private String addDataPropertyDataToResultsList(DataProperty dp, int position, String ontologyUri, int counter) {
        String tempString = "";
        if (dp == null) {
        	return tempString;
        }
        if (ontologyUri == null || ( (dp.getNamespace()!=null) && (dp.getNamespace().equals(ontologyUri)) ) ) {
            if ( counter < 1 && position < 1 ) {
                 tempString += "{ \"name\": ";
            }
            else if ( position == previous_posn ) {
                        tempString += "}, { \"name\": ";
            }
            else if ( position > previous_posn ) {
                tempString += " { \"name\": ";
            }
            else if ( position < previous_posn ) {
                tempString += "}, { \"name\": ";
            }

            String nameStr = dp.getPickListName() == null
                    ? dp.getName() == null
                            ? dp.getURI() == null
                                    ? "(no name)" : dp.getURI() : dp.getName() : dp.getPickListName();

            tempString += JacksonUtils.quote(
                    "<a href='datapropEdit?uri=" + URLEncoder.encode(
                            dp.getURI()) + "'>" + nameStr + "</a>") + ", ";

            tempString += "\"data\": { \"internalName\": " + JacksonUtils.quote(
                    dp.getPickListName()) + ", ";

            DataProperty dpLangNeut = dpDaoLangNeut.getDataPropertyByURI(dp.getURI());
            if(dpLangNeut == null) {
                dpLangNeut = dp;
            }
            String domainStr = getVClassNameFromURI(dpLangNeut.getDomainVClassURI(), vcDao, vcDaoLangNeut);

            try {
            	tempString += "\"domainVClass\": " + JacksonUtils.quote(domainStr) + ", " ;
            } catch (NullPointerException e) {
            	tempString += "\"domainVClass\": \"\",";
            }
            try {
            	Datatype rangeDatatype = dDao.getDatatypeByURI(dp.getRangeDatatypeURI());
                String rangeDatatypeStr = (rangeDatatype==null)?dp.getRangeDatatypeURI():rangeDatatype.getName();
            	tempString += "\"rangeVClass\": " + JacksonUtils.quote((rangeDatatypeStr != null) ? rangeDatatypeStr : "") + ", " ;
            } catch (NullPointerException e) {
            	tempString += "\"rangeVClass\": \"\",";
            }
            if (dp.getGroupURI() != null) {
                PropertyGroup pGroup = pgDao.getGroupByURI(dp.getGroupURI());
                tempString += "\"group\": " + JacksonUtils.quote((pGroup == null) ? "unknown group" : pGroup.getName());
            } else {
                tempString += "\"group\": \"unspecified\"";
            }
            tempString += "}, \"children\": [";

            previous_posn = position;
       }
        return tempString;
    }

    private String getVClassNameFromURI(String vclassURI, VClassDao vcDao, VClassDao vcDaoLangNeut) {
        if(vclassURI == null) {
            return "";
        }
        VClass vclass = vcDaoLangNeut.getVClassByURI(vclassURI);
        if(vclass == null) {
            return "";
        }
        if(vclass.isAnonymous()) {
            return vclass.getPickListName();
        } else {
            VClass vclassWLang = vcDao.getVClassByURI(vclassURI);
            return (vclassWLang != null) ? vclassWLang.getPickListName() : vclass.getPickListName();
        }
    }

}
