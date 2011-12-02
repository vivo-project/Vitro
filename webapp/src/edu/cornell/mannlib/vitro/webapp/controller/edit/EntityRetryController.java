/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.DynamicField;
import edu.cornell.mannlib.vedit.beans.DynamicFieldRow;
import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.impl.RequiredFieldValidator;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.DoBackEndEditing;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.utils.RoleLevelOptionsSetup;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.listener.impl.IndividualDataPropertyStatementProcessor;

public class EntityRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(EntityRetryController.class.getName());

    public void doPost (HttpServletRequest request, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new DoBackEndEditing()))) {
        	return;
        }

        VitroRequest vreq = new VitroRequest(request);
        String siteAdminUrl = vreq.getContextPath() + Controllers.SITE_ADMIN;

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        
        epo.setBeanClass(Individual.class);
        epo.setImplementationClass(IndividualImpl.class);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        WebappDaoFactory wadf = (vreq.getAssertionsWebappDaoFactory()!=null) ? vreq.getAssertionsWebappDaoFactory() : vreq.getFullWebappDaoFactory();
        
        LoginStatusBean loginBean = LoginStatusBean.getBean(request);
        WebappDaoFactory myWebappDaoFactory = wadf.getUserAwareDaoFactory(loginBean.getUserURI());

        IndividualDao ewDao = myWebappDaoFactory.getIndividualDao();
        epo.setDataAccessObject(ewDao);
        VClassDao vcDao = myWebappDaoFactory.getVClassDao();
        VClassGroupDao cgDao = myWebappDaoFactory.getVClassGroupDao();
        DataPropertyDao dpDao = myWebappDaoFactory.getDataPropertyDao();

        Individual individualForEditing = null;
        if (epo.getUseRecycledBean()) {
            individualForEditing = (Individual)epo.getNewBean();
        } else {
            String uri = vreq.getParameter("uri");
            if (uri != null) {
                try {
                    individualForEditing = (Individual)ewDao.getIndividualByURI(uri);
                    action = "update";
                    epo.setAction("update");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                individualForEditing = new IndividualImpl();
                if (vreq.getParameter("VClassURI") != null) {
                    individualForEditing.setVClassURI(vreq.getParameter("VClassURI"));
                }
            }

            epo.setOriginalBean(individualForEditing);

            //make a simple mask for the entity's id
            Object[] simpleMaskPair = new Object[2];
            simpleMaskPair[0]="URI";
            simpleMaskPair[1]=individualForEditing.getURI();
            epo.getSimpleMask().add(simpleMaskPair);

        }

        //set any validators

        LinkedList lnList = new LinkedList();
        lnList.add(new RequiredFieldValidator());
        epo.getValidatorMap().put("Name",lnList);

        //make a postinsert pageforwarder that will send us to a new entity's fetch screen
        epo.setPostInsertPageForwarder(new EntityInsertPageForwarder());
        epo.setPostDeletePageForwarder(new UrlForwarder(siteAdminUrl));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(ewDao.getClass().getDeclaredMethod("getIndividualByURI",args));
        } catch (NoSuchMethodException e) {
            log.error("EntityRetryController could not find the entityByURI method in the dao");
        }

        epo.setIdFieldName("URI");
        epo.setIdFieldClass(String.class);

        HashMap hash = new HashMap();
        
        if (individualForEditing.getVClassURI() == null) {
	        // we need to do a special thing here to make an option list with option groups for the classgroups.
	        List classGroups = cgDao.getPublicGroupsWithVClasses(true,true,false); // order by displayRank, include uninstantiated classes, don't get the counts of individuals
	        Iterator classGroupIt = classGroups.iterator();
	        ListOrderedMap optGroupMap = new ListOrderedMap();
	        while (classGroupIt.hasNext()) {
	            VClassGroup group = (VClassGroup)classGroupIt.next();
	            List classes = group.getVitroClassList();
	            optGroupMap.put(group.getPublicName(),FormUtils.makeOptionListFromBeans(classes,"URI","Name",individualForEditing.getVClassURI(),null,false));
	        }
	        hash.put("VClassURI", optGroupMap);
        } else {
        	VClass vClass = null;
        	Option opt = null;
        	try {
        		vClass = vcDao.getVClassByURI(individualForEditing.getVClassURI());
        	} catch (Exception e) {}
    		if (vClass != null) {
    			opt = new Option(vClass.getURI(),vClass.getName(),true);
    		} else {
    			opt = new Option(individualForEditing.getVClassURI(),individualForEditing.getVClassURI(),true);
    		}
    		List<Option> optList  = new LinkedList<Option>();
    		optList.add(opt);
			hash.put("VClassURI", optList);
        }
        
        hash.put("HiddenFromDisplayBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getDisplayOptionsList(individualForEditing));    
        hash.put("ProhibitedFromUpdateBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getUpdateOptionsList(individualForEditing));

        FormObject foo = new FormObject();
        foo.setOptionLists(hash);

        ListOrderedMap dpMap = new ListOrderedMap();

        //make dynamic datatype property fields
        List<VClass> vclasses = individualForEditing.getVClasses(true);
        if (vclasses == null) {
        	vclasses = new ArrayList<VClass>();
        	if (individualForEditing.getVClassURI() != null) {
        		try {
	        		VClass cls = vreq.getFullWebappDaoFactory().getVClassDao().getVClassByURI(individualForEditing.getVClassURI());
	        		if (cls != null) {
	        			vclasses.add(cls);
	        		}
        		} catch (Exception e) {}
        	}
        }
        List<DataProperty> allApplicableDataprops = new ArrayList<DataProperty>();
        for (VClass cls : vclasses) {
        	List<DataProperty> dataprops = dpDao.getDataPropertiesForVClass(cls.getURI());
        	for (DataProperty dp : dataprops) {
        		boolean notDuplicate = true;
        		for (DataProperty existingDp : allApplicableDataprops) {
        			if (existingDp.getURI().equals(dp.getURI())) {
        				notDuplicate = false;
        				break;
        			}
        		}
        		if (notDuplicate) {
        			allApplicableDataprops.add(dp);
        		}
        	}
        }
        Collections.sort(allApplicableDataprops);
        
        if (allApplicableDataprops != null) {
            Iterator<DataProperty> datapropsIt = allApplicableDataprops.iterator();

            while (datapropsIt.hasNext()){
                DataProperty d = datapropsIt.next();
                if (!dpMap.containsKey(d.getURI())) {
                    dpMap.put(d.getURI(),d);
                }
            }

            if (individualForEditing.getDataPropertyList() != null) {
                Iterator<DataProperty> existingDps = individualForEditing.getDataPropertyList().iterator();
                while (existingDps.hasNext()) {
                    DataProperty existingDp = existingDps.next();
                    dpMap.put(existingDp.getURI(),existingDp);
                }
            }

            List<DynamicField> dynamicFields = new ArrayList();
            Iterator<String> dpHashIt = dpMap.orderedMapIterator();
            while (dpHashIt.hasNext()) {
                String uri = dpHashIt.next();
                DataProperty dp = (DataProperty) dpMap.get(uri);
                DynamicField dynamo = new DynamicField();
                dynamo.setName(dp.getPublicName());
                dynamo.setTable("DataPropertyStatement");
                dynamo.setVisible(dp.getDisplayLimit());
                dynamo.setDeleteable(true);
                DynamicFieldRow rowTemplate = new DynamicFieldRow();
                Map parameterMap = new HashMap();
                parameterMap.put("DatatypePropertyURI", dp.getURI());
                rowTemplate.setParameterMap(parameterMap);
                dynamo.setRowTemplate(rowTemplate);
                try {
                    Iterator<DataPropertyStatement> existingValues = dp.getDataPropertyStatements().iterator();
                    while (existingValues.hasNext()) {
                        DataPropertyStatement existingValue = existingValues.next();
                        DynamicFieldRow row = new DynamicFieldRow();
                        //TODO: UGH
                        //row.setId(existingValue.getId());
                        row.setParameterMap(parameterMap);
                        row.setValue(existingValue.getData());
                        if (dynamo.getRowList() == null)
                            dynamo.setRowList(new ArrayList());
                        dynamo.getRowList().add(row);
                    }
                } catch (NullPointerException npe) {
                    //whatever
                }
                if (dynamo.getRowList() == null)
                    dynamo.setRowList(new ArrayList());
                dynamo.getRowList().add(rowTemplate);
                dynamicFields.add(dynamo);
            }
            foo.setDynamicFields(dynamicFields);
        }

        foo.setErrorMap(epo.getErrMsgMap());

        epo.setFormObject(foo);

        // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat minutesOnlyDateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
        DateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        FormUtils.populateFormFromBean(individualForEditing,action,epo,foo,epo.getBadValueMap());

        List cList = new ArrayList();
        cList.add(new IndividualDataPropertyStatementProcessor());
        //cList.add(new SearchReindexer()); // handled for now by SearchReindexingListener on model
        epo.setChangeListenerList(cList);
        
        epo.getAdditionalDaoMap().put("DataPropertyStatement",myWebappDaoFactory.getDataPropertyStatementDao()); // EntityDatapropProcessor will look for this
        epo.getAdditionalDaoMap().put("DataProperty",myWebappDaoFactory.getDataPropertyDao()); // EntityDatapropProcessor will look for this

        ApplicationBean appBean = vreq.getAppBean();
        
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/entity_retry.jsp");
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("title","Individual Editing Form");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+appBean.getThemeDir()+"css/edit.css\"/>");
        request.setAttribute("scripts", "/js/edit/entityRetry.js");
        // NC Commenting this out for now. Going to pass on DWR for moniker and use jQuery instead
        // request.setAttribute("bodyAttr"," onLoad=\"monikerInit()\"");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","Individual");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("EntityRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class EntityInsertPageForwarder implements PageForwarder {

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newEntityUrl = "entityEdit?uri=";
            Individual ent = (Individual) epo.getNewBean();
            //log.error(ent.getName() + " : " + ent.getURI()+" ; "+ent.getNamespace()+" ; "+ent.getLocalName());
            if (ent != null && ent.getURI() != null) {
                try {
                    newEntityUrl += URLEncoder.encode(ent.getURI(),"UTF-8");
                    response.sendRedirect(newEntityUrl);
                } catch (Exception e) {
                    log.error("EntityInsertPageForwarder could not send redirect.");
                }
            } else {
                try {
                	String siteAdminUrl = request.getContextPath() + Controllers.SITE_ADMIN;
                    response.sendRedirect(siteAdminUrl);
                } catch (IOException e) {
                    log.error("EntityInsertPageForwarder could not redirect to about page.");
                }
            }
        }
    }

}
