/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

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
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.edit.listener.impl.KeywordSearchReindexer;
import edu.cornell.mannlib.vitro.webapp.edit.validator.impl.KeywordStemmerAndValidator;

public class KeywordRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(KeywordRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("KeywordRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        /*for testing*/
        Keyword testMask = new Keyword();
        epo.setBeanClass(Keyword.class);
        epo.setBeanMask(testMask);

        KeywordDao kDao = request.getFullWebappDaoFactory().getKeywordDao();
        epo.setDataAccessObject(kDao);
        epo.getAdditionalDaoMap().put("KeywordIndividualRelationDao", request.getFullWebappDaoFactory().getKeys2EntsDao());

        String action="insert";
        
        Keyword keywordForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("id") != null) {
                int id = Integer.parseInt(request.getParameter("id"));
                if (id > 0) {
                    try {
                        keywordForEditing = kDao.getKeywordById(id);
                        action = "update";
                    } catch (NullPointerException e) {
                        log.error("Need to implement 'record not found' error message.");
                    }
                }
            } else {
                keywordForEditing = new Keyword();
                keywordForEditing.setType("keyword");
                keywordForEditing.setOrigin("unspecified"); // may be reset below
                keywordForEditing.setSource("unspecified"); // may be reset below
            }
            epo.setOriginalBean(keywordForEditing);
        } else {
            keywordForEditing = (Keyword) epo.getNewBean();
            keywordForEditing.setType("keyword");
            keywordForEditing.setOrigin("unspecified"); // may be reset below
            keywordForEditing.setSource("unspecified"); // may be reset below
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=Integer.valueOf(keywordForEditing.getId());
        epo.getSimpleMask().add(simpleMaskPair);

        //set up any validators
        LinkedList lnList = new LinkedList();
        lnList.add(new KeywordStemmerAndValidator(epo,kDao));
        epo.getValidatorMap().put("Term",lnList);

        //set up any listeners
        String entityURI = request.getParameter("individualURI");
        if (entityURI != null && request.getParameter("mode") != null) {
            List listenerList = new LinkedList();
            listenerList.add(new newKeywordToEntityLinker(entityURI, request.getParameter("mode")));
            listenerList.add(new KeywordSearchReindexer());
            epo.setChangeListenerList(listenerList);
        }

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = int.class;
            epo.setGetMethod(kDao.getClass().getDeclaredMethod("getKeywordById",args));
        } catch (NoSuchMethodException e) {
            log.error("KeywordRetryController could not find the getKeywordById method");
        }


        FormObject foo = new FormObject();
        
        boolean useShortForm=false;
        if ("dashboardCreate".equals(request.getParameter("action"))) {
            keywordForEditing.setOrigin("user-added");
            keywordForEditing.setSource("user-added");
            useShortForm=true;
        } else if ("editorCreate".equals(request.getParameter("action"))) {
            keywordForEditing.setOrigin("curator-added");
            keywordForEditing.setSource("curator-added");
            useShortForm=true;
        } else {
            HashMap OptionMap = new HashMap();
            List originsList = kDao.getAllOrigins();
            List originsOptList = new LinkedList();
            originsOptList.add(new Option("","[new origin]",false));
            Iterator originsIt = originsList.iterator();
            while (originsIt.hasNext()) {
                String optStr = (String)originsIt.next();
                boolean selected = (keywordForEditing.getOrigin()==null) ? false : keywordForEditing.getOrigin().equals(optStr);
                Option opt = new Option(optStr, optStr, selected);
                originsOptList.add(opt);
            }
            OptionMap.put("Origin", originsOptList);
            foo.setOptionLists(OptionMap);
        }
        
        foo.setErrorMap(epo.getErrMsgMap());

        epo.setFormObject(foo);

        String html = FormUtils.htmlFormFromBean(keywordForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        if (useShortForm) {
            request.setAttribute("formJsp","/templates/edit/specific/keyterms_dashboard_retry.jsp");
        } else {
            request.setAttribute("formJsp","/templates/edit/specific/keyterms_retry.jsp");
        }
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Keyword Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Keyword");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("KeywordRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public class newKeywordToEntityLinker implements ChangeListener {
    // should be a better way to do this
    // cascading deletes should be implemented differently
        String entityURI = null;
        String mode = "hidden";
        private newKeywordToEntityLinker(String entityURI, String mode) {
            this.entityURI = entityURI;
            this.mode = mode;
        }

        public void doInserted(Object newObj, EditProcessObject epo) {
            Keyword k = (Keyword) newObj;
            KeywordIndividualRelation k2e = new KeywordIndividualRelation();
            k2e.setKeyId(k.getId());
            k2e.setEntURI(entityURI);
            k2e.setMode(mode);
            ((KeywordIndividualRelationDao)epo.getAdditionalDaoMap().get("KeywordIndividualRelationDao")).insertNewKeywordIndividualRelation(k2e);
        }
        public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
            // nothing required
        }
        public void doDeleted(Object oldObj, EditProcessObject epo) {
            // nothing required
        }


    }

}