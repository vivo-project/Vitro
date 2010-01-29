package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
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
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

public class OntologyRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(OntologyRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("OntologyRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        /*for testing*/
        Ontology testMask = new Ontology();
        epo.setBeanClass(Ontology.class);
        epo.setBeanMask(testMask);

        String action = "insert";

        OntologyDao oDao = getWebappDaoFactory().getOntologyDao();
        epo.setDataAccessObject(oDao);
        NamespaceDao nDao = getWebappDaoFactory().getNamespaceDao();

        Ontology ontologyForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    ontologyForEditing = oDao.getOntologyByURI(request.getParameter("uri"));
                    action = "update";
                } catch (NullPointerException e) {
                    log.error("No ontology record found for the namespace "+request.getParameter("uri"));
                }
            } else {
                ontologyForEditing = new Ontology();
            }
            epo.setOriginalBean(ontologyForEditing);
        } else {
            ontologyForEditing = (Ontology) epo.getNewBean();
            action = "update";
            log.error("using newBean");
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(ontologyForEditing.getId());
        epo.getSimpleMask().add(simpleMaskPair);

        //set up any listeners

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }
        //make a postinsert pageforwarder that will send us to a new ontology's edit screen
        epo.setPostInsertPageForwarder(new OntologyInsertPageForwarder(currPortalId));
        //make a postdelete pageforwarder that will send us to the list of ontologies
        epo.setPostDeletePageForwarder(new UrlForwarder("listOntologies?home="+currPortalId));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(oDao.getClass().getDeclaredMethod("getOntologyByURI",args));
        } catch (NoSuchMethodException e) {
            log.error("OntologyRetryController could not find the getOntologyByURI method in the DAO");
        }


        FormObject foo = new FormObject();

        HashMap optionMap = new HashMap();
        try {
            List namespaceIdList = FormUtils.makeOptionListFromBeans(nDao.getAllNamespaces(),"Id","Name",Integer.valueOf(ontologyForEditing.getNamespaceId()).toString(),null,false);
            namespaceIdList.add(0,new Option("-1","none", false));
            optionMap.put("NamespaceId", namespaceIdList);
        } catch (Exception e) {
            log.error(this.getClass().getName());
            e.printStackTrace();
        }
        foo.setOptionLists(optionMap);

        foo.setErrorMap(epo.getErrMsgMap());

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(ontologyForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/ontology_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Ontology Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Ontology");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("OntologyRetryContro" +
                    "ller could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class OntologyInsertPageForwarder implements PageForwarder {

        private int portalId = 1;

        public OntologyInsertPageForwarder(int currPortalId) {
            portalId = currPortalId;
        }

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newOntologyUrl = "ontologyEdit?home="+portalId+"&uri=";
            Ontology ont = (Ontology) epo.getNewBean();
            try {
                newOntologyUrl += URLEncoder.encode(ont.getURI(),"UTF-8");
                response.sendRedirect(newOntologyUrl);
            } catch (IOException ioe) {
                log.error("OntologyInsertPageForwarder could not send redirect.");
            }
        }
    }

}
