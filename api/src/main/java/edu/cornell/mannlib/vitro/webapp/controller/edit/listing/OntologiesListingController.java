/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;

public class OntologiesListingController extends BaseEditController {

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
    		return;
    	}
    	
        VitroRequest vrequest = new VitroRequest(request);

        //need to figure out how to structure the results object to put the classes underneath

	    String noResultsMsgStr = "No ontologies found";

        OntologyDao dao = vrequest.getUnfilteredWebappDaoFactory().getOntologyDao();

        List<Ontology> onts = dao.getAllOntologies();

        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");
        results.add("Ontology");
        results.add("Namespace");
        results.add("Prefix");

        if (onts != null && onts.size()>0) {
        	for (Ontology ont: onts) {
                results.add("XX");
                if (ont.getName() != null) {
                    try {
                        String ontologyName = (ont.getName()==null || ont.getName().length()==0) ? ont.getURI() : ont.getName();
                        results.add("<a href=\"./ontologyEdit?uri="+URLEncoder.encode(ont.getURI(),"UTF-8")+"\">"+ontologyName+"</a>");
                    } catch (Exception e) {
                        results.add(ont.getName());
                    }
                } else {
                    results.add("");
                }
                results.add(ont.getURI() == null ? "" : ont.getURI());
                results.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
            }
        } else {
	        results.add("XX");
	        results.add("<strong>"+noResultsMsgStr+"</strong>");
	    }
	    request.setAttribute("results",results);

        request.setAttribute("columncount",new Integer(4));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Ontologies");
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new ontology");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Ontology");
        try {
            JSPPageHandler.renderBasicPage(request, response, Controllers.HORIZONTAL_JSP);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

}
