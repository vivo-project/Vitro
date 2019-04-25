/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;

public class AllClassGroupsListingController extends BaseEditController {

    private static final long serialVersionUID = 1L;

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
    		return;
    	}

        VitroRequest vreq = new VitroRequest(request);

        VClassGroupDao dao = vreq.getUnfilteredWebappDaoFactory().getVClassGroupDao();

        List<VClassGroup> groups = dao.getPublicGroupsWithVClasses();
        // uses an unfiltered dao so will see all classes

        List<String> results = new ArrayList<String>();
        results.add("XX");
        results.add("Group");
        results.add("display rank");
        results.add("");
        results.add("XX");

        if (groups != null) {
        	for(VClassGroup vcg: groups) {
                results.add("XX");
                String publicName = vcg.getPublicName();
                if ( StringUtils.isBlank(publicName) ) {
                    publicName = "(unnamed group)";
                }
                try {
                    results.add("<a href=\"./editForm?uri="+URLEncoder.encode(vcg.getURI(),"UTF-8")+"&amp;controller=Classgroup\">"+publicName+"</a>");
                } catch (Exception e) {
                    results.add(publicName);
                }
                Integer t;
                results.add(((t = Integer.valueOf(vcg.getDisplayRank())) != -1) ? t.toString() : "");
                results.add(""); // VClassGroup doesn't yet supprt getModTime()
                results.add("XX");
                List<VClass> classList = vcg.getVitroClassList();
                if (classList != null && classList.size()>0) {
                    results.add("+");
                    results.add("XX");
                    results.add("Class");
                    results.add("example");
                    results.add("description");
                    results.add("@@entities");
                    Iterator<VClass> classIt = classList.iterator();
                    while (classIt.hasNext()) {
                        VClass vcw = classIt.next();
                        results.add("XX");
                        if (vcw.getName() != null && vcw.getURI() != null) {
                            try {
                                results.add("<a href=\"vclassEdit?uri="+URLEncoder.encode(vcw.getURI(),"UTF-8")+"\">"+vcw.getName()+"</a>");
                            } catch (Exception e) {
                                results.add(vcw.getName());
                            }
                        } else {
                            results.add("");
                        }
                        String exampleStr = (vcw.getExample() == null) ? "" : vcw.getName();
                        results.add(exampleStr);
                        String descriptionStr = (vcw.getDescription() == null) ? "" : vcw.getDescription();
                        results.add(descriptionStr);
                        if (classIt.hasNext())
                            results.add("@@entities");
                    }
                }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(5));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Class Groups");
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new class group");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Classgroup");
        try {
            JSPPageHandler.renderBasicPage(request, response, Controllers.HORIZONTAL_JSP);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
