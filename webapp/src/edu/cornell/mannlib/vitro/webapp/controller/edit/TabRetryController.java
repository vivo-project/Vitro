/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
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
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseTabEditorPages;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;

@RequiresAuthorizationFor(UseTabEditorPages.class)
public class TabRetryController extends BaseEditController {

    static final int[] tabtypeIds = {0,18,20,22,24,26,28};
    static final String[] tabtypeNames = {"unspecified", "subcollection category",
                        "subcollection", "collection", "secondary tab",
                        "primary tab content", "primary tab"};
    private static final Log log = LogFactory.getLog(TabRetryController.class.getName());

    @Override
	public void doPost (HttpServletRequest req, HttpServletResponse response) {
    	VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        epo.setBeanClass(Tab.class);
        epo.setIdFieldName("TabId");

        String action = "insert";

        TabDao tDao = request.getFullWebappDaoFactory().getTabDao();
        epo.setDataAccessObject(tDao);

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }

        Tab tabForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("id") != null) {
                int id = Integer.parseInt(request.getParameter("id"));
                if (id > 0) {
                    try {
                        tabForEditing = tDao.getTab(id);
                        action = "update";
                    } catch (NullPointerException e) {
                        log.error("Need to implement 'record not found' error message.");
                    }
                }
            } else {
                tabForEditing = new Tab();
                tabForEditing.setPortalId(currPortalId);
            }
            epo.setOriginalBean(tabForEditing);
        } else {
            tabForEditing = (Tab) epo.getNewBean();
            action = "update";
            log.error("using newBean");
        }



        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(tabForEditing.getPortalId());
        epo.getSimpleMask().add(simpleMaskPair);


        //set any validators

        //set up any listeners


        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new TabInsertPageForwarder(currPortalId));
        //make a postdelete pageforwarder that will send us to the list of properties
        epo.setPostDeletePageForwarder(new UrlForwarder("listTabs?home="+currPortalId));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(tDao.getClass().getDeclaredMethod("getTab",args));
        } catch (NoSuchMethodException e) {
            log.error("TabRetryController could not find the getPortalById method in the facade");
        }


        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());

        HashMap optionMap = new HashMap();
        // TODO: userId
        List tabtypeList = new LinkedList();
        for (int i=0; i<tabtypeIds.length; i++) {
            Option tabtypeOpt = new Option(Integer.toString(tabtypeIds[i]), tabtypeNames[i], tabForEditing.getTabtypeId()==tabtypeIds[i]);
            tabtypeList.add(tabtypeOpt);
        }
        optionMap.put("TabtypeId", tabtypeList);
        List entityLinkMethodList = new LinkedList();
        entityLinkMethodList.add(new Option("mixed", "mixed manual and auto", tabForEditing.getEntityLinkMethod().equals("mixed")));
        entityLinkMethodList.add(new Option("manual", "manual", tabForEditing.getEntityLinkMethod().equals("manual")));
        entityLinkMethodList.add(new Option("auto", "by tab-type relationships", tabForEditing.getEntityLinkMethod().equals("auto")));
        optionMap.put("EntityLinkMethod",entityLinkMethodList);
        // TODO? statusId
        // portalId


        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(tabForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/tab_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Tab Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Tab");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("TabRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class TabInsertPageForwarder implements PageForwarder {

        private int portalId = 1;

        public TabInsertPageForwarder(int currPortalId) {
            portalId = currPortalId;
        }

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newTabUrl = "tabEdit?home="+portalId+"&id=";
            Tab tab = (Tab) epo.getNewBean();
            newTabUrl += tab.getTabId();
            try {
                response.sendRedirect(newTabUrl);
            } catch (IOException ioe) {
                log.error("TabInsertPageForwarder could not send redirect.");
            }
        }
    }
}
