/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;

/**
 * The goal of this class is to provide processing from 
 * an EditConfiguration and an EditSubmission to produce
 * a set of additions and retractions.
 * 
 * When working with the default object property form or the 
 * default data property from, the way to avoid having 
 * any optional N3 is to originally configure the 
 * configuration.setN3Optional() to be empty. 
 */
public class ProcessRdfForm {               
       
    /**
     * This detects if this is an edit of an existing statement or an edit
     * to create a new statement or set of statements. Then the correct
     * method will be called to convert the EditConfiguration and EditSubmission
     * into a set of additions and retractions.
     * 
     * This will handle data property editing, object property editing 
     * and general editing.
     * 
     * @throws Exception May throw an exception if Required N3 does not
     * parse correctly.
     */
    public static AdditionsAndRetractions  process(
            EditConfigurationVTwo configuration,
            MultiValueEditSubmission submission, 
            NewURIMaker newURIMaker) 
    throws Exception{  
        log.debug("configuration:\n" + configuration.toString());
        log.debug("submission:\n" + submission.toString());
        
        applyEditSubmissionPreprocessors( configuration, submission );
        
        AdditionsAndRetractions changes;
        if( configuration.isUpdate() ){
            changes = editExistingStatements(configuration, submission, newURIMaker);
        } else {
            changes = createNewStatements(configuration, submission, newURIMaker);
        }       

        changes = getMinimalChanges(changes);      
        logChanges( configuration, changes);        
        
        return changes;
    }
    
    /** 
     * Processes an EditConfiguration for to create a new statement or a 
     * set of new statements.
     *  
     * This will handle data property editing, object property editing 
     * and general editing.
     * 
     * When working with the default object property form or the 
     * default data property from, the way to avoid having 
     * any optional N3 is to originally configure the 
     * configuration.setN3Optional() to be empty. 
     * 
     * @throws Exception May throw an exception if the required N3 
     * does not parse.
     */        
    @SuppressWarnings("static-access")
    private static AdditionsAndRetractions createNewStatements(
            EditConfigurationVTwo configuration, 
            MultiValueEditSubmission submission, 
            NewURIMaker newURIMaker) throws Exception {        
        
        log.debug("in createNewStatements()" );
        
        EditN3GeneratorVTwo n3Populator  = configuration.getN3Generator();
        
        List<String> requiredN3 = configuration.getN3Required();
        List<String> optionalN3 = configuration.getN3Optional();                        
        logRequiredOpt("Original valus for required and optional", requiredN3, optionalN3);
        
        /* add subject from configuration */
        requiredN3 = n3Populator.subInUris(getSubPedObjVarMap(configuration), requiredN3);
        optionalN3 = n3Populator.subInUris(getSubPedObjVarMap(configuration), optionalN3);
        logRequiredOpt("attempted to substitute in subject, predicate and object from configuration", requiredN3, optionalN3);
        
        /* add URIs from the form/EditSubmission */
        requiredN3 = n3Populator.subInMultiUris(submission.getUrisFromForm(), requiredN3);
        optionalN3 = n3Populator.subInMultiUris(submission.getUrisFromForm(), optionalN3);
        logRequiredOpt("substitued in URIs from submission", requiredN3, optionalN3);
                
        /* add Literals from the form/EditSubmission */
        requiredN3 = n3Populator.subInMultiLiterals(submission.getLiteralsFromForm(), requiredN3);
        optionalN3 = n3Populator.subInMultiLiterals(submission.getLiteralsFromForm(), optionalN3); 
        logRequiredOpt("substitued in Literals from form", requiredN3, optionalN3);
        
        /* Add URIs in scope */
        requiredN3 = n3Populator.subInMultiUris(configuration.getUrisInScope(), requiredN3);
        optionalN3 = n3Populator.subInMultiUris(configuration.getUrisInScope(), optionalN3);
        logRequiredOpt("substitued in URIs from configuration scope", requiredN3, optionalN3);
        
        /* Add Literals in scope */
        requiredN3 = n3Populator.subInMultiLiterals(configuration.getLiteralsInScope(), requiredN3);
        optionalN3 = n3Populator.subInMultiLiterals(configuration.getLiteralsInScope(), optionalN3);                        
        logRequiredOpt("substitued in Literals from scope", requiredN3, optionalN3);
        
        /* add URIs for new resources */
        Map<String,String> urisForNewResources = URIsForNewRsources(configuration, newURIMaker);
        
        requiredN3 = n3Populator.subInUris(urisForNewResources, requiredN3);
        optionalN3 = n3Populator.subInUris(urisForNewResources, optionalN3);
        logRequiredOpt("substitued in new resource URIs", requiredN3, optionalN3);
        
        /* parse N3 to RDF Models */
        List<Model> assertions = parseN3ToRDF( requiredN3 , REQUIRED );
        assertions.addAll( parseN3ToRDF( optionalN3, OPTIONAL ));                                
        
        /* No retractions since all of the statements are new. */         
        List<Model> retractions = Collections.emptyList();                
        return new AdditionsAndRetractions(assertions, retractions);
    }


