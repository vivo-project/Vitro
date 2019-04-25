/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.N3EditUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;
/*
 * Custom deletion controller to which deletion requests from default property form are sent. May be replaced
 * later with additional features in process rdf form controller or alternative location.
 */
@WebServlet(name = "DeletePropertyController", urlPatterns = {"/deletePropertyController"} )
public class DeletePropertyController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(DeletePropertyController.class);

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.DO_FRONT_END_EDITING.ACTION ;
	}

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

    	String redirectUrl = getRedirectUrl(vreq);
    	return new RedirectResponseValues(redirectUrl, HttpServletResponse.SC_SEE_OTHER);
    }


    private String getRedirectUrl(VitroRequest vreq) {
		// TODO Auto-generated method stub
    	String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
    	String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	int hashIndex = predicateUri.lastIndexOf("#");
    	String localName = predicateUri.substring(hashIndex + 1);
    	String redirectUrl =  "/entity?uri=" + URLEncoder.encode(subjectUri);
		return redirectUrl + "#" + URLEncoder.encode(localName);
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
    		DataProperty prop = getDataProperty(vreq);

    		if(prop == null) {
    			return "In delete property controller, could not find data property " + predicateUri;
    		}
    	}

    	return null;

	}

	private DataProperty getDataProperty(VitroRequest vreq) {
		//This is the standard mechanism but note that datapropStmtDelete uses wdf with user aware

		//DataProperty prop = EditConfigurationUtils.getDataProperty(vreq);
		String editorUri = N3EditUtils.getEditorUri(vreq);
		WebappDaoFactory wdf = vreq.getWebappDaoFactory().getUserAwareDaoFactory(editorUri);
		DataProperty prop = wdf.getDataPropertyDao().getDataPropertyByURI(
				EditConfigurationUtils.getPredicateUri(vreq));
		return prop;
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
    	String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);

    	int dataHash = EditConfigurationUtils.getDataHash(vreq);
		DataPropertyStatement dps = EditConfigurationUtils.getDataPropertyStatement(vreq, vreq.getSession(), dataHash, predicateUri);
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();



		if(dps != null) {
			logDataPropertyDeletionMessages(dps);
			processDataPropertyStatement(dps, subjectUri, predicateUri);
			wdf.getDataPropertyStatementDao().deleteDataPropertyStatement(dps);
		}
	}


	private void processDataPropertyStatement(
			DataPropertyStatement dps, String subjectUri, String predicateUri) {
		 //if no individual Uri set to subject uri
		if( dps.getIndividualURI() == null || dps.getIndividualURI().trim().length() == 0){
	        log.debug("adding missing subjectURI to DataPropertyStatement" );
	        dps.setIndividualURI( subjectUri );
	    }
		//if no predicate, set predicate uri
	    if( dps.getDatapropURI() == null || dps.getDatapropURI().trim().length() == 0){
	        log.debug("adding missing datapropUri to DataPropertyStatement");
	        dps.setDatapropURI( predicateUri );
	    }
	}


	private void logDataPropertyDeletionMessages(DataPropertyStatement dps) {
		log.debug("attempting to delete dataPropertyStatement: subjectURI <" + dps.getIndividualURI() +">");
        log.debug( "predicateURI <" + dps.getDatapropURI() + ">");
        log.debug( "literal \"" + dps.getData() + "\"" );
        log.debug( "lang @" + (dps.getLanguage() == null ? "null" : dps.getLanguage()));
        log.debug( "datatype ^^" + (dps.getDatatypeURI() == null ? "null" : dps.getDatatypeURI() ));

	}

	//process object property
    private void processObjectProperty(VitroRequest vreq) {
    	ObjectProperty prop = EditConfigurationUtils.getObjectProperty(vreq);

    	//if this property is true, it means the object needs to be deleted along with statement
    	//while the second test is to see if a different object uri (i.e. not the direct objet of the predicate)
    	//needs to be deleted
    	if(prop.getStubObjectRelation() || hasDeleteObjectUri(vreq))
    	{
    		deleteObjectIndividual(vreq);
    	}

    	if(!hasDeleteObjectUri(vreq)) {
    		deleteObjectPropertyStatement(vreq);
    	}

    }

    private void deleteObjectPropertyStatement(VitroRequest vreq) {
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		String objectUri = EditConfigurationUtils.getObjectUri(vreq);
		String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
		String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
		//delete object property statement
		wdf.getPropertyInstanceDao().deleteObjectPropertyStatement(subjectUri, predicateUri, objectUri);
	}

	private Individual getObjectIndividualForDeletion(VitroRequest vreq, String objectUri) {

    	Individual object = EditConfigurationUtils.getIndividual(vreq, objectUri);
    	if(object == null) {
			WebappDaoFactory wadf = ModelAccess.on(vreq.getSession().getServletContext()).getWebappDaoFactory();
    		object = wadf.getIndividualDao().getIndividualByURI(objectUri);
    	}

    	return object;

    }

    private void deleteObjectIndividual(VitroRequest vreq) {
    	String objectUri = EditConfigurationUtils.getObjectUri(vreq);
    	if(hasDeleteObjectUri(vreq)) {
    		//if a different individual needs to be deleted, get that uri instead
    		objectUri = getDeleteObjectUri(vreq);
    	}
    	Individual object = getObjectIndividualForDeletion(vreq, objectUri);
    	if(object != null) {
    		log.warn("Deleting individual " + object.getName() + "since property has been set to force range object deletion or has been set to delete a specific object");
    		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    		wdf.getIndividualDao().deleteIndividual(object);
    	} else {
    		//TODO: Throw error?
    		log.error("could not find object as request attribute or in model " + objectUri);
    	}
    }

    //This checks if the object uri is not the individual to be deleted but another individual connected
    private String getDeleteObjectUri(VitroRequest vreq) {
    	return (String) vreq.getParameter("deleteObjectUri");
    }

    private boolean hasDeleteObjectUri(VitroRequest vreq) {
    	String deleteObjectUri = getDeleteObjectUri(vreq);
    	return (deleteObjectUri != null && !deleteObjectUri.isEmpty());
    }







}
