package edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;

public class ProcessRdfForm {
    
    private static Log log = LogFactory.getLog(ProcessRdfForm.class);    
    
    /**
     * Execute any modelChangePreprocessors in the editConfiguration;
     * 
     */
    public static void preprocessModels(AdditionsAndRetractions changes, EditConfiguration editConfiguration, VitroRequest request){

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


    @SuppressWarnings("static-access")
    public static AdditionsAndRetractions createNewResource(EditConfiguration editConfiguration , EditSubmission submission){
        List<String> errorMessages = new ArrayList<String>();
        
        EditN3Generator n3Subber = editConfiguration.getN3Generator();
        
        if(log.isDebugEnabled()){
            log.debug("creating a new relation " + editConfiguration.getPredicateUri());
        }
        
        //handle creation of a new object property and maybe a resource
        List<String> n3Required = editConfiguration.getN3Required();
        List<String> n3Optional = editConfiguration.getN3Optional();
        
        /* ********** URIs and Literals on Form/Parameters *********** */
        //sub in resource uris off form
        n3Required = n3Subber.subInUris(submission.getUrisFromForm(), n3Required);
        n3Optional = n3Subber.subInUris(submission.getUrisFromForm(), n3Optional);      
        if(log.isDebugEnabled()) {
            logRequiredOpt("substituted in URIs  off from ",n3Required,n3Optional);
        }
        
        //sub in literals from form
        n3Required = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Required);
        n3Optional = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Optional);
        if(log.isDebugEnabled()) {
            logRequiredOpt("substituted in literals off from ",n3Required,n3Optional);
        }
        
        /* ****************** URIs and Literals in Scope ************** */        
        n3Required = n3Subber.subInUris( editConfiguration.getUrisInScope(), n3Required);
        n3Optional = n3Subber.subInUris( editConfiguration.getUrisInScope(), n3Optional);
        if(log.isDebugEnabled()) {
            logRequiredOpt("substituted in URIs from scope ",n3Required,n3Optional);
        }
        
        n3Required = n3Subber.subInLiterals( editConfiguration.getLiteralsInScope(), n3Required);
        n3Optional = n3Subber.subInLiterals( editConfiguration.getLiteralsInScope(), n3Optional);
        if(log.isDebugEnabled()) {
            logRequiredOpt("substituted in Literals from scope ",n3Required,n3Optional);
        }
        
        //deal with required N3
        List<Model> requiredNewModels = new ArrayList<Model>();
        for(String n3 : n3Required){
            try{
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                requiredNewModels.add(model);
            } catch(Throwable t){
                errorMessages.add("error processing required n3 string \n" + t.getMessage() + '\n' + "n3: \n" + n3);
            }
        }
        
        if(!errorMessages.isEmpty()){
            String error = "problems processing required n3: \n";
            for(String errorMsg: errorMessages){
                error += errorMsg + '\n';
            }
            if(log.isDebugEnabled()){
                log.debug(error);
            }        
        }
        List<Model> requiredAssertions = requiredNewModels;        
        
        //deal with optional N3
        List<Model> optionalNewModels = new ArrayList<Model>();
        for(String n3 : n3Optional){
            try{
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                optionalNewModels.add(model);
            }catch(Throwable t){
                //if an optional N3 block fails to parse it will be ignored
                //this is what is meant by optional.
            }
        }
        requiredAssertions.addAll( optionalNewModels );
        
        return getMinimalChanges(new AdditionsAndRetractions(requiredAssertions, Collections.<Model>emptyList()));
    }

    @SuppressWarnings("static-access")
    public static AdditionsAndRetractions editExistingResource(EditConfiguration editConfiguration, EditSubmission submission) {
        
        Map<String, List<String>> fieldAssertions = Utilities.fieldsToAssertionMap(editConfiguration.getFields());
        Map<String, List<String>> fieldRetractions = Utilities.fieldsToRetractionMap(editConfiguration.getFields());
        EditN3Generator n3Subber = editConfiguration.getN3Generator();

        /* ********** URIs and Literals on Form/Parameters *********** */
        fieldAssertions = n3Subber.substituteIntoValues(submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions);
        if(log.isDebugEnabled()) {
            logAddRetract("substituted in literals from form",fieldAssertions,fieldRetractions);
        }        
        
        /* ****************** URIs and Literals in Scope ************** */
        fieldAssertions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldAssertions );
        fieldRetractions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldRetractions);
        if(log.isDebugEnabled()) {
            logAddRetract("substituted in URIs and Literals from scope",fieldAssertions,fieldRetractions);
        }        
        
        //do edits ever need new resources? (YES)
/*        Map<String,String> varToNewResource = newToUriMap(editConfiguration.getNewResources(),wdf);
        fieldAssertions = n3Subber.substituteIntoValues(varToNewResource, null, fieldAssertions);
        if(log.isDebugEnabled()) {
            Utilities.logAddRetract("substituted in URIs for new resources",fieldAssertions,fieldRetractions);
        }
        entToReturnTo = n3Subber.subInUris(varToNewResource,entToReturnTo);
*/        
        //editing an existing statement
        List<Model> requiredFieldAssertions  = new ArrayList<Model>();
        List<Model> requiredFieldRetractions = new ArrayList<Model>();
        
        List<String> errorMessages = new ArrayList<String>();
        
        for(String fieldName : fieldAssertions.keySet()){                   
            List<String> assertions = fieldAssertions.get(fieldName);
            List<String> retractions = fieldRetractions.get(fieldName);
            
            for(String n3: assertions){
                try{
                    Model model = ModelFactory.createDefaultModel();
                    StringReader reader = new StringReader(n3);
                    model.read(reader, "", "N3");
                }catch(Throwable t){
                    String errMsg = "error processing N3 assertion string from field " + fieldName + "\n" +
                    t.getMessage() + '\n' + "n3: \n" + n3;
                    errorMessages.add(errMsg);
                    if(log.isDebugEnabled()){
                        log.debug(errMsg);
                    }
                }
            }
            
            for(String n3 : retractions){
                try{
                    Model model = ModelFactory.createDefaultModel();
                    StringReader reader = new StringReader(n3);
                    model.read(reader, "", "N3");
                    requiredFieldRetractions.add(model);
                }catch(Throwable t){
                    String errMsg = "error processing N3 retraction string from field " + fieldName + "\n"+
                    t.getMessage() + '\n' + "n3: \n" + n3;
                    errorMessages.add(errMsg);
                    if(log.isDebugEnabled()){
                        log.debug(errMsg);
                    }
                }
            }        
        }
        
        return getMinimalChanges(new AdditionsAndRetractions(requiredFieldAssertions, requiredFieldRetractions));
    }
    

    /**
     * This is intended to substitute vars from the EditConfiguration and
     * EditSubmission into the URL to return to.
     */
    public static String substitueForURL(EditConfiguration configuration, EditSubmission submission){
        
        List<String> entToReturnTo = new ArrayList<String>(1);
        entToReturnTo.add(configuration.getEntityToReturnTo());
        
        EditN3Generator n3Subber = configuration.getN3Generator();
        // Substitute in URIs from the submission
        entToReturnTo = n3Subber.subInUris(submission.getUrisFromForm(), entToReturnTo);                       
        
        // Substitute in URIs from the scope of the EditConfiguration                
        entToReturnTo = n3Subber.subInUris(configuration.getUrisInScope(), entToReturnTo);                
        
        //The problem is that subInURI will add < and > around URIs for the N3 syntax.
        //for the URL to return to, replace < and > from URI additions.  
        return entToReturnTo.get(0).trim().replaceAll("<","").replaceAll(">","");        
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
}
