/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.beans.ButtonForm;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class IndividualsListingController extends BaseEditController {

    //private static final int MAX_INDIVIDUALS = 50;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new EditOntology()))) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);

        //need to figure out how to structure the results object to put the classes underneath

        String assertedOnlyStr = request.getParameter("assertedOnly");
        
        WebappDaoFactory wadf = null;
        
        if (assertedOnlyStr != null && assertedOnlyStr.equalsIgnoreCase("true")) {
        	wadf = vrequest.getAssertionsWebappDaoFactory();
        }
        if (wadf == null) {
        	wadf = vrequest.getFullWebappDaoFactory();
        }
        
        IndividualDao dao = wadf.getIndividualDao();
        VClassDao vcDao = wadf.getVClassDao();

        String vclassURI = request.getParameter("VClassURI");
        VClass vc = vcDao.getVClassByURI(vclassURI);
        
        List inds = dao.getIndividualsByVClassURI(vclassURI);
        //List inds = dao.getIndividualsByVClassURI(vclassURI,1,MAX_INDIVIDUALS);

        ArrayList results = new ArrayList();
        results.add("XX");
        results.add("Individual");
        results.add("class");

        if (inds != null && inds.size()>0) {
            Iterator indsIt = inds.iterator();
            while (indsIt.hasNext()) {
                Individual ind = (Individual) indsIt.next();

                results.add("XX");

                if (ind.getName() != null) {
                    try {
                        String individualName = (ind.getName()==null || ind.getName().length()==0) ? ind.getURI() : ind.getName();
                        results.add("<a href=\"./entityEdit?uri="+URLEncoder.encode(ind.getURI(),"UTF-8")+"\">"+individualName+"</a>");
                    } catch (Exception e) {
                        results.add(ind.getName());
                    }
                } else {
                    results.add("");
                }


                if (vc != null) {
                    try {
                        String vclassName = (vc.getName()==null || vc.getName().length()==0) ? vc.getURI() : vc.getName();
                        results.add("<a href=\"./vclassEdit?uri="+URLEncoder.encode(vc.getURI(),"UTF-8")+"\">"+vclassName+"</a>");
                    } catch (Exception e) {
                        results.add(vc.getName());
                    }
                } else {
                    results.add(vclassURI);
                }

            }
        } else {
            results.add("XX");
            results.add("No individuals to display");
        }

        request.setAttribute("results",results);

        request.setAttribute("columncount",new Integer(3));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title", "Individuals in Class "+ ( (vc != null) ? vc.getName() : vclassURI ) );
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);

        // new individual button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newIndividualParams=new HashMap<String,String>();
        newIndividualParams.put("VClassURI",vclassURI);    
        newIndividualParams.put("controller","Entity");
        ButtonForm newIndividualButton = new ButtonForm(Controllers.RETRY_URL,"buttonForm","Add instance",newIndividualParams);
        buttons.add(newIndividualButton);
        request.setAttribute("topButtons", buttons);
          
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

}
