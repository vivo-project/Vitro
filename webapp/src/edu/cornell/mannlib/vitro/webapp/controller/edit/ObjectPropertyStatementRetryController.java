/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstanceIface;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class ObjectPropertyStatementRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(ObjectPropertyStatementRetryController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new DoBackEndEditing()))) {
        	return;
        }

        VitroRequest vreq = new VitroRequest(request);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        epo.setBeanClass(PropertyInstanceIface.class);
        Class[] classarray = {PropertyInstanceIface.class};
        try {
        	epo.setInsertMethod(PropertyInstanceDao.class.getMethod("insertProp", classarray));
        	epo.setUpdateMethod(epo.getInsertMethod());
        } catch (NoSuchMethodException nsme) {
        	log.error("Unable to find "+PropertyInstanceDao.class.getName()+".insertProp("+PropertyInstanceIface.class.getName()+")");
        }
        
        try {
        	epo.setDeleteMethod(
        			PropertyInstanceDao.class.getMethod(
        					"deletePropertyInstance", classarray));
        } catch(NoSuchMethodException nsme) {
        	log.error("Unable to find "+PropertyInstanceDao.class.getName()+
        			".deletePropertyInstance("+
        					PropertyInstanceIface.class.getName()+")");
        }
                
        String action = "insert";

        PropertyInstanceDao piDao = vreq.getFullWebappDaoFactory().getPropertyInstanceDao();
        epo.setDataAccessObject(piDao);
        ObjectPropertyDao pDao = vreq.getFullWebappDaoFactory().getObjectPropertyDao();
        IndividualDao eDao = vreq.getFullWebappDaoFactory().getIndividualDao();
        VClassDao vcDao = vreq.getFullWebappDaoFactory().getVClassDao();

        PropertyInstance objectForEditing = null;
        if (!epo.getUseRecycledBean()){
            objectForEditing = new PropertyInstance();
            populateBeanFromParams(objectForEditing,vreq);
            if (vreq.getParameter(MULTIPLEXED_PARAMETER_NAME) != null) {
                action = "update";
            }
            epo.setOriginalBean(objectForEditing);
        } else {
            objectForEditing = (PropertyInstance) epo.getNewBean();
        }

        //set up any listeners
//        List changeListenerList = new LinkedList();
//        changeListenerList.add(new SearchReindexer());
//        epo.setChangeListenerList(changeListenerList);

        FormObject foo = new FormObject();
        ObjectProperty p = pDao.getObjectPropertyByURI(objectForEditing.getPropertyURI());
        if (p != null) {
                foo.getValues().put("Prop",p.getDomainPublic());
        } else {
            foo.getValues().put("Prop", "The property must be specified.");
        }
        HashMap optionMap = new HashMap();
        String domainEntityName = "";
        String rangeEntityName = "";
        Individual domainEntity = eDao.getIndividualByURI(objectForEditing.getSubjectEntURI());
        List domainOptionList = new LinkedList();
        Individual subjectInd = eDao.getIndividualByURI(objectForEditing.getSubjectEntURI());
        if (subjectInd != null) {
            Option subjOpt = new Option(subjectInd.getURI(), subjectInd.getName());
        }
        domainOptionList.add(new Option(domainEntity.getURI(), domainEntity.getName(), true));
        optionMap.put("SubjectEntURI", domainOptionList);

        // TODO : handle list of VClasses
        List<VClass> possibleClasses = vcDao.getVClassesForProperty(domainEntity.getVClassURI(),objectForEditing.getPropertyURI());
        Iterator<VClass> possIt = possibleClasses.iterator();
        HashSet<Individual> possIndSet = new HashSet<Individual>();
        while (possIt.hasNext()) {
            VClass possClass = possIt.next();
            List<Individual> possibleIndividuals = eDao.getIndividualsByVClass(possClass);
            possIndSet.addAll(possibleIndividuals);
        }
        List<Individual> indList = new LinkedList();
        indList.addAll(possIndSet);
        Collections.sort(indList, new IndComparator());
        List objectEntOptionList = new LinkedList();
        Iterator<Individual> indIt = indList.iterator();
        while (indIt.hasNext()) {
            Individual objInd = indIt.next();
            Option objIndOpt = new Option(objInd.getURI(), objInd.getName());
            if (objectForEditing.getObjectEntURI() != null && objectForEditing.getObjectEntURI().equals(objInd.getURI())) {
                objIndOpt.setSelected(true);
            }
            objectEntOptionList.add(objIndOpt);
        }
        if (objectEntOptionList.size()==0) {
            objectEntOptionList.add(new Option("", "There are no individuals yet defined that could fill this role."));
        }
        optionMap.put("ObjectEntURI", objectEntOptionList);

        foo.setOptionLists(optionMap);
        epo.setFormObject(foo);

        FormUtils.populateFormFromBean(objectForEditing,action,foo);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/ents2ents_retry_domainSide.jsp");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Object Property Instance Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","ObjectPropertyStatement");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("ObjectPropertyStatementRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    private class IndComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Collator collator = Collator.getInstance();
            return collator.compare(((Individual)o1).getName() , ((Individual)o2).getName());
        }
    }


}
