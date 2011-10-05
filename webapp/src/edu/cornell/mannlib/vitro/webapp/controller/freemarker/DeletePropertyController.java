/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.MenuManagementDataUtils;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageMenus;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
 
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
/*
 * Custom deletion controller to which deletion requests from default property form are sent. May be replaced 
 * later with additional features in process rdf form controller or alternative location.
 */
public class DeletePropertyController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(DeletePropertyController.class);
  
    //since forwarding from edit Request dispatch for now
   //TODO: Check what required actions would make sense here
    //public final static Actions REQUIRED_ACTIONS = new Actions(new ManageMenus());
    
   
    /*
     *  @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
    }*/

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	//if error conditions then return here
    	String errorMessage = handleErrors(vreq);
    	if(errorMessage != null) {
    		return doErrorMessage(errorMessage);
    	}
    	
    	//handle based on whether object property or data property
    	if(EditConfigurationUtils.isObjectProperty(
    			EditConfigurationUtils.getPredicateUri(vreq), vreq))
    	{
    		processObjectProperty(vreq);
    	} else {
    		processDataProperty(vreq);
    	}
    	//Get subject, predicate uri ,
    	//Redirect 
    	//return new TemplateResponseValues(editConfig.getTemplate(), map);
    	String redirectUrl = getRedirectUrl(vreq);
    	return new RedirectResponseValues(redirectUrl, HttpServletResponse.SC_SEE_OTHER);
    }
    
    
    private String getRedirectUrl(VitroRequest vreq) {
		// TODO Auto-generated method stub
    	String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	int hashIndex = predicateUri.lastIndexOf("#");
    	String localName = predicateUri.substring(hashIndex + 1);
    	String redirectUrl = "/entity?uri=" + subjectUri;
		return null;
	}


	private String handleErrors(VitroRequest vreq) {
		//If subject or property empty then need to return error
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	if(wdf == null) {
    		   return "could not get a WebappDaoFactory";
    	}
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	if(subject == null) {
    		return "could not find subject " + EditConfigurationUtils.getSubjectUri(vreq);
    	}
    	
    	//if object property, check object property otherwise check data property
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	if(EditConfigurationUtils.isObjectProperty(predicateUri, vreq)) {
    		ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    		if(prop == null) {
    			return "In delete property controller, could not find object property " + predicateUri;
    		}
    	} else {
    		DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
    		if(prop == null) {
    			return "In delete property controller, could not find data property " + predicateUri;
    		}
    	}
    	
    	return null;
		
	}

	private TemplateResponseValues doErrorMessage(String errorMessage) {
		HashMap<String,Object> map = new HashMap<String,Object>();
	   	map.put("errorMessage", errorMessage);
	   	return new TemplateResponseValues("error-message.ftl", map); 
	}
		


	//process data property
    private void processDataProperty(VitroRequest vreq) {
    	deleteDataPropertyStatement(vreq);		
    }
    
    private void deleteDataPropertyStatement(VitroRequest vreq) {
    	Individual subject = EditConfigurationUtils.getSubjectIndividual(vreq);
    	//TODO: if null, need to throw or show error
    	int dataHash = getDataHash(vreq);
		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
		DataPropertyStatement dps = EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		if(dps != null) {
			wdf.getDataPropertyStatementDao().deleteDataPropertyStatement(dps);
		}
	}


	private int getDataHash(VitroRequest vreq) {
		int dataHash = 0;
		String datapropKey = EditConfigurationUtils.getDataPropKey(vreq);
		if (datapropKey!=null && datapropKey.trim().length()>0) {
	        try {
	            dataHash = Integer.parseInt(datapropKey);
	        } catch (NumberFormatException ex) {
	            log.error("Cannot decode incoming dataprop key str " + datapropKey + "as integer hash");
	        	//throw new JspException("Cannot decode incoming datapropKey String value "+datapropKeyStr+" as an integer hash in datapropStmtDelete.jsp");
	        }
	    }
		return dataHash;
	}


	//process object property
    private void processObjectProperty(VitroRequest vreq) {
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);
    	
    	//if this property is true, it means the object needs to be deleted along with statement
    	if(prop.getStubObjectRelation())
    	{
    		deleteObjectIndividual(vreq);
    	}
    	
    	deleteObjectPropertyStatement(vreq);
		
    }
    
    private void deleteObjectPropertyStatement(VitroRequest vreq) {
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
		//delete object property statement
		wdf.getPropertyInstanceDao().deleteObjectPropertyStatement(subjectUri, predicateUri, objectUri);
	}

	private Individual getObjectIndividualForStubRelation(VitroRequest vreq, String objectUri) {
    
    	Individual object = EditConfigurationUtils.getIndividual(vreq, objectUri);
    	if(object == null) {
    		WebappDaoFactory wadf = (WebappDaoFactory) vreq.getSession().getServletContext().getAttribute("webappDaoFactory");
    		object = wadf.getIndividualDao().getIndividualByURI(objectUri);
    	}
    	
    	return object;
    	
    }
    
    private void deleteObjectIndividual(VitroRequest vreq) {
    	String objectUri = EditConfigurationUtils.getObjectUri(vreq);
    	Individual object = getObjectIndividualForStubRelation(vreq, objectUri);
    	if(object != null) {
    		log.warn("Deleting individual " + object.getName() + "since property has been set to force range object deletion");
    		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    		wdf.getIndividualDao().deleteIndividual(object);
    	} else {
    		//TODO: Throw error?
    		log.error("could not find object as request attribute or in model " + objectUri);
    	}
    }

    

    
    
    
    
}
