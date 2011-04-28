/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.RDFS;
import com.ibm.icu.text.Collator;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOntology;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class RestrictionRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RestrictionRetryController.class.getName());
	private static final boolean DATA = true;
	private static final boolean OBJECT = false;
	
	public void doGet(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new EditOntology()))) {
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
			
			List<? extends Property> pList = (propertyType == OBJECT) 
				? request.getFullWebappDaoFactory().getObjectPropertyDao().getAllObjectProperties()
			    : request.getFullWebappDaoFactory().getDataPropertyDao().getAllDataProperties();
			List<Option> onPropertyList = new LinkedList<Option>(); 
			Collections.sort(pList, new PropSorter());
			for (Property p: pList) {
				onPropertyList.add( new Option(p.getURI(),p.getLocalNameWithPrefix()) );
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
			
	        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
	        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
	        request.setAttribute("formJsp","/templates/edit/specific/restriction_retry.jsp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
	        request.setAttribute("title","Add Restriction");
	        request.setAttribute("_action","insert");
	        setRequestAttributes(request,epo);
	
	        try {
	            rd.forward(request, response);
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
		VClassDao vcDao = request.getFullWebappDaoFactory().getVClassDao();
		for (VClass vc: vcDao.getAllVclasses()) {
			valueClassOptionList.add(new Option(vc.getURI(), vc.getLocalNameWithPrefix()));
		}
		return valueClassOptionList;
	}
	
	private List<Option> getValueDatatypeOptionList(VitroRequest request) {
		List<Option> valueDatatypeOptionList = new LinkedList<Option>();
		DatatypeDao dtDao = request.getFullWebappDaoFactory().getDatatypeDao();
		for (Datatype dt: dtDao.getAllDatatypes()) {
			valueDatatypeOptionList.add(new Option(dt.getUri(), dt.getName()));
		}
		valueDatatypeOptionList.add(new Option(RDFS.Literal.getURI(), "rdfs:Literal"));
		return valueDatatypeOptionList;
	}
	
	private class PropSorter implements Comparator<Property> {
		
		public int compare(Property p1, Property p2) {
			if (p1.getLocalNameWithPrefix() == null) return 1;
			if (p2.getLocalNameWithPrefix() == null) return -1;
			return Collator.getInstance().compare(p1.getLocalNameWithPrefix(), p2.getLocalNameWithPrefix());
		}
		
	}
	
}
