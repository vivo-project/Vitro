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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManagePortals;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.TabIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabIndividualRelationDao;

public class Tabs2EntsRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(Tabs2EntsRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new ManagePortals()))) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);
    	
        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }

        String action = "insert";

        TabIndividualRelationDao t2eDao = request.getFullWebappDaoFactory().getTabs2EntsDao();
        epo.setDataAccessObject(t2eDao);
        TabDao tDao = request.getFullWebappDaoFactory().getTabDao();
        IndividualDao eDao = request.getFullWebappDaoFactory().getIndividualDao();

        TabIndividualRelation objectForEditing = null;

        if (request.getParameter("uri") != null) {
                try {
                    objectForEditing = t2eDao.getTabIndividualRelationByURI(request.getParameter("uri"));
                    action = "update";
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
        } else {
            objectForEditing = new TabIndividualRelation();
            String entityIdStr = request.getParameter("entityUri");
            if (entityIdStr != null) {
                try {
                    objectForEditing.setEntURI(entityIdStr);
                } catch (NumberFormatException e) {
                    // not really an integer.
                }
            }
            String tabIdStr = request.getParameter("TabId");
            if (tabIdStr != null) {
                try {
                    objectForEditing.setTabId(Integer.decode(tabIdStr));
                } catch (NumberFormatException e) {
                    // not really an integer.
                }
            }
        }

        epo.setOriginalBean(objectForEditing);

        // populateBeanFromParams(objectForEditing, request);

        //set up any listeners
        //List changeListenerList = new LinkedList();
        //epo.setChangeListenerList(changeListenerList);

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(t2eDao.getClass().getDeclaredMethod("getTabs2TypesById",args));
        } catch (NoSuchMethodException e) {
            log.error("Tabs2EntsRetryController could not find the getTabs2TypesById method in the facade");
        }

        HashMap optionMap = new HashMap();
        List<Tab> tabList = tDao.getAllManuallyLinkableTabs(currPortalId);
        Collections.sort(tabList);
        optionMap.put("TabId", FormUtils.makeOptionListFromBeans(tabList, "TabId", "Title", Integer.toString(objectForEditing.getTabId()), null));
        if (objectForEditing.getEntURI() != null) {
            Individual ent = eDao.getIndividualByURI(objectForEditing.getEntURI());
            List l = new LinkedList();
            l.add(new Option(ent.getURI(),ent.getName(),true));
            optionMap.put("EntId", l);
        } else try {
            String vclassUri = request.getParameter("VClassUri");
            VClass scratch = new VClass();
            scratch.setURI(vclassUri);
            List<Individual> individualsInClass = eDao.getIndividualsByVClass(scratch);
            Collections.sort(individualsInClass);
            optionMap.put("EntId", FormUtils.makeOptionListFromBeans(individualsInClass, "URI", "Name", objectForEditing.getEntURI(), null, false));
        } catch (Exception e){
            try {
                String entityUri = request.getParameter("entityUri");
                Individual entity = eDao.getIndividualByURI(entityUri);
                List l = new LinkedList();
                l.add(new Option(entity.getURI(),entity.getName(),true));
                optionMap.put("EntId", l);
            } catch (Exception f) {
                List l = new LinkedList();
                l.add(new Option("-1","Error: VClass must be specified", true));
                optionMap.put("EntId", l);
            }
        }
        FormObject foo = new FormObject();
        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(objectForEditing,action,foo);

        Portal portal = (new VitroRequest(request)).getPortal();
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/tabs2ents_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Tab-Individual Association Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Tabs2Ents");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Tabs2EntsRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
