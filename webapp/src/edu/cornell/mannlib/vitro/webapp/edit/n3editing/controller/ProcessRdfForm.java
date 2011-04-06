/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ForwardResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Generator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.ModelChangePreprocessor;

/**
 * This servlet will process EditConfigurations with query parameters
 * to perform an edit.
 *  
 */
public class ProcessRdfForm extends FreemarkerHttpServlet{
	
    private Log log = LogFactory.getLog(ProcessRdfForm.class);
    	
	//bdc34: this is likely to become a servlet instead of a jsp.
	// You can get a reference to the servlet from the context.
	// this will need to be converted from a jsp to something else
	public static final String POST_EDIT_CLEANUP_JSP = "postEditCleanUp.jsp";	   	    
    	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
    ServletException, IOException{
	    doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
									ServletException, IOException{		
		VitroRequest vreq = new VitroRequest(request);
		
		//get the EditConfiguration 
		EditConfiguration configuration = getEditConfiguration(request);
        if(configuration == null){
            doEditConfigNotFound( vreq, response);            
            return;
        }

        //get the EditSubmission
        EditSubmission submission = new EditSubmission(vreq.getParameterMap(), configuration);        	
		EditSubmission.putEditSubmissionInSession(request.getSession(), submission);

		boolean hasErrors = processValidationErrors(vreq, configuration, submission, response);
		if( hasErrors)
		    return; //processValidationErrors() already forwarded to redisplay the form with validation messages 		

        // get the models to work with in case the write model and query model are not the defaults 
		OntModel queryModel = configuration.getQueryModelSelector().getModel(request, getServletContext());		
	    OntModel writeModel = configuration.getWriteModelSelector().getModel(request,getServletContext());  
	    
	    AdditionsAndRetractions changes;
		if(configuration.isUpdate()){
		    changes = editExistingResource(configuration, submission);
		}else{
		    changes = createNewResource(configuration, submission);
		}
		
		changes = getMinimalChanges( changes );
		
		if( configuration.isUseDependentResourceDelete() )
		    changes = addDependentDeletes(changes, queryModel);		
		
		preprocessModels(changes, configuration, vreq);
		
		applyChangesToWriteModel(changes, queryModel, writeModel, EditN3Utils.getEditorUri(vreq) );
		
		//Here we are trying to get the entity to return to URL, 
		//Maybe this should be in POST_EDIT_CLEANUP? 
        if( configuration.getEntityToReturnTo() != null ){      
            request.setAttribute("entityToReturnTo", substitueForURL( configuration, submission));                   
        }      
        
        doPostEdit(vreq,response);		
	}

    /**
     * Execute any modelChangePreprocessors in the editConfiguration;
     */
	protected void preprocessModels(AdditionsAndRetractions changes, EditConfiguration editConfiguration, VitroRequest request){

        List<ModelChangePreprocessor> modelChangePreprocessors = editConfiguration.getModelChangePreprocessors();
        if ( modelChangePreprocessors != null ) {
            for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
                //these work by side effect
                pp.preprocess( changes.getRetractions(), changes.getAdditions(), request );
            }
        }           	    
	}
		
	protected AdditionsAndRetractions getMinimalChanges( AdditionsAndRetractions changes ){
	    //make a model with all the assertions and a model with all the 
        //retractions, do a diff on those and then only add those to the jenaOntModel
        Model allPossibleAssertions = changes.getAdditions();
        Model allPossibleRetractions = changes.getRetractions();        
        
        //find the minimal change set
        Model assertions = allPossibleAssertions.difference( allPossibleRetractions );    
        Model retractions = allPossibleRetractions.difference( allPossibleAssertions );        
        return new AdditionsAndRetractions(assertions,retractions);
	}
	
	protected AdditionsAndRetractions addDependentDeletes( AdditionsAndRetractions changes, Model queryModel){
	    //Add retractions for dependent resource delete if that is configured and 
        //if there are any dependent resources.        	            
        Model depResRetractions = 
            DependentResourceDeleteJena
            .getDependentResourceDeleteForChange(changes.getAdditions(),changes.getRetractions(),queryModel);                

        changes.getRetractions().add(depResRetractions);        
        return changes; 
	}
	