    /** 
    * Process an EditConfiguration to edit a set of existing statements.
    * 
    * In this method, the N3 associated with fields that changed be
    * processed in two sets, one for the state before the edit and
    * another for the state after the edit.
    * 
    * This will handle data property editing, object property editing 
    * and general editing. 
    */   
    private static AdditionsAndRetractions editExistingStatements(
            EditConfigurationVTwo editConfiguration,
            MultiValueEditSubmission submission, 
            NewURIMaker newURIMaker) {

        log.debug("in editExistingStatements()");
        
        List<Model> fieldAssertions = populateAssertions(editConfiguration, submission, newURIMaker);
        List<Model> fieldRetractions = populateRetractions(editConfiguration, submission, newURIMaker);
        return new AdditionsAndRetractions(fieldAssertions, fieldRetractions);
    }
    
    //TODO: maybe move this to utils or contorller?
    public static AdditionsAndRetractions addDependentDeletes( AdditionsAndRetractions changes, Model queryModel){
        //Add retractions for dependent resource delete if that is configured and 
        //if there are any dependent resources.                     
        Model depResRetractions = 
            DependentResourceDeleteJena
            .getDependentResourceDeleteForChange(changes.getAdditions(),changes.getRetractions(),queryModel);                

        changes.getRetractions().add(depResRetractions);        
        return changes; 
    }
       
    //TODO: move this to utils or controller?
    public static void applyChangesToWriteModel(
            AdditionsAndRetractions changes, 
            OntModel queryModel, OntModel writeModel, String editorUri) {                             
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
        
    /**
     * Parse the n3Strings to a List of RDF Model objects.
     * 
     * @param n3Strings
     * @param parseType if OPTIONAL, then don't throw exceptions on errors
     * If REQUIRED, then throw exceptions on errors.
     * @throws Exception 
     */
    private static List<Model> parseN3ToRDF(
            List<String> n3Strings, N3ParseType parseType ) throws Exception {
       List<String> errorMessages = new ArrayList<String>();
       
        List<Model> rdfModels = new ArrayList<Model>();
        for(String n3 : n3Strings){
            try{
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                rdfModels.add( model );
            }catch(Throwable t){
                errorMessages.add(t.getMessage() + "\nN3: \n" + n3 + "\n");
            }
        }
        
        String errors = "";
        for( String errorMsg : errorMessages){
            errors += errorMsg + '\n';
        }
        
       if( !errorMessages.isEmpty() ){
           if( REQUIRED.equals(parseType)  ){        
               throw new Exception("Errors processing required N3. The EditConfiguration should " +
                    "be setup so that if a submission passes validation, there will not be errors " +
                    "in the required N3.\n" +  errors );
           }else if( OPTIONAL.equals(parseType) ){
               log.debug("Some Optional N3 did not parse, if a optional N3 does not parse it " +
                    "will be ignored.  This allows optional parts of a form submission to " +
                    "remain unfilled out and then the optional N3 does not get values subsituted in from" +
                    "the form submission values.  It may also be the case that there are unintentional " +
                    "syntax errors the optional N3." );
               log.debug( errors );                            
           }
       }
              
       return rdfModels;       
    }

    


    //optional field assertions
	//if data property, then this would be empty
	public static List<Model> getOptionalN3Assertions(
	        EditConfigurationVTwo configuration, MultiValueEditSubmission submission, 
	        NewURIMaker newURIMaker) {
		
	    //Default object property form and default data property form 
	    // avoid having the optional N3 assertions worked with by 
	    // not configuring anything in configuration.setN3Optional()
	    		
		List<Model> optionalN3Assertions = new ArrayList<Model>();
		List<String> n3Optional = configuration.getN3Optional();
		//Substitute uris and literals, including for 
		n3Optional = subUrisAndLiteralsForN3(configuration, submission, newURIMaker, n3Optional);
		optionalN3Assertions = processN3Assertions(configuration, submission, newURIMaker, n3Optional);
		return optionalN3Assertions;
	}
    

    /**
	 * Certain methods/mechanisms overlap across type of property (object or data) and whether or not
	 * the updates are for existing or new resource or value
	 */

	
	//get field assertions - so these appear to be employed when editing an EXISTING literal or resource
	//Also note this depends on field assertions
	public static List<Model> getRequiredFieldAssertions(
	        EditConfigurationVTwo configuration, 
	        MultiValueEditSubmission submission,
	        NewURIMaker newURIMaker) {
		
		List<Model> requiredFieldAssertions = new ArrayList<Model>();
		//Get the original assertions from the edit configuration field
		Map<String, List<String>> fieldAssertions = 
		    Utilities.fieldsToN3Map(configuration.getFields(), Utilities.assertionsType);
		
		fieldAssertions = subUrisAndLiteralsInFieldAssertions(configuration, submission, newURIMaker, fieldAssertions);
		//process assertions
		requiredFieldAssertions = parseFieldAssertions( fieldAssertions );
		return requiredFieldAssertions;
	}


	public static List<Model> getRequiredFieldRetractions(	        
	        EditConfigurationVTwo configuration, 
	        MultiValueEditSubmission submission ) {
		
	    List<Model> requiredFieldRetractions = new ArrayList<Model>();

	    //TODO: Huda: Check if need to check twice or if once is sufficient?
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
			//fieldRetractions = subUrisAndLiteralsInFieldRetractions(configuration, submission, fieldRetractions);
			requiredFieldRetractions = processFieldRetractions(configuration, submission,  fieldRetractions);
		} 
		if(configuration.isDataPropertyUpdate()) {
			//this simply goes through each field and checks if it has retractions
			requiredFieldRetractions = processFieldRetractions(configuration, submission);
		}
		return requiredFieldRetractions;
	}

