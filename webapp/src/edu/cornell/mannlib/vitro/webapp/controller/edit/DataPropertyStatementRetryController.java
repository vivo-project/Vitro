/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class DataPropertyStatementRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(DataPropertyStatementRetryController.class.getName());
	
    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new DoBackEndEditing()))) {
        	return;
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        String action = "insert";

        VitroRequest vreq = new VitroRequest(request);
        
        DataPropertyStatementDao dataPropertyStatementDao = 
        		vreq.getFullWebappDaoFactory().getDataPropertyStatementDao();
        epo.setDataAccessObject(dataPropertyStatementDao);
        DataPropertyDao dpDao = vreq.getFullWebappDaoFactory().getDataPropertyDao();
        IndividualDao eDao = vreq.getFullWebappDaoFactory().getIndividualDao();
        epo.setBeanClass(DataPropertyStatement.class);

        DataPropertyStatement objectForEditing = null;
        if (!epo.getUseRecycledBean()){
            objectForEditing = new DataPropertyStatementImpl();
            populateBeanFromParams(objectForEditing,vreq);
            if (vreq.getParameter(MULTIPLEXED_PARAMETER_NAME) != null) {
                action = "update";
            }
            epo.setOriginalBean(objectForEditing);
        } else {
            objectForEditing = (DataPropertyStatement) epo.getNewBean();
        }

        FormObject foo = new FormObject();
        foo.setValues(new HashMap());
        HashMap OptionMap = new HashMap();
        List entityList = new LinkedList();
        if (objectForEditing.getIndividualURI() != null) {
            Individual individual = eDao.getIndividualByURI(objectForEditing.getIndividualURI());
            entityList.add(new Option(individual.getURI(),individual.getName(),true));
        } else {
            entityList.add(new Option ("-1", "Error: the individual must be specified", true));
        }
        OptionMap.put("IndividualURI",entityList);
        DataProperty dp = dpDao.getDataPropertyByURI(objectForEditing.getDatapropURI());
        if (dp == null) {
            foo.getValues().put("Dataprop", "Error: the data property must be specified");
        } else {
            foo.getValues().put("Dataprop", dp.getPublicName());
        }
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        FormUtils.populateFormFromBean(objectForEditing,action,foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/ents2data_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Individual Data Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","DataPropertyStatement");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName() + " could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