    protected void applyChangesToWriteModel(AdditionsAndRetractions changes, OntModel queryModel, OntModel writeModel, String editorUri) {    	   		    	   	   
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

	private EditConfiguration getEditConfiguration(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		EditConfiguration editConfiguration = EditConfiguration.getConfigFromSession(session, request);
		
		return editConfiguration;
	}
	
	@SuppressWarnings("static-access")
	private AdditionsAndRetractions createNewResource(EditConfiguration editConfiguration , EditSubmission submission){
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
        	Utilities.logRequiredOpt("substituted in URIs  off from ",n3Required,n3Optional);
        }
        
        //sub in literals from form
        n3Required = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Required);
        n3Optional = n3Subber.subInLiterals(submission.getLiteralsFromForm(), n3Optional);
        if(log.isDebugEnabled()) {
        	Utilities.logRequiredOpt("substituted in literals off from ",n3Required,n3Optional);
        }
        
        /* ****************** URIs and Literals in Scope ************** */        
        n3Required = n3Subber.subInUris( editConfiguration.getUrisInScope(), n3Required);
        n3Optional = n3Subber.subInUris( editConfiguration.getUrisInScope(), n3Optional);
        if(log.isDebugEnabled()) {
        	Utilities.logRequiredOpt("substituted in URIs from scope ",n3Required,n3Optional);
        }
        
        n3Required = n3Subber.subInLiterals( editConfiguration.getLiteralsInScope(), n3Required);
        n3Optional = n3Subber.subInLiterals( editConfiguration.getLiteralsInScope(), n3Optional);
        if(log.isDebugEnabled()) {
        	Utilities.logRequiredOpt("substituted in Literals from scope ",n3Required,n3Optional);
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
        
        return new AdditionsAndRetractions(requiredAssertions, Collections.<Model>emptyList());
 	}