	//required assertions based on N3
	public static List<Model> getRequiredN3Assertions(
	        EditConfigurationVTwo configuration, MultiValueEditSubmission submission, 
	        NewURIMaker newURIMaker) {
	    		
		List<String> n3Required = configuration.getN3Required();
		//Substitute uris and literals, including for 
		n3Required = subUrisAndLiteralsForN3(configuration, submission, newURIMaker, n3Required);
		return processN3Assertions(configuration, submission, newURIMaker, n3Required);		 
	}


	/**
	 * TODO: bdc34: what does this check? Why?
	 */
	public static boolean isGenerateModelFromField(
	        String fieldName, 
	        EditConfigurationVTwo configuration, MultiValueEditSubmission submission) {
//		if(Utilities.isObjectProperty(configuration, vreq)) {
//			return true;
//		}
//		if(Utilities.isDataProperty(configuration, vreq)) {
//			if(Utilities.hasFieldChanged(fieldName, configuration, submission)) {
//				return true;
//			}
//		}
		return false;
	}	
	
	private static boolean logRequiredOpt(String msg, List<String>required, List<String>optional){
	    if( log.isDebugEnabled() ){
	        String out = msg + "\n";
	        
	        if( required != null ){
	            out += "required:\n" ;
	            for( String str : required){
	                out +=  "    " + str + "\n";
	            }
	        }else{
	            out += "    No required\n";
	        }
	        
	        if( optional != null ){
                out += "optional:\n";
                for( String str : optional){
                    out += "    " + str + "\n";
                }                
            }else{
                out += "   No Optional\n";
            }
	        
	    }
        return true;
    }
		
	//generally should always have assertions
	private static List<Model> populateAssertions(
	        EditConfigurationVTwo configuration, MultiValueEditSubmission submission, 
	        NewURIMaker newURIMaker) {
		List<Model> assertions = new ArrayList<Model>();
		//if editing existing statement, then assertions based on field
		if(configuration.isDataPropertyUpdate() || configuration.isObjectPropertyUpdate()) {
			assertions = getRequiredFieldAssertions(configuration, submission, newURIMaker);
		}	
		//otherwise, if new literal or value, assertions generated from n3 required or n3 optional statements
		else {

		}
		return assertions;
	}
	
	//"final" or general methods- get the assertions and retractions
	private static List<Model> populateRetractions(
	        EditConfigurationVTwo configuration, MultiValueEditSubmission submission, 
	        NewURIMaker newURIMaker) {
		List<Model> retractions = new ArrayList<Model>();
		//if adding new object no retractions, although this should be empty if adding new literal too?
		if(!configuration.isDataPropertyUpdate() && !configuration.isObjectPropertyUpdate()) {
			return new ArrayList<Model>();
		}
		
		//retractions = getRequiredFieldRetractions(configuration, submission, newURIMaker);

		return retractions;
	}
	
		
	//this occurs for edits of existing statements whether object resource or literal
	//In data form, not only is the condition for edit check but an additional check regarding
	//has field changed is included, whereas object form depends only on whether or not this is an edit
	//Here we take care of both conditions but including a method that checks whether or not to populate the assertions
	//model
	
