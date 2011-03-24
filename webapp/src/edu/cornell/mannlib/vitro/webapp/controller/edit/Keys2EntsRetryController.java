package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashMap;
import java.util.Iterator;
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
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.edit.listener.impl.KeywordSearchReindexer;
import edu.cornell.mannlib.vitro.webapp.web.jsptags.InputElementFormattingTag;

public class Keys2EntsRetryController extends BaseEditController {

    private static final Log log = LogFactory.getLog(Keys2EntsRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
    	
    	VitroRequest request = new VitroRequest(req);
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("Keys2EntsRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        boolean isDashboardForm=false;
        String action = "insert";
        if ("dashboardDelete".equals(request.getParameter("action"))){
            action = "delete"; // will change -- ignore for now
            isDashboardForm=true;
        } else if ("dashboardInsert".equals(request.getParameter("action"))){
            isDashboardForm=true;
        }

        KeywordIndividualRelationDao k2eDao = getWebappDaoFactory().getKeys2EntsDao();
        epo.setDataAccessObject(k2eDao);
        KeywordDao kDao = getWebappDaoFactory().getKeywordDao();
        IndividualDao eDao = getWebappDaoFactory().getIndividualDao();

        KeywordIndividualRelation objectForEditing = new KeywordIndividualRelation();

        String uri = request.getParameter("uri");
        if (uri != null) {
            try {
                objectForEditing = k2eDao.getKeywordIndividualRelationByURI(uri);
                action = "update";
            } catch (NullPointerException e) {
                log.error("Could not find KeywordIndividualRelation "+uri);
            }
        }
        
        String individualURI = request.getParameter("individualURI");
        Individual ent = null;
        
        HashMap<String,List<Option>> optionMap = new HashMap<String,List<Option>>();
        List<Option> entList = new LinkedList<Option>();
        Option entOpt = null;
        if (individualURI != null) {
            objectForEditing.setEntURI(individualURI);
            ent = eDao.getIndividualByURI(objectForEditing.getEntURI());
            if (ent==null) {
                log.error("cannot find entity via URI '"+individualURI+"'");
                throw new Error("cannot find entity via URI '"+individualURI+"' in Keys2EntsRetryController");
            }
            entOpt = new Option(ent.getURI(),ent.getName(),true);
        } else {
            entOpt = new Option("-1", "Error: the entity must be specified", false);
        }
        entList.add(entOpt);
        optionMap.put("EntId",entList);
        
        /*
        List<KeywordIndividualRelation> k2iList = null;
        if (action.equals("remove")){
            k2iList = k2eDao.getKeywordIndividualRelationsByIndividualURI(individualURI);
        } */
        
        epo.setOriginalBean(objectForEditing);

        populateBeanFromParams(objectForEditing, request);

        //set up any listeners
        List changeListenerList = new LinkedList();
        changeListenerList.add(new KeywordSearchReindexer());
        epo.setChangeListenerList(changeListenerList);


        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(k2eDao.getClass().getDeclaredMethod("getKeywordIndividualRelationByURI",args));
        } catch (NoSuchMethodException e) {
            log.error("Keys2EntsRetryController could not find the getKeywordIndividualRelationByURI method");
        }

        FormObject foo = new FormObject();

        if (isDashboardForm && action.equals("delete")) {
            List<Option> existingKeywordRelations = new LinkedList();
            KeywordIndividualRelationDao kirDao = getWebappDaoFactory().getKeys2EntsDao();
            List kirs = kirDao.getKeywordIndividualRelationsByIndividualURI(ent.getURI());
            if (kirs != null) {
                Iterator kirIt = kirs.iterator();
                while (kirIt.hasNext()) {
                    KeywordIndividualRelation kir = (KeywordIndividualRelation) kirIt.next();
                    if (kir.getKeyId() > 0) {
                        Keyword k = kDao.getKeywordById(kir.getKeyId());
                        if (k != null) {
                            Option kOpt = new Option();
                            kOpt.setValue(kir.getURI());
                            kOpt.setBody(k.getTerm()+" ("+kir.getMode()+")");
                            existingKeywordRelations.add(kOpt);
                        }
                    }
                }
            }
            //foo.getOptionLists().put("existingKeywordRelations",existingKeywordRelations);
            optionMap.put("KeyId",existingKeywordRelations);
            //optionMap.put("KeyId",FormUtils.makeOptionListFromBeans((List)ent.getKeywordObjects(), "Id", "Term", Integer.toString(objectForEditing.getKeyId()), null));
            action = "update"; //reset for processing
        } else {
            optionMap.put("KeyId",FormUtils.makeOptionListFromBeans((List)kDao.getAllKeywords(), "Id", "Term", Integer.toString(objectForEditing.getKeyId()), null));
        }

        List modeOptionList = new LinkedList();
        String VISIBLE = "visible";
        Option visibleOption = new Option(VISIBLE);
        if (isDashboardForm){
            visibleOption.setSelected(true);
        } else {
            String HIDDEN = "hidden";
            Option hiddenOption = new Option(HIDDEN);
            if (objectForEditing.getMode().equalsIgnoreCase(HIDDEN)) {
                hiddenOption.setSelected(true);
            } else /*if (objectForEditing.getMode().equalsIgnoreCase(VISIBLE))*/ { //make this the default
                visibleOption.setSelected(true);
            }
            modeOptionList.add(hiddenOption);
        }
        modeOptionList.add(visibleOption);
        optionMap.put("Mode", modeOptionList);

        foo.setOptionLists(optionMap);
        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(objectForEditing,action,foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        if (isDashboardForm) {
            request.setAttribute("formJsp","/templates/edit/specific/keys2ents_dashboard_retry.jsp");
        } else {
            request.setAttribute("formJsp","/templates/edit/specific/keys2ents_retry.jsp");
        }
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Individual-Keyword Link Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Keys2Ents");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Keys2EntsRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
