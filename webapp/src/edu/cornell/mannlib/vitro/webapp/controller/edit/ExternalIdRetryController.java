/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;
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
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class ExternalIdRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ExternalIdRetryController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new DoBackEndEditing()))) {
        	return;
        }

        VitroRequest vreq = new VitroRequest(request);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        String action = "insert";

        DataPropertyDao dpDao = vreq.getFullWebappDaoFactory().getDataPropertyDao();
        DataPropertyStatementDao edDao = vreq.getFullWebappDaoFactory().getDataPropertyStatementDao();
        epo.setDataAccessObject(edDao);
        epo.setBeanClass(DataPropertyStatement.class);

        IndividualDao eDao = vreq.getFullWebappDaoFactory().getIndividualDao();

        DataPropertyStatement eidForEditing = null;
        if (!epo.getUseRecycledBean()){
            eidForEditing = new DataPropertyStatementImpl();
            populateBeanFromParams(eidForEditing,vreq);
            if (vreq.getParameter(MULTIPLEXED_PARAMETER_NAME) != null) {
                action = "update";
            }
            epo.setOriginalBean(eidForEditing);
        } else {
            eidForEditing = (DataPropertyStatement) epo.getNewBean();
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        //simpleMaskPair[1]=Integer.valueOf(eidForEditing.getId());
        epo.getSimpleMask().add(simpleMaskPair);

        //set up any listeners

        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();
        List entityList = new LinkedList();
        if (eidForEditing.getIndividualURI() != null) {
            Individual individual = eDao.getIndividualByURI(eidForEditing.getIndividualURI());
            entityList.add(new Option(individual.getURI(),individual.getName(),true));
        } else {
            entityList.add(new Option ("-1", "Error: the entity must be specified", true));
        }
        OptionMap.put("IndividualURI",entityList);
        // TOOD change following DAO call to getAllExternalIdDataProperties once implemented
        List allExternalIdDataProperties = dpDao.getAllExternalIdDataProperties();
        Collections.sort(allExternalIdDataProperties);
        OptionMap.put("DatapropURI", FormUtils.makeOptionListFromBeans(allExternalIdDataProperties, "URI", "PublicName", eidForEditing.getDatapropURI(),""));
        foo.setOptionLists(OptionMap);
        foo.setErrorMap(epo.getErrMsgMap());

        epo.setFormObject(foo);

        FormUtils.populateFormFromBean(eidForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/externalIds_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","External Id Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","External Id");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("ExternalIdRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
