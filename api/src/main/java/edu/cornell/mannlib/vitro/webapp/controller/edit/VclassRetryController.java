/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.impl.XMLNameValidator;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.utils.RoleLevelOptionsSetup;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

@WebServlet(name = "VclassRetryController", urlPatterns = {"/vclass_retry"} )
public class VclassRetryController extends BaseEditController {

	private static final Log log = LogFactory.getLog(VclassRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        /*for testing*/
        VClass testMask = new VClass();
        epo.setBeanClass(VClass.class);
        epo.setImplementationClass(VClass.class);
        epo.setBeanMask(testMask);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        WebappDaoFactory wadf = ModelAccess.on(getServletContext()).getWebappDaoFactory();

        VClassDao vcwDao = wadf.getVClassDao();
        epo.setDataAccessObject(vcwDao);
        VClassGroupDao cgDao = wadf.getVClassGroupDao();
        OntologyDao oDao = wadf.getOntologyDao();

        VClass vclassForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    vclassForEditing = (VClass)vcwDao.getVClassByURI(request.getParameter("uri"));
                    action = "update";
                    epo.setAction("update");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                vclassForEditing = new VClass();
                if (request.getParameter("GroupId") != null) {
                    vclassForEditing.setGroupURI(request.getParameter("GroupURI"));
                }
            }
            epo.setOriginalBean(vclassForEditing);
        } else {
            vclassForEditing = (VClass) epo.getNewBean();
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="URI";
        simpleMaskPair[1]=vclassForEditing.getURI();
        epo.getSimpleMask().add(simpleMaskPair);

        //validators
        List localNameValidatorList = new ArrayList();
        localNameValidatorList.add(new XMLNameValidator());
        epo.getValidatorMap().put("LocalName",localNameValidatorList);

        //set up any listeners
        List changeListenerList = new LinkedList();
        if (request.getParameter("superclassUri") != null) {
            changeListenerList.add(new SubclassListener(request.getParameter("superclassUri"), request.getUnfilteredWebappDaoFactory()));
        }
        epo.setChangeListenerList(changeListenerList);

        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new VclassInsertPageForwarder());
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("showClassHierarchy"));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(VClassDao.class.getDeclaredMethod("getVClassByURI",args));
        } catch (NoSuchMethodException e) {
            log.error(this.getClass().getName()+" could not find the getVClassByURI method");
        }

        HashMap<String, List<Option>> optionMap = new HashMap<String,List<Option>>();
        try {
            VClassGroupDao vcgDao = request.getUnfilteredWebappDaoFactory().getVClassGroupDao();
            List classGroupOptionList = FormUtils.makeOptionListFromBeans(vcgDao.getPublicGroupsWithVClasses(),"URI","PublicName",vclassForEditing.getGroupURI(),null,(vclassForEditing.getGroupURI()!=null && !(vclassForEditing.getGroupURI().equals(""))));
            classGroupOptionList.add(0,new Option("", "none", ("update".equals(action) && (vclassForEditing.getGroupURI()==null || vclassForEditing.getGroupURI().equals("")))));
            optionMap.put("GroupURI", classGroupOptionList);

        } catch (Exception e) {
            log.error("unable to create GroupId option list");
        }
        try {
            List namespaceIdList = (action.equals("insert"))
                    ? FormUtils.makeOptionListFromBeans(oDao.getAllOntologies(),"URI","Name", ((vclassForEditing.getNamespace()==null) ? "" : vclassForEditing.getNamespace()), null, false)
                    : FormUtils.makeOptionListFromBeans(oDao.getAllOntologies(),"URI","Name", ((vclassForEditing.getNamespace()==null) ? "" : vclassForEditing.getNamespace()), null, true);
	        namespaceIdList.add(0, new Option(request.getUnfilteredWebappDaoFactory().getDefaultNamespace(),"default"));
            optionMap.put("Namespace", namespaceIdList);
        } catch (Exception e) {
            log.error(this.getClass().getName() + "unable to create Namespace option list");
        }

        optionMap.put("HiddenFromDisplayBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getDisplayOptionsList(vclassForEditing));
        optionMap.put("ProhibitedFromUpdateBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getUpdateOptionsList(vclassForEditing));
        optionMap.put("HiddenFromPublishBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getPublishOptionsList(vclassForEditing));

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());
        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);

        request.setAttribute("formValue",foo.getValues());

        FormUtils.populateFormFromBean(vclassForEditing,action,foo,epo.getBadValueMap());

        request.setAttribute("formJsp","/templates/edit/specific/vclass_retry.jsp");
        request.setAttribute("colspan","4");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Class Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","VClass");
        setRequestAttributes(request,epo);

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/formBasic.jsp");
        } catch (Exception e) {
            log.error("VclassRetryController could not forward to view.", e);
            throw new RuntimeException(e);
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    /** This listener allows us to link a new class to a parent upon creation */
    class SubclassListener implements ChangeListener {
        String superclassURI = null;
        WebappDaoFactory daoFactory = null;
        public SubclassListener(String superclassURI, WebappDaoFactory cdf) {
            this.superclassURI = superclassURI;
            this.daoFactory = cdf;
        }
        public void doInserted(Object newObj, EditProcessObject epo) {
            Classes2Classes c2c = new Classes2Classes();
            c2c.setSubclassURI(((VClass)newObj).getURI());
            c2c.setSuperclassURI(superclassURI);
            daoFactory.getVClassDao().insertNewClasses2Classes(c2c);
        }
        public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
            // nothing to do
        }
        public void doDeleted(Object oldObj, EditProcessObject epo) {
            // nothing to do
        }
    }

    class VclassInsertPageForwarder implements PageForwarder {

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newVclassUrl = "vclassEdit?uri=";
            VClass vcl = (VClass) epo.getNewBean();
            try {
                newVclassUrl += URLEncoder.encode(vcl.getURI(),"UTF-8");
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not use UTF-8 encoding to encode new URL");
            }
            try {
                response.sendRedirect(newVclassUrl);
            } catch (IOException ioe) {
                log.error("VclassInsertPageForwarder could not send redirect.");
            }
        }
    }

}
