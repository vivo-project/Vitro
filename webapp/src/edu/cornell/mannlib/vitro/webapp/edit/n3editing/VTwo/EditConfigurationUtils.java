/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit.EditConfigurationTemplateModel;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerConfigurationLoader;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import freemarker.template.Configuration;

public class EditConfigurationUtils {
	private static Log log = LogFactory.getLog(EditConfigurationUtils.class);

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
    	subject = getIndividual(vreq, subjectUri);
    	
    	 if( subject!= null ){
	         vreq.setAttribute("subject", subject);    
	 	 }
    	return subject;
    }
    
    public static Individual getIndividual(VitroRequest vreq, String uri) {
    	Individual individual = null; 
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();

    	 if( uri != null ){
	        individual = wdf.getIndividualDao().getIndividualByURI(uri);
    	 }
    	return individual;
    }
    
    public static Individual getObjectIndividual(VitroRequest vreq) {
    	String objectUri = getObjectUri(vreq);
    	Individual object = getIndividual(vreq, objectUri);
        if( object != null ) {
             vreq.setAttribute("subject", object);    
 	    }
    	return object;
    }
   
    
    public static ObjectProperty getObjectProperty(VitroRequest vreq) {
    	//gets the predicate uri from the request
    	String predicateUri = getPredicateUri(vreq);
    	return getObjectPropertyForPredicate(vreq, predicateUri);
    }
    
    public static DataProperty getDataProperty(VitroRequest vreq) {
    	String predicateUri = getPredicateUri(vreq);
    	return getDataPropertyForPredicate(vreq, predicateUri);
    }
    
    public static ObjectProperty getObjectPropertyForPredicate(VitroRequest vreq, String predicateUri) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	ObjectProperty objectProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
    	return objectProp;
    }
    
    public static DataProperty getDataPropertyForPredicate(VitroRequest vreq, String predicateUri) {
    	WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    	//TODO: Check reason for employing unfiltered webapp dao factory and note if using a different version
    	//would change the results
    	//For some reason, note that edit data prop statement request dispatch utilizes unfiltered webapp dao facotry
    	//DataProperty dataProp = wdf.getDataPropertyDao().getDataPropertyByURI(predicateUri);
    	 WebappDaoFactory unfilteredWdf = vreq.getUnfilteredWebappDaoFactory();
    	 DataProperty dataProp = unfilteredWdf.getDataPropertyDao().getDataPropertyByURI( predicateUri );
      	return dataProp;
    }
    
    public static String getFormUrl(VitroRequest vreq) {
    	return getEditUrl(vreq) + "?" + vreq.getQueryString();
    }
    
    public static String getEditUrl(VitroRequest vreq) {
    	return vreq.getContextPath() + "/editRequestDispatch";
    }
    
    public static String getCancelUrlBase(VitroRequest vreq) {
    	 return vreq.getContextPath() + "/postEditCleanupController";
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
    
    //TODO: Include get object property statement
    public static int getDataHash(VitroRequest vreq) {
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
    
    //Copied from the original input element formatting tag
    //Allows the retrieval of the string values for the literals
    //Useful for cases with date/time and other mechanisms
    public static Map<String, List<String>> getExistingLiteralValues(VitroRequest vreq, EditConfigurationVTwo editConfig) {
    	Map<String, List<String>> literalsInScopeStringValues = new HashMap<String, List<String>>();
    	Map<String, List<Literal>> literalsInScope = editConfig.getLiteralsInScope();
    	
    	for(String key: literalsInScope.keySet() ) {
    		List<String> stringValues = processLiteral(editConfig, key);
    		literalsInScopeStringValues.put(key, stringValues);
    	}
    	return literalsInScopeStringValues;
    }
    
    //Copied from input element formatting tag
    private static List<String> processLiteral(EditConfigurationVTwo editConfig, String fieldName) {
    	Map<String, List<Literal>> literalsInScope = editConfig.getLiteralsInScope();
    	List<String> stringValues = new ArrayList<String>();
		List<Literal> literalValues = literalsInScope.get(fieldName);
    	for(Literal l: literalValues) {
    		//Could do additional processing here if required, for example if date etc. if need be
    		stringValues.add(l.getValue().toString());
    	}
		return stringValues;
	}

	public static Map<String, List<String>> getExistingUriValues(EditConfigurationVTwo editConfig) {
    	return editConfig.getUrisInScope();
    }
	
	//Generate HTML for a specific field name given 
	public static String generateHTMLForElement(VitroRequest vreq, String fieldName, EditConfigurationVTwo editConfig) {
		String html = "";
        Configuration fmConfig = FreemarkerConfigurationLoader.getConfig(vreq, vreq.getSession().getServletContext());

        FieldVTwo field = editConfig == null ? null : editConfig.getField(fieldName);
        MultiValueEditSubmission editSub =  new MultiValueEditSubmission(vreq.getParameterMap(), editConfig);  
        if( field != null && field.getEditElement() != null ){
    	  html = field.getEditElement().draw(fieldName, editConfig, editSub, fmConfig);
        }
		return html;
	}
   

}
