package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlagUpdateController extends BaseEditController {

    private static final Log log = LogFactory.getLog(FlagUpdateController.class.getName());

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
    	
    	String defaultLandingPage = getDefaultLandingPage(request);
    	
        if(!checkLoginStatus(request,response))
            return;

        EditProcessObject epo = super.createEpo(request);

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" encountered exception calling super.doGet()");
        }

        VitroRequest vrequest = new VitroRequest(request);

        String flag1Set  = makeCommaSeparatedList(vrequest.getParameterValues("Flag1Value"));
        String flag2Set  = makeCommaSeparatedList(vrequest.getParameterValues("Flag2Value"));

        Individual ind = ((Individual) epo.getOriginalBean());
        ind.setFlag1Set(flag1Set);
        ind.setFlag2Set(flag2Set);

        ((IndividualDao) epo.getDataAccessObject()).updateIndividual(ind);

        //the referer stuff all will be changed so as not to rely on the HTTP header
        String referer = request.getHeader("REFERER");
        if (referer == null) {
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response.sendRedirect(referer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String makeCommaSeparatedList(String[] values) {
        String listStr = "";
        if (values != null) {
            boolean firstTime = true;
            for (int i=0; i<values.length; i++) {
                if (!firstTime) {
                    listStr += ",";
                } else {
                    firstTime = false;
                }
                listStr += values[i];
            }
        }
        return listStr;
    }

}
