/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class RestrictionRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RestrictionRetryController.class.getName());
	private static final boolean DATA = true;
	private static final boolean OBJECT = false;
	
	public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

		VitroRequest request = new VitroRequest(req);

		try {
			
			EditProcessObject epo = createEpo(request);
			
			request.setAttribute("editAction","addRestriction");
			epo.setAttribute("VClassURI", request.getParameter("VClassURI"));
			
			String restrictionTypeStr = request.getParameter("restrictionType");
			epo.setAttribute("restrictionType",restrictionTypeStr);
			request.setAttribute("restrictionType",restrictionTypeStr);
				
			// default to object property restriction
			boolean propertyType = ("data".equals(request.getParameter("propertyType"))) ? DATA : OBJECT ;
			
			List<? extends ResourceBean> pList = (propertyType == OBJECT) 
				? request.getUnfilteredWebappDaoFactory().getObjectPropertyDao().getAllObjectProperties()
			    : request.getUnfilteredWebappDaoFactory().getDataPropertyDao().getAllDataProperties();
			List<Option> onPropertyList = new LinkedList<Option>(); 
			sortForPickList(pList, request);
			for (ResourceBean p: pList) {
				onPropertyList.add( new Option(p.getURI(),p.getPickListName()));
			}
					
			epo.setFormObject(new FormObject());
			epo.getFormObject().getOptionLists().put("onProperty", onPropertyList);
			
			if (restrictionTypeStr.equals("someValuesFrom")) {
				request.setAttribute("specificRestrictionForm","someValuesFromRestriction_retry.jsp");
				List<Option> optionList = (propertyType == OBJECT) 
					? getValueClassOptionList(request)
					: getValueDatatypeOptionList(request) ;
				epo.getFormObject().getOptionLists().put("ValueClass",optionList);
			} else if (restrictionTypeStr.equals("allValuesFrom")) {
				request.setAttribute("specificRestrictionForm","allValuesFromRestriction_retry.jsp");
				List<Option> optionList = (propertyType == OBJECT) 
					? getValueClassOptionList(request)
				    : getValueDatatypeOptionList(request) ;
				epo.getFormObject().getOptionLists().put("ValueClass",optionList);
			} else if (restrictionTypeStr.equals("hasValue")) {
				request.setAttribute("specificRestrictionForm", "hasValueRestriction_retry.jsp");
				if (propertyType == OBJECT) {
					request.setAttribute("propertyType", "object");	
				} else {
					request.setAttribute("propertyType", "data");
				}	
			} else if (restrictionTypeStr.equals("minCardinality") || restrictionTypeStr.equals("maxCardinality") || restrictionTypeStr.equals("cardinality")) {
				request.setAttribute("specificRestrictionForm", "cardinalityRestriction_retry.jsp");
			} 
			
	        request.setAttribute("formJsp","/templates/edit/specific/restriction_retry.jsp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
	        request.setAttribute("title","Add Restriction");
	        request.setAttribute("_action","insert");
	        setRequestAttributes(request,epo);
	
	        try {
				JSPPageHandler.renderBasicPage(request, response, "/templates/edit/formBasic.jsp");
	        } catch (Exception e) {
	            log.error(this.getClass().getName()+"PropertyRetryController could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }
        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<Option> getValueClassOptionList(VitroRequest request) {
		List<Option> valueClassOptionList = new LinkedList<Option>();
		VClassDao vcDao = request.getUnfilteredWebappDaoFactory().getVClassDao();
		List<VClass> vclasses = vcDao.getAllVclasses();
        boolean addOwlThing = true;
        for (VClass vclass : vclasses) {
            if (OWL.Thing.getURI().equals(vclass.getURI())) {
                addOwlThing = false;
                break;
            }
        }
        if(addOwlThing) {
            vclasses.add(new VClass(OWL.Thing.getURI()));
        }
        Collections.sort(vclasses);
		for (VClass vc: vclasses) {
			valueClassOptionList.add(new Option(vc.getURI(), vc.getPickListName()));
		}
		return valueClassOptionList;
	}
	
	private List<Option> getValueDatatypeOptionList(VitroRequest request) {
		List<Option> valueDatatypeOptionList = new LinkedList<Option>();
		DatatypeDao dtDao = request.getUnfilteredWebappDaoFactory().getDatatypeDao();
		for (Datatype dt: dtDao.getAllDatatypes()) {
			valueDatatypeOptionList.add(new Option(dt.getUri(), dt.getName()));
		}
		valueDatatypeOptionList.add(new Option(RDFS.Literal.getURI(), "rdfs:Literal"));
		return valueDatatypeOptionList;
	}
	
	
}