	/** Parse field assertion N3 to RDF Model objects */
	private static List<Model> parseFieldAssertions(
//			EditConfigurationVTwo configuration,
//			MultiValueEditSubmission submission,			
			Map<String, List<String>> fieldAssertions) {
	    
		List<Model> requiredFieldAssertions = new ArrayList<Model>();
		List<String> errorMessages = new ArrayList<String>();
		
		//Loop through field assertions
        for(String fieldName : fieldAssertions.keySet()){    
        	//this checks whether or not proceed with populating the model based on the field
//           if(isGenerateModelFromField(fieldName, configuration, submission)) {
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
	                    errorMessages.add(errMsg);	                    	                    
	                }
	            //}
           }
        }
        
        log.error("bdc34: is this code every used?");
        if( !errorMessages.isEmpty() ){
            String msg = "Error processing required N3.\n";
            for( String em : errorMessages){
                msg += em + '\n';
            }
            throw new Error(msg);            
        }
        
        //if data property - since only see that there - then check for empty string condition
        //which means only one value and it is an empty string
        //TODO: bdc34 why is this happening here?  Could this happen in a default data property form 
        // model processor?
//        if(Utilities.checkForEmptyString(submission, configuration, vreq)) {
//        	requiredFieldAssertions.clear();
//        }
		return requiredFieldAssertions;
	}
	
	//data property version , gets retractions from each field
	private static List<Model> processFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission) {
	    
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
	
	//this expects an input map with retractions populated and and with uris/literals subbed 
	private static List<Model> processFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, 
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

	private static List<Model> processN3Assertions(
			EditConfigurationVTwo configuration, MultiValueEditSubmission submission, 
			NewURIMaker newURIMaker,
			List<String> n3Statements) {

	    //deal with required N3, any that cannot 
	    //be parsed are serious configuration errors
	    List<String> errorMessages = new ArrayList<String>();
	    List<Model> n3Models = new ArrayList<Model>();
	     for(String n3 : n3Statements){
	         try{
	             Model model = ModelFactory.createDefaultModel();
	             StringReader reader = new StringReader(n3);
	             model.read(reader, "", "N3");
	             n3Models.add( model );
	         }catch(Throwable t){
	             errorMessages.add( t.getMessage() + '\n' +
	                     "Required N3: \n" + n3 );
	         }
	     }
	     
	     if( !errorMessages.isEmpty() ){
	            String msg = "Error processing required N3.\n";
	            for( String em : errorMessages){
	                msg += em + '\n';
	            }
	            throw new Error(msg);            
	     }
	     
		return n3Models;
	}
	
	private static void setVarToNewResource(EditConfigurationVTwo configuration, NewURIMaker newURIMaker) {
//		if(varToNewResource == null) {
//		    varToNewResource = Utilities.newToUriMap(configuration.getNewResources(),newURIMaker);
//		}
	}

//there are no retractions based on N3 since N3 is only employed when new literal or resource being processed
	
	//For both new and existing statement, need to incorporate varToNewResource
    //Check whether data or not, to be consistent
    //Map<String, String> varToNewResource = newToUriMap
    //TODO: Check if this still needs to differentiate between object and data property
	private static Map<String, List<String>> subNewResourceForField(
			EditConfigurationVTwo configuration, NewURIMaker newURIMaker,
			Map<String, List<String>> fieldAssertions) {
	    
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
        setVarToNewResource(configuration, newURIMaker);
   	 	//fieldAssertions = n3Subber.substituteIntoValues(varToNewResource, null, fieldAssertions );
   	 	return fieldAssertions;
	}

	//Substitue for uris and literals from both form and scope for field
	private static Map<String, List<String>> subUrisAndLiteralsForField(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			Map<String, List<String>> fieldN3) {
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
			NewURIMaker newURIMaker, List<String> n3Statements) {
		EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
		
		//Substitute URIs and literals from form
		n3Statements = n3Subber.subInMultiUris(submission.getUrisFromForm(), n3Statements);
		n3Statements = n3Subber.subInMultiLiterals(submission.getLiteralsFromForm(), n3Statements);
		//Substitute URIS and literals in scope
		n3Statements = n3Subber.subInMultiUris(configuration.getUrisInScope(), n3Statements);
		n3Statements = n3Subber.subInMultiLiterals(configuration.getLiteralsInScope(), n3Statements);
		//for new resource
		setVarToNewResource(configuration, newURIMaker);
		//n3Statements = n3Subber.subInMultiUris(varToNewResource, n3Statements);
		return n3Statements;
	}
	
    
    //substitute uris and literals and also handle new resource
	private static Map<String, List<String>> subUrisAndLiteralsInFieldAssertions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			NewURIMaker newURIMaker, Map<String, List<String>> fieldAssertions) {
		//Substitute URIs and literals from form and from scope
		//fieldAssertions = subUrisAndLiteralsForField(configuration, submission, newURIMaker, fieldAssertions);
		fieldAssertions = subNewResourceForField(configuration, newURIMaker, fieldAssertions);
		return fieldAssertions;
	}
  
    //TODO: get rid of this as it does nothing new or interesting
	private static Map<String, List<String>> subUrisAndLiteralsInFieldRetractions(
			EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission,
			NewURIMaker newURIMaker, Map<String, List<String>> fieldRetractions) {
		return subUrisAndLiteralsForField(configuration, submission, fieldRetractions);
	}

    private static Map<String, String> getSubPedObjVarMap(
            EditConfigurationVTwo configuration) 
    {
        Map<String,String> varToValue = new HashMap<String,String>();
        
        String varNameForSub = configuration.getVarNameForSubject();
        if( varNameForSub != null && ! varNameForSub.isEmpty()){            
            varToValue.put( varNameForSub,configuration.getSubjectUri());                 
        }else{
            log.debug("no varNameForSubject found in configuration");
        }
        
        String varNameForPred = configuration.getVarNameForPredicate();
        if( varNameForPred != null && ! varNameForPred.isEmpty()){            
            varToValue.put( varNameForPred,configuration.getPredicateUri());
        }else{
            log.debug("no varNameForPredicate found in configuration");
        }
        
        String varNameForObj = configuration.getVarNameForObject();
        if( varNameForObj != null && ! varNameForObj.isEmpty()){            
            varToValue.put( varNameForObj, configuration.getObject());
        }else{
            log.debug("no varNameForObject found in configuration");
        }        
        
        return varToValue;        
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

   private static void applyEditSubmissionPreprocessors(
            EditConfigurationVTwo configuration, MultiValueEditSubmission submission) {
        List<EditSubmissionVTwoPreprocessor> preprocessors = configuration.getEditSubmissionPreprocessors();
        if(preprocessors != null) {
            for(EditSubmissionVTwoPreprocessor p: preprocessors) {
                p.preprocess(submission);
            }
        }
    }

   //this works differently based on whether this is object property editing or data property editing
   //TODO: bdc34: Why would this work differently for data prop or obj prop?
   
   //Object prop version below
   //Also updating to allow an array to be returned with the uri instead of a single uri
   //Note this would require more analysis in context of multiple uris possible for a field
   public static Map<String,String> URIsForNewRsources(
           EditConfigurationVTwo configuration, NewURIMaker newURIMaker) 
           throws InsertException {       
       Map<String,String> newResources = configuration.getNewResources();
       
       HashMap<String,String> varToNewURIs = new HashMap<String,String>();       
       for (String key : newResources.keySet()) {
           String prefix = newResources.get(key);
           String uri = newURIMaker.getUnusedNewURI(prefix);                        
           varToNewURIs.put(key, uri);  
       }   
       log.debug( "URIs for new resources: " + varToNewURIs );
       return varToNewURIs;
   }

   private static void logChanges(EditConfigurationVTwo configuration,
           AdditionsAndRetractions changes) {
       if( log.isDebugEnabled() )
           log.debug("Changes for edit " + configuration.getEditKey() + 
                   "\n" + changes.toString());
   }
   
   private static N3ParseType OPTIONAL = N3ParseType.OPTIONAL;
   private static N3ParseType REQUIRED = N3ParseType.REQUIRED;
   
   private enum N3ParseType {
       /* indicates that the n3 is optional and that a parse error should not 
        * throw an exception */         
       OPTIONAL,  
       /* indicates that the N3 is required and that a parse error should 
        * stop the processing and throw an exception. */
       REQUIRED 
   };
   
   static Random random = new Random();
   private static Log log = LogFactory.getLog(ProcessRdfForm.class);
}
