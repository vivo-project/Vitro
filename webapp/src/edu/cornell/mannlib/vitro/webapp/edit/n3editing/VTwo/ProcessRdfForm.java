/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
public class ProcessRdfForm {
    
    private static Log log = LogFactory.getLog(ProcessRdfForm.class);   
    //Making this a global variable because this may be referenced in multiple places
    //Alternatively we could handle all new resource related information separately in its own method
    private static Map<String,List<String>> varToNewResource = null;
    
    
    
   
    /**
     * Execute any modelChangePreprocessors in the editConfiguration;
     * 
     */
    public static void preprocessModels(AdditionsAndRetractions changes, EditConfigurationVTwo editConfiguration, VitroRequest request){

        List<ModelChangePreprocessor> modelChangePreprocessors = editConfiguration.getModelChangePreprocessors();
        if ( modelChangePreprocessors != null ) {
            for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
                //these work by side effect
                pp.preprocess( changes.getRetractions(), changes.getAdditions(), request );
            }
        }                   
    }
       
    protected static AdditionsAndRetractions getMinimalChanges( AdditionsAndRetractions changes ){
        //make a model with all the assertions and a model with all the 
        //retractions, do a diff on those and then only add those to the jenaOntModel
        Model allPossibleAssertions = changes.getAdditions();
        Model allPossibleRetractions = changes.getRetractions();        
        
        //find the minimal change set
        Model assertions = allPossibleAssertions.difference( allPossibleRetractions );    
        Model retractions = allPossibleRetractions.difference( allPossibleAssertions );        
        return new AdditionsAndRetractions(assertions,retractions);
    }
        
    public static AdditionsAndRetractions addDependentDeletes( AdditionsAndRetractions changes, Model queryModel){
        //Add retractions for dependent resource delete if that is configured and 
        //if there are any dependent resources.                     
        Model depResRetractions = 
            DependentResourceDeleteJena
            .getDependentResourceDeleteForChange(changes.getAdditions(),changes.getRetractions(),queryModel);                

        changes.getRetractions().add(depResRetractions);        
        return changes; 
    }
    
    
    public static void applyChangesToWriteModel(AdditionsAndRetractions changes, OntModel queryModel, OntModel writeModel, String editorUri) {                             
        //side effect: modify the write model with the changes      
        Lock lock = null;
        try{
            lock =  writeModel.getLock();
            lock.enterCriticalSection(Lock.WRITE);
            writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,true));   
            writeModel.add( changes.getAdditions() );
            writeModel.remove( changes.getRetractions() );
        }catch(Throwable t){
            log.error("error adding edit change n3required model to in memory model \n"+ t.getMessage() );
        }finally{
            writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,false));
            lock.leaveCriticalSection();
        }          
    }

    //Create new resource
    //
    @SuppressWarnings("static-access")
    public static AdditionsAndRetractions createNewStatement(EditConfigurationVTwo editConfiguration , MultiValueEditSubmission submission, VitroRequest vreq){
    	//Get all assertions
    	List<Model> assertions = populateAssertions(editConfiguration, submission, vreq);
    	//Retractions should be empty anyway but the method should take care of that
    	List<Model> retractions = new ArrayList<Model>();
        return getMinimalChanges(new AdditionsAndRetractions(assertions, retractions));

    }
    
    public static AdditionsAndRetractions editExistingStatement(EditConfigurationVTwo editConfiguration, MultiValueEditSubmission submission, VitroRequest vreq) {
    	List<Model> fieldAssertions = populateAssertions(editConfiguration, submission, vreq);
    	List<Model> fieldRetractions = populateRetractions(editConfiguration, submission, vreq);
        return getMinimalChanges(new AdditionsAndRetractions(fieldAssertions, fieldRetractions));
    }


	/**
	 * Certain methods/mechanisms overlap across type of property (object or data) and whether or not
	 * the updates are for existing or new resource or value
	 */
	//This should be a separate method because varToNewResources is referred to in multiple places
	
	
	//get field assertions - so these appear to be employed when editing an EXISTING literal or resource
	//Also note this depends on field assertions
	public static List<Model> getRequiredFieldAssertions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		
		List<Model> requiredFieldAssertions = new ArrayList<Model>();
		//Get the original assertions from the edit configuration field
		Map<String, List<String>> fieldAssertions = Utilities.fieldsToN3Map(configuration.getFields(), Utilities.assertionsType);
		fieldAssertions = subUrisAndLiteralsInFieldAssertions(configuration, submission, vreq, fieldAssertions);
		//process assertions
		requiredFieldAssertions = processFieldAssertions(configuration, submission, vreq, fieldAssertions);
		return requiredFieldAssertions;
	}
	
	private static Map<String, List<String>> subUrisAndLiteralsInFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			VitroRequest vreq, Map<String, List<String>> fieldRetractions) {
		return subUrisAndLiteralsForField(configuration, submission, vreq, fieldRetractions);
	}

	
	//substitute uris and literals and also handle new resource
	private static Map<String, List<String>> subUrisAndLiteralsInFieldAssertions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			VitroRequest vreq, Map<String, List<String>> fieldAssertions) {
		//Substitute URIs and literals from form and from scope
		fieldAssertions = subUrisAndLiteralsForField(configuration, submission, vreq, fieldAssertions);
		fieldAssertions = subNewResourceForField(configuration, vreq, fieldAssertions);
		return fieldAssertions;
	}
	
	//For both new and existing statement, need to incorporate varToNewResource
    //Check whether data or not, to be consistent
    //Map<String, String> varToNewResource = newToUriMap
    //TODO: Check if this still needs to differentiate between object and data property
	private static Map<String, List<String>> subNewResourceForField(
			EditConfigurationVTwo configuration, VitroRequest vreq,
			Map<String, List<String>> fieldAssertions) {
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
        setVarToNewResource(configuration, vreq);
   	 	fieldAssertions = n3Subber.substituteIntoValues(varToNewResource, null, fieldAssertions );
   	 	return fieldAssertions;
	}

	//Substitue for uris and literals from both form and scope for field
	private static Map<String, List<String>> subUrisAndLiteralsForField(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			VitroRequest vreq, Map<String, List<String>> fieldN3) {
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
		//Substitute URIs and literals from form
		fieldN3 = n3Subber.substituteIntoValues(submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldN3);
		//Substitute URIS and literals from scope
        fieldN3 = n3Subber.substituteIntoValues(configuration.getUrisInScope(), configuration.getLiteralsInScope(), fieldN3 );
		return fieldN3;
	}

	//Substitute for uris and literals for n3 required or n3Optional
	private static List<String> subUrisAndLiteralsForN3(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			VitroRequest vreq, List<String> n3Statements) {
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
		
		//Substitute URIs and literals from form
		n3Statements = n3Subber.subInMultiUris(submission.getUrisFromForm(), n3Statements);
		n3Statements = n3Subber.subInMultiLiterals(submission.getLiteralsFromForm(), n3Statements);
		//Substitute URIS and literals in scope
		n3Statements = n3Subber.subInMultiUris(configuration.getUrisInScope(), n3Statements);
		n3Statements = n3Subber.subInMultiLiterals(configuration.getLiteralsInScope(), n3Statements);
		//for new resource
		setVarToNewResource(configuration, vreq);
		n3Statements = n3Subber.subInMultiUris(varToNewResource, n3Statements);
		return n3Statements;
	}

	public static void setVarToNewResource(EditConfigurationVTwo configuration, VitroRequest vreq) {
		if(varToNewResource == null) {
			//No longer using the data poperty method but just using the object processing form version
		//	if(Utilities.isDataProperty(configuration, vreq)) {
	    //   	 	OntModel resourcesModel = configuration.getResourceModelSelector().getModel(vreq,vreq.getSession().getServletContext());
	    //   	 	varToNewResource = Utilities.newToUriMap(configuration.getNewResources(),resourcesModel);
		//	} else {
	       	 	varToNewResource = Utilities.newToUriMap(configuration.getNewResources(),vreq.getWebappDaoFactory());
	       	 /*
	            if(log.isDebugEnabled()) {
	                Utilities.logAddRetract("substituted in URIs for new resources",fieldAssertions,fieldRetractions);
	       }*/
		//	}
		}
	}
	
	//this occurs for edits of existing statements whether object resource or literal
	//In data form, not only is the condition for edit check but an additional check regarding
	//has field changed is included, whereas object form depends only on whether or not this is an edit
	//Here we take care of both conditions but including a method that checks whether or not to populate the assertions
	//model
	private static List<Model> processFieldAssertions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			VitroRequest vreq,
			Map<String, List<String>> fieldAssertions) {
		List<Model> requiredFieldAssertions = new ArrayList<Model>();
		
		//Loop through field assertions
        for(String fieldName : fieldAssertions.keySet()){    
        	//this checks whether or not proceed with populating the model based on the field
           if(isGenerateModelFromField(fieldName, configuration, submission, vreq)) {
	        	List<String> assertions = fieldAssertions.get(fieldName);
	            for(String n3: assertions){
	                try{
	                    Model model = ModelFactory.createDefaultModel();
	                    StringReader reader = new StringReader(n3);
	                    model.read(reader, "", "N3");
	                    requiredFieldAssertions.add(model);
	                }catch(Throwable t){
	                    String errMsg = "error processing N3 assertion string from field " + fieldName + "\n" +
	                    t.getMessage() + '\n' + "n3: \n" + n3;
	                    //errorMessages.add(errMsg);
	                    log.error(errMsg);
	                    //TODO:Check whether need to throw exception here
	                }
	            }
           }
        }
        
        //if data property - since only see that there - then check for empty string condition
        //which means only one value and it is an empty string
        if(Utilities.checkForEmptyString(submission, configuration, vreq)) {
        	requiredFieldAssertions.clear();
        }
		return requiredFieldAssertions;
	}
		
	//Process Entity to Return to - substituting uris etc. 
	public static String processEntityToReturnTo(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		List<String> entityToReturnTo = new ArrayList<String>();
		String entity = configuration.getEntityToReturnTo();
		entityToReturnTo.add(entity);
		//Substitute uris and literals on form
		//Substitute uris and literals in scope
		//Substite var to new resource
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
		
		//Substitute URIs and literals from form
		entityToReturnTo = n3Subber.subInMultiUris(submission.getUrisFromForm(), entityToReturnTo);
		entityToReturnTo = n3Subber.subInMultiLiterals(submission.getLiteralsFromForm(), entityToReturnTo);
		setVarToNewResource(configuration, vreq);
		entityToReturnTo = n3Subber.subInMultiUris(varToNewResource, entityToReturnTo);
		
		String processedEntity = entityToReturnTo.get(0);
		if(processedEntity != null) {
			
			processedEntity = processedEntity.trim().replaceAll("<","").replaceAll(">","");       
		}
		return processedEntity;

	}
	
	public static boolean isGenerateModelFromField(String fieldName, EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		if(Utilities.isObjectProperty(configuration, vreq)) {
			return true;
		}
		if(Utilities.isDataProperty(configuration, vreq)) {
			if(Utilities.hasFieldChanged(fieldName, configuration, submission)) {
				return true;
			}
		}
		return false;
	}
	
		
	public static List<Model> getRequiredFieldRetractions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		List<Model> requiredFieldRetractions = new ArrayList<Model>();
		//TODO: Check if need to check twice or if once is sufficient?
		//if adding new object no retractions, although this should be empty if adding new literal too?
		if(!configuration.isDataPropertyUpdate() && !configuration.isObjectPropertyUpdate()) {
			return new ArrayList<Model>();
		}
		//else populate
		//If data property, field retractions based on field alone and if object property additional 
		//retraction processing required
		if(configuration.isObjectPropertyUpdate()){
			Map<String, List<String>> fieldRetractions = Utilities.fieldsToN3Map(configuration.getFields(), Utilities.retractionsType);
			//sub in uris and literals for field
			fieldRetractions = subUrisAndLiteralsInFieldRetractions(configuration, submission, vreq, fieldRetractions);
			requiredFieldRetractions = processFieldRetractions(configuration, submission, vreq, fieldRetractions);
		} 
		if(configuration.isDataPropertyUpdate()) {
			//this simply goes through each field and checks if it has retractions
			requiredFieldRetractions = processFieldRetractions(configuration, submission, vreq);
		}
		return requiredFieldRetractions;
	}
	
	//this expects an input map with retractions populated and and with uris/literals subbed 
	private static List<Model> processFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq,
			Map<String, List<String>> fieldRetractions) {
		List<Model> requiredFieldRetractions = new ArrayList<Model>();
		//Get key set for fields
		Map<String, FieldVTwo> fields = configuration.getFields();
		for(String fieldName: fields.keySet()) {
			//get retractions from field retractions for this field - post uri substitution etc. 
			List<String> retractions = fieldRetractions.get(fieldName);
		    for( String n3 : retractions ){
	            try{
	                Model model = ModelFactory.createDefaultModel();
	                StringReader reader = new StringReader(n3);
	                model.read(reader, "", "N3");
	                requiredFieldRetractions.add(model);
	            }catch(Throwable t){
	            	String errMsg = "error processing N3 retraction string from field " + fieldName + "\n"+
	                t.getMessage() + '\n' +
	                "n3: \n" + n3;
	                //errorMessages.add(errMsg);
	                log.error(errMsg);
	            }
	        }
	    }
		return requiredFieldRetractions;
	}
	
	//data property version , gets retractions from each field
	private static List<Model> processFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq) {
		List<Model> requiredFieldRetractions = new ArrayList<Model>();
		//Get key set for fields
		Map<String, FieldVTwo> fields = configuration.getFields();
		for(String fieldName: fields.keySet()) {
			//get retractions from field retractions for this field - post uri substitution etc. 
			FieldVTwo field = fields.get(fieldName);
			if(Utilities.hasFieldChanged(fieldName, configuration, submission)) {
				List<String> retractions = field.getRetractions();
				if(retractions != null) {
					for( String n3 : retractions ){
			            try{
			                Model model = ModelFactory.createDefaultModel();
			                StringReader reader = new StringReader(n3);
			                model.read(reader, "", "N3");
			                requiredFieldRetractions.add(model);
			            }catch(Throwable t){
			            	String errMsg = "error processing N3 retraction string from field " + fieldName + "\n"+
			                t.getMessage() + '\n' +
			                "n3: \n" + n3;
			                //errorMessages.add(errMsg);
			                log.error(errMsg);
			            }
			        }

				}
			}
		    	    }
		return requiredFieldRetractions;
	}

	//required assertions based on N3
	public static List<Model> getRequiredN3Assertions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		List<Model> requiredN3Assertions = new ArrayList<Model>();
		List<String> n3Required = configuration.getN3Required();
		//Substitute uris and literals, including for 
		n3Required = subUrisAndLiteralsForN3(configuration, submission, vreq, n3Required);
		requiredN3Assertions = processN3Assertions(configuration, submission, vreq, n3Required);
		return requiredN3Assertions;
	}
	
