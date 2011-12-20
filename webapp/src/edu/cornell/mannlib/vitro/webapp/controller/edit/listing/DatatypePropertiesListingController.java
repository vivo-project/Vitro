/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class DatatypePropertiesListingController extends BaseEditController {
    private final int NUM_COLS = 9;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);

        String noResultsMsgStr = "No data properties found";

        String ontologyUri = request.getParameter("ontologyUri");

        DataPropertyDao dao = vrequest.getFullWebappDaoFactory().getDataPropertyDao();
        VClassDao vcDao = vrequest.getFullWebappDaoFactory().getVClassDao();
        DatatypeDao dDao = vrequest.getFullWebappDaoFactory().getDatatypeDao();
        PropertyGroupDao pgDao = vrequest.getFullWebappDaoFactory().getPropertyGroupDao();

        List<DataProperty> props = new ArrayList<DataProperty>();

        if (request.getParameter("propsForClass") != null) {
            noResultsMsgStr = "There are no data properties that apply to this class.";
            Collection <DataProperty> dataProps = dao.getDataPropertiesForVClass(request.getParameter("vclassUri"));
            Iterator<DataProperty> dataPropIt = dataProps.iterator();
            HashSet<String> propURIs = new HashSet<String>();
            while (dataPropIt.hasNext()) {
                DataProperty dp = dataPropIt.next();
                if (!(propURIs.contains(dp.getURI()))) {
                    propURIs.add(dp.getURI());
                    DataProperty prop = dao.getDataPropertyByURI(dp.getURI());
                    if (prop != null) {
                        props.add(prop);
                    }
                }
            }
        } else {
        	props = dao.getAllDataProperties();
        }

        if (ontologyUri != null) {
            List<DataProperty> scratch = new ArrayList<DataProperty>();
            for (DataProperty p: props) {
                if (p.getNamespace().equals(ontologyUri)) {
                    scratch.add(p);
                }
            }
            props = scratch;
        }

        if (props != null) {
        	Collections.sort(props);
        }

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");             // column 1
        results.add("Data Property");  // column 2
        results.add("domain");         // column 3
        results.add("range datatype"); // column 4
        results.add("group");          // column 5
        results.add("display tier");   // column 6
        results.add("display limit");  // column 7
        results.add("display level");  // column 8
        results.add("update level");   // column 9


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
            	for (DataProperty prop: props) {
                    results.add("XX"); // column 1
                    String nameStr = prop.getPublicName()==null ? prop.getName()==null ? prop.getURI()==null ? "(no name)" : prop.getURI() : prop.getName() : prop.getPublicName();
                    try {
                        results.add("<a href=\"datapropEdit?uri="+URLEncoder.encode(prop.getURI(),"UTF-8")+"\">"+nameStr+"</a> <span style='font-style:italic; color:\"grey\";'>"+prop.getLocalNameWithPrefix()+"</span>"); // column 2
                    } catch (Exception e) {
                        results.add(nameStr + " <span style='font-style:italic; color:\"grey\";'>" + prop.getLocalNameWithPrefix() + "</span>"); // column 2
                    }
                    VClass vc = null;
                    String domainStr="";
                    if (prop.getDomainClassURI() != null) {
                        vc = vcDao.getVClassByURI(prop.getDomainClassURI());
                        if (vc != null) {
                            try {
                                domainStr="<a href=\"vclassEdit?uri="+URLEncoder.encode(prop.getDomainClassURI(),"UTF-8")+"\">"+vc.getName()+"</a>";
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    results.add(domainStr);

                    Datatype rangeDatatype = dDao.getDatatypeByURI(prop.getRangeDatatypeURI());
                    String rangeDatatypeStr = (rangeDatatype==null)?prop.getRangeDatatypeURI():rangeDatatype.getName();
                    results.add((rangeDatatypeStr==null)?"<i>untyped</i>":rangeDatatypeStr); // column 4
                    if (prop.getGroupURI() != null) {
                        PropertyGroup pGroup = pgDao.getGroupByURI(prop.getGroupURI());
                        results.add((pGroup == null) ? "unknown group" : pGroup.getName()); // column 5
                    } else {
                        results.add("unspecified");
                    }
                    Integer displayTier = prop.getDisplayTier();
                    String displayTierStr = (displayTier < 0) 
                            ? "" 
                            : Integer.toString(displayTier);  
                    results.add(displayTierStr); // column 6
                    Integer displayLimit = prop.getDisplayLimit();
                    String displayLimitStr = (displayLimit < 0) 
                            ? "" 
                            : Integer.toString(displayLimit);  
                    results.add(displayLimitStr); // column 7
                    results.add(prop.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : prop.getHiddenFromDisplayBelowRoleLevel().getShorthand()); // column 8
                    results.add(prop.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : prop.getProhibitedFromUpdateBelowRoleLevel().getShorthand()); // column 9
                }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Data Properties");
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new data property");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Dataprop");
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
    private class DatatypePropertyAlphaComparator implements Comparator {
        public int compare (Object o1, Object o2) {
            Collator collator = Collator.getInstance();
            DataProperty dp1 = (DataProperty) o1;
            DataProperty dp2 = (DataProperty) o2;
            String dp1Str = (dp1.getPublicName()==null) ? dp1.getName() : dp1.getPublicName();
            dp1Str = (dp1Str == null) ? "" : dp1Str;
            String dp2Str = (dp2.getPublicName()==null) ? dp2.getName() : dp2.getPublicName();
            dp2Str = (dp2Str == null) ? "" : dp2Str;
            return collator.compare(dp1Str,dp2Str);
        }
    }
    */

}
