/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
import edu.cornell.mannlib.vedit.validator.Validator;
import edu.cornell.mannlib.vedit.validator.impl.IntValidator;
import edu.cornell.mannlib.vedit.validator.impl.XMLNameValidator;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionListener;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.utils.RoleLevelOptionsSetup;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class PropertyRetryController extends BaseEditController {
    
    private static final Log log = LogFactory.getLog(PropertyRetryController.class.getName());
    
    @Override
    public void doPost (HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
            return;
        }

        VitroRequest request = new VitroRequest(req);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);

        /*for testing*/
        ObjectProperty testMask = new ObjectProperty();
        epo.setBeanClass(ObjectProperty.class);
        epo.setBeanMask(testMask);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        ObjectPropertyDao propDao = request.getFullWebappDaoFactory().getObjectPropertyDao();
        epo.setDataAccessObject(propDao);
        OntologyDao ontDao = request.getFullWebappDaoFactory().getOntologyDao();
        VClassDao vclassDao = request.getFullWebappDaoFactory().getVClassDao();
        DataPropertyDao dpDao = request.getFullWebappDaoFactory().getDataPropertyDao();

        ObjectProperty propertyForEditing = null;
        if (!epo.getUseRecycledBean()){
            String uri = request.getParameter("uri");
            if (uri != null) {
                try {
                    propertyForEditing = propDao.getObjectPropertyByURI(uri);
                    action = "update";
                    epo.setAction("update");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                propertyForEditing = new ObjectProperty();
                if (request.getParameter("parentId") != null) {
                    propertyForEditing.setParentURI(request.getParameter("parentId"));
                }
                if (request.getParameter("domainClassUri") != null) {
                    propertyForEditing.setDomainVClassURI(request.getParameter("domainClassUri"));
                }
            }
            epo.setOriginalBean(propertyForEditing);
        } else {
            propertyForEditing = (ObjectProperty) epo.getNewBean();
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="Id";
        simpleMaskPair[1]=propertyForEditing.getURI();
        epo.getSimpleMask().add(simpleMaskPair);


        //set any validators
        List localNameValidatorList = new ArrayList();
        localNameValidatorList.add(new XMLNameValidator());
        List localNameInverseValidatorList = new ArrayList();
        localNameInverseValidatorList.add(new XMLNameValidator(true));
        epo.getValidatorMap().put("LocalName", localNameValidatorList);
        epo.getValidatorMap().put("LocalNameInverse", localNameInverseValidatorList);
        List<Validator> displayRankValidatorList = new ArrayList<Validator>();
        displayRankValidatorList.add(new IntValidator());
        epo.getValidatorMap().put("DisplayRank", displayRankValidatorList);

        //set up any listeners
        List changeListenerList = new ArrayList();
        changeListenerList.add(new PropertyRestrictionListener(getServletContext()));
        epo.setChangeListenerList(changeListenerList);

        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new PropertyInsertPageForwarder());
        //make a postdelete pageforwarder that will send us to the list of properties
        epo.setPostDeletePageForwarder(new UrlForwarder("listPropertyWebapps"));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(propDao.getClass().getDeclaredMethod("getObjectPropertyByURI",args));
        } catch (NoSuchMethodException e) {
            log.error("PropertyRetryController could not find the getPropertyByURI method in the PropertyWebappDao");
        }

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());     

        HashMap<String, List<Option>> optionMap = new HashMap<String, List<Option>>();
        try {
            populateOptionMap(optionMap, propertyForEditing, request, ontDao, propDao);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e);
        }

        optionMap.put("HiddenFromDisplayBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getDisplayOptionsList(propertyForEditing));    
        optionMap.put("ProhibitedFromUpdateBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getUpdateOptionsList(propertyForEditing));

        List groupOptList = FormUtils.makeOptionListFromBeans(request.getFullWebappDaoFactory().getPropertyGroupDao().getPublicGroups(true),"URI","Name", ((propertyForEditing.getGroupURI()==null) ? "" : propertyForEditing.getGroupURI()), null, (propertyForEditing.getGroupURI()!=null));
        HashMap<String,Option> hashMap = new HashMap<String,Option>();
        groupOptList = getSortedList(hashMap,groupOptList);
        groupOptList.add(0,new Option("","none"));
        optionMap.put("GroupURI", groupOptList);
        
        foo.setOptionLists(optionMap);
        
        request.setAttribute("transitive",propertyForEditing.getTransitive());
        request.setAttribute("symmetric",propertyForEditing.getSymmetric());
        request.setAttribute("functional",propertyForEditing.getFunctional());
        request.setAttribute("inverseFunctional",propertyForEditing.getInverseFunctional());
        request.setAttribute("selectFromExisting",propertyForEditing.getSelectFromExisting());
        request.setAttribute("offerCreateNewOption", propertyForEditing.getOfferCreateNewOption());
        request.setAttribute("stubObjectRelation", propertyForEditing.getStubObjectRelation());
        request.setAttribute("collateBySubclass", propertyForEditing.getCollateBySubclass());
        
        //checkboxes are pretty annoying : we don't know if someone *unchecked* a box, so we have to default to false on updates.
        if (propertyForEditing.getURI() != null) {
             propertyForEditing.setTransitive(false);
            propertyForEditing.setSymmetric(false);
            propertyForEditing.setFunctional(false);
            propertyForEditing.setInverseFunctional(false);
            propertyForEditing.setSelectFromExisting(false);
            propertyForEditing.setOfferCreateNewOption(false);
            propertyForEditing.setStubObjectRelation(false);
            propertyForEditing.setCollateBySubclass(false);
        }

        epo.setFormObject(foo);

        FormUtils.populateFormFromBean(propertyForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("colspan","4");
        request.setAttribute("formJsp","/templates/edit/specific/property_retry.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Property Editing Form");
        request.setAttribute("_action",action);
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("PropertyRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    private void populateOptionMap(HashMap<String, List<Option>> optionMap, 
                                   ObjectProperty propertyForEditing, 
                                   VitroRequest request, 
                                   OntologyDao ontDao, 
                                   ObjectPropertyDao propDao) {
    	
        List<Option> namespaceIdList = FormUtils.makeOptionListFromBeans(
                ontDao.getAllOntologies(), "URI", "Name", 
                        ((propertyForEditing.getNamespace() == null)
                                ? "" 
                                : propertyForEditing.getNamespace()), 
                        null, (propertyForEditing.getNamespace() != null));
        namespaceIdList.add(0, new Option(
                request.getFullWebappDaoFactory().getDefaultNamespace(), "default"));
        optionMap.put("Namespace", namespaceIdList);
        
        List<Option> namespaceIdInverseList = FormUtils.makeOptionListFromBeans(
                ontDao.getAllOntologies(), "URI", "Name", 
                        ((propertyForEditing.getNamespaceInverse() == null) 
                                ? "" 
                                : propertyForEditing.getNamespaceInverse()), 
                        null, (propertyForEditing.getNamespaceInverse() != null));
        namespaceIdInverseList.add(0, new Option(
                request.getFullWebappDaoFactory().getDefaultNamespace(),"default"));
        optionMap.put("NamespaceInverse", namespaceIdInverseList);
        
        List<ObjectProperty> objPropList = propDao.getAllObjectProperties();
        Collections.sort(objPropList);
        List<Option> parentIdList = FormUtils.makeOptionListFromBeans(
                objPropList,"URI","PickListName",propertyForEditing.getParentURI(),null);
        HashMap<String,Option> hashMap = new HashMap<String,Option>();
        parentIdList = getSortedList(hashMap,parentIdList);
        parentIdList.add(0,new Option("-1","none (root property)", false));
        optionMap.put("ParentURI", parentIdList);
        
//        List<DataProperty> dpList = dpDao.getAllDataProperties();
//        Collections.sort(dpList);
//        List<Option> objectIndividualSortPropertyList = 
//                FormUtils.makeOptionListFromBeans(
//                        dpList, "URI", "Name", 
//                        propertyForEditing.getObjectIndividualSortPropertyURI(),
//                        null);
//        objectIndividualSortPropertyList.add(0, new Option(
//                "","- select data property -"));
//        optionMap.put("ObjectIndividualSortPropertyURI", 
//                objectIndividualSortPropertyList);
        
        List<Option> domainOptionList = FormUtils.makeVClassOptionList(
                request.getFullWebappDaoFactory(), 
                        propertyForEditing.getDomainVClassURI());
        if (propertyForEditing.getDomainVClass() != null 
                && propertyForEditing.getDomainVClass().isAnonymous()) {
            domainOptionList.add(0, new Option(
                    propertyForEditing.getDomainVClass().getURI(), 
                    propertyForEditing.getDomainVClass().getName(), 
                    true));
        }
        domainOptionList.add(0, new Option("","(none specified)"));
        optionMap.put("DomainVClassURI", domainOptionList);
        
        List<Option> rangeOptionList = FormUtils.makeVClassOptionList(
                request.getFullWebappDaoFactory(), 
                        propertyForEditing.getRangeVClassURI());
        if (propertyForEditing.getRangeVClass() != null 
                && propertyForEditing.getRangeVClass().isAnonymous()) {
            rangeOptionList.add(0, new Option(
                    propertyForEditing.getRangeVClass().getURI(), 
                    propertyForEditing.getRangeVClass().getName(), 
                    true));
        }
        rangeOptionList.add(0, new Option("","(none specified)"));
        optionMap.put("RangeVClassURI", rangeOptionList);
    }
    
    private List<VClass> makeVClassListForOptions(String VClassURI, List<VClass> allClassBeanList, VClassDao vclassDao) {
        List<VClass> currentClassList = new ArrayList<VClass>();
        VClass currentVClass = vclassDao.getVClassByURI(VClassURI);
        if (currentVClass != null && currentVClass.isAnonymous()) {
            currentClassList.addAll(allClassBeanList);
            currentClassList.add(0,currentVClass);
        } else {
            currentClassList = allClassBeanList; 
        }
        return currentClassList;
    }
    
    class PropertyInsertPageForwarder implements PageForwarder {

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newPropertyUrl = "propertyEdit?uri=";
            ObjectProperty p = (ObjectProperty) epo.getNewBean();
            try {
                newPropertyUrl += URLEncoder.encode(p.getURI(),"UTF-8");
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not use UTF-8 encoding to encode new URL");
            }
            try {
                response.sendRedirect(newPropertyUrl);
            } catch (IOException ioe) {
                log.error("PropertyInsertPageForwarder could not send redirect.");
            }
        }
    }
    
    public List<Option> getSortedList(HashMap<String,Option> hashMap, List<Option> optionList){
        
           class ListComparator implements Comparator<String>{
               @Override
               public int compare(String str1, String str2) {
                   // TODO Auto-generated method stub
                   Collator collator = Collator.getInstance();
                   return collator.compare(str1, str2);
               }
               
           }

          List<String> bodyVal = new ArrayList<String>();
          List<Option> options = new ArrayList<Option>();
          Iterator<Option> itr = optionList.iterator();
           while(itr.hasNext()){
               Option option = itr.next();
               hashMap.put(option.getBody(),option);
              bodyVal.add(option.getBody());
           }
           
                   
          Collections.sort(bodyVal, new ListComparator());
          ListIterator<String> itrStr = bodyVal.listIterator();
          while(itrStr.hasNext()){
              options.add(hashMap.get(itrStr.next()));
          }
          return options;
      }
}
