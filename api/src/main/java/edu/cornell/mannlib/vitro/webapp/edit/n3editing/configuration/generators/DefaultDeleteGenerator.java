/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * Generates delete form which submits the deletion request to the deletion controller.
 * This is the page to which the user is redirected if they select Delete on the default property form. 
 *
 */
public class DefaultDeleteGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {
	
	private Log log = LogFactory.getLog(DefaultObjectPropertyFormGenerator.class);
	private String subjectUri = null;
	private String predicateUri = null;
	private String objectUri = null;	
	private Integer dataHash = 0;
	private DataPropertyStatement dps = null;
	private String dataLiteral = null;
	private String template = "confirmDeletePropertyForm.ftl";
	private static HashMap<String,String> defaultsForXSDtypes ;
	  static {
		defaultsForXSDtypes = new HashMap<String,String>();
		//defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","2001-01-01T12:00:00");
		defaultsForXSDtypes.put("http://www.w3.org/2001/XMLSchema#dateTime","#Unparseable datetime defaults to now");
	  }
	  
	//In this case, simply return the edit configuration currently saved in session
	//Since this is forwarding from another form, an edit configuration should already exist in session
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) {
    	EditConfigurationVTwo editConfiguration = EditConfigurationVTwo.getConfigFromSession(session, vreq);
    	//Two paths for deletion: (i) from front page and (ii) from edit page of individual
    	//If (ii), edit configuration already exists but if (i) no edit configuration exists or is required for deletion
    	//so stub will be created that contains a minimal set of information
    	//Set template to be confirm delete
    	if(editConfiguration == null) {
    		editConfiguration = setupEditConfiguration(vreq, session);
    	}
    	editConfiguration.setTemplate(template);
    	//prepare update?
    	prepare(vreq, editConfiguration);
    	return editConfiguration;
    }

	private EditConfigurationVTwo  setupEditConfiguration(VitroRequest vreq, HttpSession session) {
		EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
		initProcessParameters(vreq, session, editConfiguration);
		//set edit key for this as well
		editConfiguration.setEditKey(editConfiguration.newEditKey(session));
		return editConfiguration;
		
	}
	
	//Do need to know whether data or object property and how to handle that
    private void initProcessParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
    	subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
    	predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
    	editConfiguration.setSubjectUri(subjectUri);
    	editConfiguration.setPredicateUri(predicateUri);
    	editConfiguration.setEntityToReturnTo(subjectUri);
    	editConfiguration.setUrlPatternToReturnTo("/individual");

    	if(EditConfigurationUtils.isObjectProperty(predicateUri, vreq)) {
    		//not concerned about remainder, can move into default obj prop form if required
    		this.initObjectParameters(vreq);
    		this.processObjectPropForm(vreq, editConfiguration);
    	} else {    		
    	   this.processDataPropForm(vreq, session, editConfiguration);
    	}
    }


    
	private void initObjectParameters(VitroRequest vreq) {
		//in case of object property
    	objectUri = EditConfigurationUtils.getObjectUri(vreq);
	}

	private void processObjectPropForm(VitroRequest vreq, EditConfigurationVTwo editConfiguration) {
    	editConfiguration.setObject(objectUri);
    	//this needs to be set for the editing to be triggered properly, otherwise the 'prepare' method
    	//pretends this is a data property editing statement and throws an error
    	//TODO: Check if null in case no object uri exists but this is still an object property
    }
    
	
    private void processDataPropForm(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
        dataHash = EditConfigurationUtils.getDataHash(vreq);
        if( dataHash != null ){     
            log.debug("Found a datapropKey in parameters and parsed it to int: " + dataHash);
            editConfiguration.setDatapropKey( dataHash );
            dps = EditConfigurationUtils.getDataPropertyStatement(vreq, session, dataHash, predicateUri);
            if( dps != null ){
                editConfiguration.addFormSpecificData("dataPropertyLexicalValue", dps.getData());
            }else{
                editConfiguration.addFormSpecificData("dataPropertyLexicalValue", "unknown value");
            }
        }else{
            log.debug("Did NOT find a datapropKey for hte data hash.");
            editConfiguration.addFormSpecificData("dataPropertyLexicalValue", "unknown value");
        }                
    }
    


}
