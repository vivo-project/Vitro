/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.beans.ButtonForm;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;

@WebServlet(name = "ObjectPropertyStatementListingController", urlPatterns = {"/listObjectPropertyStatements"} )
public class ObjectPropertyStatementListingController extends
		BaseEditController {

   public void doGet(HttpServletRequest request, HttpServletResponse response) {
       if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
       	return;
       }

        VitroRequest vrequest = new VitroRequest(request);

        boolean assertedStatementsOnly = false;

        String assertParam = request.getParameter("assertedStmts");
        if (assertParam!=null && assertParam.equalsIgnoreCase("true")) {
            assertedStatementsOnly = true;
        }

        boolean showVClasses = false;
        String displayParam = request.getParameter("showVClasses");
        if (displayParam!=null && displayParam.equalsIgnoreCase("true")) {
            showVClasses = true;  // this will trigger a limitation to asserted vclasses, since we can't easily display all vclasses for an individual
        }

        int startAt=1;
        String startAtParam = request.getParameter("startAt");
        if (startAtParam!=null && startAtParam.trim().length()>0) {
            try {
                startAt = Integer.parseInt(startAtParam);
                if (startAt<=0) {
                    startAt = 1;
                }
            } catch(NumberFormatException ex) {
                throw new Error("Cannot interpret "+startAtParam+" as a number");
            }
        }

        int endAt=50;
        String endAtParam = request.getParameter("endAt");
        if (endAtParam!=null && endAtParam.trim().length()>0) {
            try {
                endAt = Integer.parseInt(endAtParam);
                if (endAt<=0) {
                    endAt=1;
                }
                if (endAt<startAt) {
                    int temp = startAt;
                    startAt = endAt;
                    endAt = temp;
                }
            } catch(NumberFormatException ex) {
                throw new Error("Cannot interpret "+endAtParam+" as a number");
            }
        }


        ArrayList<String> results = new ArrayList();

        request.setAttribute("results",results);

        results.add("XX");
        results.add("subject");
        if (showVClasses) results.add("type");
        results.add("property");
        results.add("object");
        if (showVClasses) results.add("type");

        ObjectPropertyStatementDao opsDao = null;
        if (assertedStatementsOnly){ // get only asserted, not inferred, object property statements
            opsDao = vrequest.getUnfilteredAssertionsWebappDaoFactory().getObjectPropertyStatementDao();
        } else {
            opsDao = vrequest.getUnfilteredWebappDaoFactory().getObjectPropertyStatementDao();
        }

        // get all object properties -- no concept of asserted vs. inferred object properties
        ObjectPropertyDao opDao = vrequest.getUnfilteredWebappDaoFactory().getObjectPropertyDao();

        IndividualDao iDao = null;
        if (showVClasses) {
            iDao = vrequest.getUnfilteredAssertionsWebappDaoFactory().getIndividualDao();
        } else {
            iDao = vrequest.getUnfilteredWebappDaoFactory().getIndividualDao();
        }

        String propURIStr = request.getParameter("propertyURI");

        ObjectProperty op = opDao.getObjectPropertyByURI(propURIStr);

        int count = 0;


       for (ObjectPropertyStatement objectPropertyStatement : opsDao.getObjectPropertyStatements(op, startAt, endAt)) {
           count++;
           ObjectPropertyStatement ops = objectPropertyStatement;
           Individual subj = iDao.getIndividualByURI(ops.getSubjectURI());
           Individual obj = iDao.getIndividualByURI(ops.getObjectURI());
           results.add("XX");
           results.add(ListingControllerWebUtils.formatIndividualLink(subj));
           if (showVClasses) {
               try {
                   results.add(ListingControllerWebUtils.formatVClassLinks(subj.getVClasses(true)));
               } catch (Exception e) {
                   results.add("?");
               }
           }
           results.add(op.getDomainPublic());
           results.add(ListingControllerWebUtils.formatIndividualLink(obj));
           if (showVClasses) {
               try {
                   results.add(ListingControllerWebUtils.formatVClassLinks(obj.getVClasses(true)));
               } catch (Exception e) {
                   results.add("?");
               }
           }
       }

        if (count == 0) {
        	results.add("XX");
        	results.add("No statements found for property \""+op.getPickListName()+"\"");
        	results.add("");
        	results.add("");
        	if (showVClasses) {
        	    results.add("");
        	    results.add("");
        	}
        }

        if (showVClasses){
            request.setAttribute("columncount",new Integer(6));
        } else {
            request.setAttribute("columncount",new Integer(4));
        }
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Object Property Statements");
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newPropParams=new HashMap<String,String>();
        newPropParams.put("controller", "Property");
        ButtonForm newPropButton = new ButtonForm(Controllers.RETRY_URL,"buttonForm","Add new object property",newPropParams);
        buttons.add(newPropButton);
        HashMap<String,String> rootPropParams=new HashMap<String,String>();
        rootPropParams.put("iffRoot", "true");
        ButtonForm rootPropButton = new ButtonForm("showObjectPropertyHierarchy","buttonForm","root properties",rootPropParams);
        buttons.add(rootPropButton);
        request.setAttribute("topButtons", buttons);
        /*
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new object property");
        request.setAttribute("horizontalJspAddButtonControllerParam", "Property");
        */
        try {
            JSPPageHandler.renderBasicPage(request, response, Controllers.HORIZONTAL_JSP);
        } catch (Throwable t) {
            t.printStackTrace();
        }

   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) {
	   // don't post to this controller
   }

}
