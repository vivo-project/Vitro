/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;

public class DataPropertyStatementListingController extends BaseEditController {

   public void doGet(HttpServletRequest request, HttpServletResponse response) {
       if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
       	return;
       }

        VitroRequest vrequest = new VitroRequest(request);

        String noResultsMsgStr = "No data properties found";

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
        
        ArrayList results = new ArrayList();
        
        request.setAttribute("results",results);
        
        results.add("XX");
        results.add("subject");
        results.add("property");
        results.add("object");
        
        DataPropertyStatementDao dpsDao = vrequest.getUnfilteredWebappDaoFactory().getDataPropertyStatementDao();
        DataPropertyDao dpDao = vrequest.getUnfilteredWebappDaoFactory().getDataPropertyDao();
        IndividualDao iDao = vrequest.getUnfilteredWebappDaoFactory().getIndividualDao();
        
        String propURIStr = request.getParameter("propertyURI");
        
        DataProperty dp = dpDao.getDataPropertyByURI(propURIStr);        
        
        int count = 0;
        
        for (Iterator<DataPropertyStatement> i = dpsDao.getDataPropertyStatements(dp,startAt,endAt).iterator(); i.hasNext();) {
        	count++;
        	DataPropertyStatement dps = i.next();
        	Individual subj = iDao.getIndividualByURI(dps.getIndividualURI());
        	results.add("XX");
        	results.add(ListingControllerWebUtils.formatIndividualLink(subj));
        	results.add(dp.getPublicName());
        	results.add(dps.getData());
        }
        
        if (count == 0) {
        	results.add("XX");
        	results.add("No statements found for property \""+dp.getPublicName()+"\"");
        	results.add("");
        	results.add("");
        }
        
        request.setAttribute("columncount",new Integer(4));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Data Property Statements");
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
