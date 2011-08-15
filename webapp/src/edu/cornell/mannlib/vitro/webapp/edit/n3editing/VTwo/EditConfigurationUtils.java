/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class EditConfigurationUtils {

    protected static final String MULTI_VALUED_EDIT_SUBMISSION = "MultiValueEditSubmission";
    
    /* *************** Static utility methods used in edit configuration and in generators *********** */
    public static String getSubjectUri(VitroRequest vreq) {
    	return vreq.getParameter("subjectUri");
    }
    
    public static String getPredicateUri(VitroRequest vreq) {
    	return vreq.getParameter("predicateUri");
    }
    
    /*
    public static String getData(VitroRequest vreq) {
    	return vreq.getParameter("subjectUri");
    }*/
    
    public static String getObjectUri(VitroRequest vreq) {
    	return vreq.getParameter("objectUri");
    }
    
    //get individual
    
    public static Individual getSubjectIndividual(VitroRequest vreq) {
    	Individual subject = null;
    	String subjectUri = getSubjectUri(vreq);
	    WebappDaoFactory wdf = vreq.getWebappDaoFactory();

    	 if( subjectUri != null ){
	        subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
	         if( subject != null )
	             vreq.setAttribute("subject", subject);    
	 	    }
    	return subject;
    }
    
    public static Individual getObjectIndividual(VitroRequest vreq) {
    	String objectUri = getObjectUri(vreq);
    	Individual object = null;
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();

    	 if( objectUri != null ){
	        object = wdf.getIndividualDao().getIndividualByURI(objectUri);
	         if( object != null )
	             vreq.setAttribute("subject", object);    
	 	    }
    	return object;
    }
   
    
    public static ObjectProperty getObjectProperty(VitroRequest vreq) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	String predicateUri = getPredicateUri(vreq);
    	ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    	return objectProp;
    }
    
    public static DataProperty getDataProperty(VitroRequest vreq) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	String predicateUri = getPredicateUri(vreq);
    	DataProperty dataProp = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
      	return dataProp;
    }
    
    public static String getFormUrl(VitroRequest vreq) {
    	return vreq.getContextPath() + "/edit/editRequestDispatch?" + vreq.getQueryString();
    }
    
    
    public static String getEditKey(VitroRequest vreq) {
    	HttpSession session = vreq.getSession();        
        String editKey = 
            (EditConfigurationVTwo.getEditKey(vreq) == null) 
                ? EditConfigurationVTwo.newEditKey(session)
                : EditConfigurationVTwo.getEditKey(vreq);
        return editKey;
    	
    }
    
  //is data property or vitro label
    public static boolean isDataProperty(String predicateUri, VitroRequest vreq) {
    	
    	if(isVitroLabel(predicateUri)) {
    		return true;
    	}
    	DataProperty dataProp = vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	return (dataProp != null);
    }
    
    public static String getDataPropKey(VitroRequest vreq) {
    	return vreq.getParameter("datapropKey");
    }
    //is object property
    public static boolean isObjectProperty(String predicateUri, VitroRequest vreq) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	ObjectProperty op = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    	DataProperty dp = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	return (op != null && dp == null);
    }
    
	private static boolean isVitroLabel(String predicateUri) {
		return predicateUri.equals(VitroVocabulary.LABEL);
	}
	
    public static DataPropertyStatement getDataPropertyStatement(VitroRequest vreq, HttpSession session, int dataHash, String predicateUri) {
    	DataPropertyStatement dps = null;
   	    if( dataHash != 0) {
   	        Model model = (Model)session.getServletContext().getAttribute("jenaOntModel");
   	        dps = RdfLiteralHash.getPropertyStmtByHash(EditConfigurationUtils.getSubjectIndividual(vreq), predicateUri, dataHash, model);
   	                              
   	        if (dps==null) {
   	            //log.error("No match to existing data property \""+predicateUri+"\" statement for subject \""+subjectUri+"\" via key "+datapropKeyStr);
   	            //TODO: Needs to forward to dataPropMissingStatement.jsp
   	            //return null;
   	        }                     
   	        
   	    }
   	    return dps;
    }

}