	@SuppressWarnings("static-access")
	private AdditionsAndRetractions editExistingResource(EditConfiguration editConfiguration, EditSubmission submission) {
		
		Map<String, List<String>> fieldAssertions = Utilities.fieldsToAssertionMap(editConfiguration.getFields());
		Map<String, List<String>> fieldRetractions = Utilities.fieldsToRetractionMap(editConfiguration.getFields());
		EditN3Generator n3Subber = editConfiguration.getN3Generator();

        /* ********** URIs and Literals on Form/Parameters *********** */
		fieldAssertions = n3Subber.substituteIntoValues(submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions);
        if(log.isDebugEnabled()) {
        	Utilities.logAddRetract("substituted in literals from form",fieldAssertions,fieldRetractions);
        }        
        
        /* ****************** URIs and Literals in Scope ************** */
        fieldAssertions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldAssertions );
        fieldRetractions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldRetractions);
        if(log.isDebugEnabled()) {
        	Utilities.logAddRetract("substituted in URIs and Literals from scope",fieldAssertions,fieldRetractions);
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
        
        return new AdditionsAndRetractions(requiredFieldAssertions, requiredFieldRetractions);
	}
	
    private void doEditConfigNotFound(VitroRequest request, HttpServletResponse response) {
        HashMap<String,Object>map = new HashMap<String,Object>();
        map.put("message", "No editing configuration found, cannot process edit.");
        ResponseValues values = new TemplateResponseValues("message.ftl", map);        
        try {
            doResponse(request,response,values);
        } catch (TemplateProcessingException e) {
            log.error("Could not process template for doEditConfigNotFound()",e);
        }        
    }

	private boolean processValidationErrors(VitroRequest vreq,
			EditConfiguration editConfiguration, EditSubmission submission,
			HttpServletResponse response) throws ServletException, IOException {
		
		Map<String, String> errors = submission.getValidationErrors();
		
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));			
						
	        RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(editConfiguration.getFormUrl());
	        requestDispatcher.forward(vreq, response);
	        return true;    
		}
		return false;		
	}

    private void doPostEdit(VitroRequest vreq, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(POST_EDIT_CLEANUP_JSP);
        requestDispatcher.forward(vreq, response);
    }
    
	
	
	public static class Utilities {
		
		private static Log log = LogFactory.getLog(ProcessRdfForm.class);
	    static Random random = new Random();
		
	    public static Map<String,List<String>> fieldsToAssertionMap( Map<String,Field> fields){
	        Map<String,List<String>> out = new HashMap<String,List<String>>();
	        for( String fieldName : fields.keySet()){
	            Field field = fields.get(fieldName);

	            List<String> copyOfN3 = new ArrayList<String>();
	            for( String str : field.getAssertions()){
	                copyOfN3.add(str);
	            }
	            out.put( fieldName, copyOfN3 );
	        }
	        return out;
	    }

	     public static Map<String,List<String>> fieldsToRetractionMap( Map<String,Field> fields){
	        Map<String,List<String>> out = new HashMap<String,List<String>>();
	        for( String fieldName : fields.keySet()){
	            Field field = fields.get(fieldName);

	            List<String> copyOfN3 = new ArrayList<String>();
	            for( String str : field.getRetractions()){
	                copyOfN3.add(str);
	            }
	            out.put( fieldName, copyOfN3 );
	        }
	        return out;
	    }	
	     
	     public static Map<String,String> newToUriMap(Map<String,String> newResources, WebappDaoFactory wdf){
	         HashMap<String,String> newVarsToUris = new HashMap<String,String>();
	         HashSet<String> newUris = new HashSet<String>();
	         for( String key : newResources.keySet()){        	
	             String prefix = newResources.get(key);
	         	String uri = makeNewUri(prefix, wdf);
	         	while( newUris.contains(uri) ){
	         		uri = makeNewUri(prefix,wdf);
	         	}
	         	newVarsToUris.put(key,uri);
	         	newUris.add(uri);
	         }
	          return newVarsToUris;
	     }

	     
	     public static String makeNewUri(String prefix, WebappDaoFactory wdf){
	         if( prefix == null || prefix.length() == 0 ){
	         	String uri = null;       
	         	try{
	         		uri = wdf.getIndividualDao().getUnusedURI(null);
	             }catch(InsertException ex){
	             	log.error("could not create uri");
	             }        
	 			return uri;
	         }
	         
	         String goodURI = null;
	         int attempts = 0;
	         while( goodURI == null && attempts < 30 ){            
	             Individual ind = new IndividualImpl();
	             ind.setURI( prefix + random.nextInt() );
	             try{
	         		goodURI = wdf.getIndividualDao().getUnusedURI(ind);
	             }catch(InsertException ex){
	             	log.debug("could not create uri");
	             }
	             attempts++;
	         }        
	         if( goodURI == null )
	         	log.error("could not create uri for prefix " + prefix);
	         return goodURI;
	     
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
		
	/**
	 * This is intended to substitute vars from the EditConfiguration and
	 * EditSubmission into the URL to return to.
	 */
	protected String substitueForURL(EditConfiguration configuration, EditSubmission submission){
	    
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
	
	/**
	 * This is a data structure to allow a method to return
	 * a pair of Model objects for additions and retractions.	 
	 */
	protected class AdditionsAndRetractions {
	    Model additions;
        Model retractions;
        
        public AdditionsAndRetractions(List<Model>adds, List<Model>retractions){
            Model allAdds = ModelFactory.createDefaultModel();
            Model allRetractions = ModelFactory.createDefaultModel();
            
            for( Model model : adds ) {
                allAdds.add( model );
            }
            for( Model model : retractions ){
                allRetractions.add( model );
            }
            
            this.setAdditions(allAdds);
            this.setRetractions(allRetractions);
        }
        
        public AdditionsAndRetractions(Model add, Model retract){
            this.additions = add;
            this.retractions = retract;
        }
        
	    public Model getAdditions() {
            return additions;
        }
        public void setAdditions(Model additions) {
            this.additions = additions;
        }
        public Model getRetractions() {
            return retractions;
        }
        public void setRetractions(Model retractions) {
            this.retractions = retractions;
        }        
	}
}
