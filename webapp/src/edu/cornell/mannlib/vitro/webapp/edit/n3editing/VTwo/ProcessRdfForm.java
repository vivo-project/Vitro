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
import com.hp.hpl.jena.rdf.model.Literal;
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
       
    private NewURIMaker newURIMaker;
    private EditN3GeneratorVTwo populator;
    
    private Map<String,String> urisForNewResources = null; 
    
    /**
     * Construct the ProcessRdfForm object. 
     */
    public ProcessRdfForm( EditConfigurationVTwo config, NewURIMaker newURIMaker){
        this.newURIMaker = newURIMaker;
        this.populator = config.getN3Generator();
    }
    
    /**
     * This detects if this is an edit of an existing statement or an edit
     * to create a new statement or set of statements. Then the correct
     * method will be called to convert the EditConfiguration and EditSubmission
     * into a set of additions and retractions.
     * 
     * This will handle data property editing, object property editing 
     * and general editing.
     * 
     * The submission object will be modified to have its entityToReturnTo string
     * substituted with the values from the processing.
     * 
     * @throws Exception May throw an exception if Required N3 does not
     * parse correctly.
     */
    public AdditionsAndRetractions  process(
            EditConfigurationVTwo configuration,
            MultiValueEditSubmission submission) 
    throws Exception{  
        log.debug("configuration:\n" + configuration.toString());
        log.debug("submission:\n" + submission.toString());
        
        applyEditSubmissionPreprocessors( configuration, submission );
        
        AdditionsAndRetractions changes;
        if( configuration.isUpdate() ){
            changes = editExistingStatements(configuration, submission);
        } else {
            changes = createNewStatements(configuration, submission );
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
    private AdditionsAndRetractions createNewStatements(
            EditConfigurationVTwo configuration, 
            MultiValueEditSubmission submission) throws Exception {                
        log.debug("in createNewStatements()" );        
        
        //getN3Required and getN3Optional will return copies of the 
        //N3 String Lists so that this code will not modify the originals.
        List<String> requiredN3 = configuration.getN3Required();
        List<String> optionalN3 = configuration.getN3Optional();                        
        
        /* substitute in the form values and existing values */
        subInValuesToN3( configuration, submission, requiredN3, optionalN3, null , null);
                   
        /* parse N3 to RDF Models, No retractions since all of the statements are new. */         
        return parseN3ToChange(requiredN3, optionalN3, null, null);        
    }

    /* for a list of N3 strings, substitute in the subject, predicate and object URIs 
     * from the EditConfiguration. */
    protected void substituteInSubPredObjURIs(
            EditConfigurationVTwo configuration,
            List<String>... n3StrLists){                
        Map<String, String> valueMap = getSubPedObjVarMap(configuration);
        for (List<String> n3s : n3StrLists) {
            populator.subInUris(valueMap, n3s);
        }
    }

    /**
     * Process an EditConfiguration to edit a set of existing statements.
     * 
     * This will handle data property editing, object property editing and
     * general editing.
     * 
     * No longer checking if field has changed, because assertions and
     * retractions are mutually diff'ed before statements are added to or
     * removed from the model. The explicit change check can cause problems in
     * more complex setups, like the automatic form building in DataStaR.
     */
    protected AdditionsAndRetractions editExistingStatements(
            EditConfigurationVTwo editConfig,
            MultiValueEditSubmission submission) throws Exception {

        log.debug("editing an existing resource: " + editConfig.getObject() );        

        //getN3Required and getN3Optional will return copies of the 
        //N3 String Lists so that this code will not modify the originals.
        List<String> N3RequiredAssert = editConfig.getN3Required();        
        List<String> N3OptionalAssert = editConfig.getN3Optional();
        List<String> N3RequiredRetract = editConfig.getN3Required();        
        List<String> N3OptionalRetract = editConfig.getN3Optional();

        subInValuesToN3(editConfig, submission, 
                N3RequiredAssert, N3OptionalAssert, 
                N3RequiredRetract, N3OptionalRetract);
                                  
        return parseN3ToChange( 
                N3RequiredAssert,N3OptionalAssert, 
                N3RequiredRetract, N3OptionalRetract);        
    }    
    
    @SuppressWarnings("unchecked")
    protected void subInValuesToN3(
            EditConfigurationVTwo editConfig, MultiValueEditSubmission submission, 
            List<String> requiredAsserts, List<String> optionalAsserts,
            List<String> requiredRetracts, List<String> optionalRetracts ) throws InsertException{
        
        //need to substitute into the return to URL becase it may need new resource URIs
        List<String> URLToReturnTo = Arrays.asList(submission.getEntityToReturnTo());
                
        /* ********** Form submission URIs ********* */
        substituteInMultiURIs(submission.getUrisFromForm(), requiredAsserts, optionalAsserts, URLToReturnTo);
        logSubstitue( "Added form URIs", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        //Retractions does NOT get values from form.
        
        /* ******** Form submission Literals *********** */
        substituteInMultiLiterals( submission.getLiteralsFromForm(), requiredAsserts, optionalAsserts, URLToReturnTo);
        logSubstitue( "Added form Literals", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        //Retractions does NOT get values from form.                               
        
        /* *********** Add subject, object and predicate ******** */
        substituteInSubPredObjURIs(editConfig, requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts, URLToReturnTo);
        logSubstitue( "Added sub, pred and obj URIs", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        
        /* ********* Existing URIs and Literals ********** */
        substituteInMultiURIs(editConfig.getUrisInScope(), 
                requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts, URLToReturnTo);
        logSubstitue( "Added existing URIs", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        
        substituteInMultiLiterals(editConfig.getLiteralsInScope(), 
                requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts, URLToReturnTo);
        logSubstitue( "Added existing Literals", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        //Both Assertions and Retractions get existing values.
        
        /* ************  Edits may need new resources *********** */
        urisForNewResources = URIsForNewRsources(editConfig, newURIMaker);
        substituteInURIs( urisForNewResources, requiredAsserts, optionalAsserts, URLToReturnTo);
        logSubstitue( "Added URIs for new Resources", requiredAsserts, optionalAsserts, requiredRetracts, optionalRetracts);
        // Only Assertions get new resources.       
        
        submission.setEntityToReturnTo(URLToReturnTo.get(0));
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
        
    protected AdditionsAndRetractions parseN3ToChange( 
            List<String> requiredAdds, List<String> optionalAdds,
            List<String> requiredDels, List<String> optionalDels) throws Exception{
        
        List<Model> adds = parseN3ToRDF(requiredAdds, REQUIRED);
        adds.addAll( parseN3ToRDF(optionalAdds, OPTIONAL));
        
        List<Model> retracts = new ArrayList<Model>();
        if( requiredDels != null && optionalDels != null ){
            retracts.addAll( parseN3ToRDF(requiredDels, REQUIRED) );
            retracts.addAll( parseN3ToRDF(optionalDels, OPTIONAL) );
        }
        
        return new AdditionsAndRetractions(adds,retracts);
    }
    
    /**
     * Parse the n3Strings to a List of RDF Model objects.
     * 
     * @param n3Strings
     * @param parseType if OPTIONAL, then don't throw exceptions on errors
     * If REQUIRED, then throw exceptions on errors.
     * @throws Exception 
     */
    protected static List<Model> parseN3ToRDF(
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
		    
    protected void logSubstitue(String msg, List<String> requiredAsserts,
            List<String> optionalAsserts, List<String> requiredRetracts,
            List<String> optionalRetracts) {
        if( !log.isDebugEnabled() ) return;
        log.debug(msg);
        logSubstitueN3( msg, requiredAsserts, "required assertions");
        logSubstitueN3( msg, optionalAsserts, "optional assertions");
        logSubstitueN3( msg, requiredRetracts, "required retractions");
        logSubstitueN3( msg, optionalRetracts, "optional retractions");        
    }
    
    private void logSubstitueN3(String msg, List<String> n3, String label){
        if( n3 == null || n3.size() == 0) return;        
        String out = label + ":\n";
        for( String str : n3 ){
            out += "    " + str + "\n";
        }                      
        log.debug(out);
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
        if( varNameForObj != null 
        		&& ! varNameForObj.isEmpty()){            
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

   private void applyEditSubmissionPreprocessors(
            EditConfigurationVTwo configuration, MultiValueEditSubmission submission) {
        List<EditSubmissionVTwoPreprocessor> preprocessors = configuration.getEditSubmissionPreprocessors();
        if(preprocessors != null) {
            for(EditSubmissionVTwoPreprocessor p: preprocessors) {
                p.preprocess(submission);
            }
        }
    }

   
   //Note this would require more analysis in context of multiple URIs
   public Map<String,String> URIsForNewRsources(
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

   private void substituteInMultiLiterals(
           Map<String, List<Literal>> literalsFromForm,
           List<String> ... n3StrLists) {
       
       for( List<String> n3s : n3StrLists){
           populator.subInMultiLiterals(literalsFromForm, n3s);
       }
   }

   private  void substituteInMultiURIs(
           Map<String, List<String>> multiUris, List<String> ... n3StrLists) {
       
       for( List<String> n3s : n3StrLists){
           if( n3s != null ){
               populator.subInMultiUris(multiUris, n3s);
           }
       }                
   }

   private void substituteInURIs(
           Map<String, String> uris, List<String> ... n3StrLists) {        
       for( List<String> n3s : n3StrLists){
           if( n3s != null ){
               populator.subInUris(uris, n3s);
           }
       }                
   }    
      
   private static Log log = LogFactory.getLog(ProcessRdfForm.class);
}