private static List<Model> processN3Assertions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq,
			List<String> n3Statements) {
	 //deal with required N3
	    List<Model> n3Models = new ArrayList<Model>();
	     for(String n3 : n3Statements){
	         try{
	             Model model = ModelFactory.createDefaultModel();
	             StringReader reader = new StringReader(n3);
	             model.read(reader, "", "N3");
	             n3Models.add( model );
	         }catch(Throwable t){
	             log.error("error processing required n3 string \n"+
	                     t.getMessage() + '\n' +
	                     "n3: \n" + n3 );
	         }
	     }
		return n3Models;
	}

//there are no retractions based on N3 since N3 is only employed when new literal or resource being processed
	
	//optional field assertions
	//if data property, then this would be empty
	public static List<Model> getOptionalN3Assertions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		
		if(Utilities.isDataProperty(configuration, vreq)) {
			return new ArrayList<Model>();
		}
		//if object property and existing prop update then return null
		//otherwise this needs to be populated
		if(Utilities.isObjectProperty(configuration, vreq) && configuration.isObjectPropertyUpdate()) {
			return new ArrayList<Model>();
		}
		
		//populate
		List<Model> optionalN3Assertions = new ArrayList<Model>();
		List<String> n3Optional = configuration.getN3Optional();
		//Substitute uris and literals, including for 
		n3Optional = subUrisAndLiteralsForN3(configuration, submission, vreq, n3Optional);
		optionalN3Assertions = processN3Assertions(configuration, submission, vreq, n3Optional);
		return optionalN3Assertions;
	}

	//"final" or general methods- get the assertions and retractions
	public static List<Model> populateRetractions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		List<Model> retractions = new ArrayList<Model>();
		//if adding new object no retractions, although this should be empty if adding new literal too?
		if(!configuration.isDataPropertyUpdate() && !configuration.isObjectPropertyUpdate()) {
			return new ArrayList<Model>();
		}
		
		retractions = getRequiredFieldRetractions(configuration, submission, vreq);

		return retractions;
	}
	
	//generally should always have assertions
	public static List<Model> populateAssertions(EditConfigurationVTwo configuration, MultiValueEditSubmission submission, VitroRequest vreq) {
		List<Model> assertions = new ArrayList<Model>();
		//if editing existing statement, then assertions based on field
		if(configuration.isDataPropertyUpdate() || configuration.isObjectPropertyUpdate()) {
			assertions = getRequiredFieldAssertions(configuration, submission, vreq);
		}	
		//otherwise, if new literal or value, assertions generated from n3 required or n3 optional statements
		else {
			assertions = getRequiredN3Assertions(configuration, submission, vreq);
			assertions.addAll(getOptionalN3Assertions(configuration, submission, vreq));
		}
		return assertions;
	}
	
    
    private static boolean logAddRetract(String msg, Map<String,List<String>>add, Map<String,List<String>>retract){
        log.debug(msg);
        if( add != null ) log.debug( "assertions: " + add.toString() );
        if( retract != null ) log.debug( "retractions: " +  retract.toString() );
        return true;
    }
  
    private static boolean logRequiredOpt(String msg, List<String>required, List<String>optional){
        log.debug(msg);
        if( required != null ) log.debug( "required: " + required.toString() );
        if( optional != null ) log.debug( "optional: " +  optional.toString() );
        return true;
    }

	public static void updateEditConfigurationForBackButton(
			EditConfigurationVTwo editConfig,
			MultiValueEditSubmission submission, VitroRequest vreq, OntModel writeModel) {
		
		//now setup an EditConfiguration so a single back button submissions can be handled
		//Do this if data property
		if(EditConfigurationUtils.isDataProperty(editConfig.getPredicateUri(), vreq)) {
		    EditConfigurationVTwo copy = editConfig.copy();
		    
		    //need a new DataPropHash and a new editConfig that uses that, and replace 
		    //the editConfig used for this submission in the session.  The same thing
		    //is done for an update or a new insert since it will convert the insert
		    //EditConfig into an update EditConfig.
		    log.debug("attempting to make an updated copy of the editConfig for browser back button support");
		    FieldVTwo dataField = copy.getField(copy.getVarNameForObject());
		     
		    DataPropertyStatement dps = new DataPropertyStatementImpl();
		    List<Literal> submitted = submission.getLiteralsFromForm().get(copy.getVarNameForObject());
		    if( submitted != null && submitted.size() > 0){
		    	for(Literal submittedLiteral: submitted) {
			    	dps.setIndividualURI( copy.getSubjectUri() );
			    	dps.setDatapropURI( copy.getPredicateUri() );
			    	dps.setDatatypeURI( submittedLiteral.getDatatypeURI());
			    	dps.setLanguage( submittedLiteral.getLanguage() );
			    	dps.setData( submittedLiteral.getLexicalForm() );
			       
			    	copy.prepareForDataPropUpdate(writeModel, dps);
			    	copy.setDatapropKey( Integer.toString(RdfLiteralHash.makeRdfLiteralHash(dps)) );
		    	}
		    	EditConfigurationVTwo.putConfigInSession(copy,vreq.getSession());
		    }
		}
		
	}


}
