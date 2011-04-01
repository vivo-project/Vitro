/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.forms;

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

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Generator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.ModelChangePreprocessor;

public class ProcessRdfForm extends VitroHttpServlet{
	
	public static final String NO_EDITCONFIG_FOUND_JSP = "/edit/messages/noEditConfigFound.jsp" ;
	public static final String POST_EDIT_CLEANUP_JSP = "postEditCleanUp.jsp";
	
	private Log log = LogFactory.getLog(ProcessRdfForm.class);
    
	//we should get this from the application
    static String defaultUriPrefix = "http://vivo.library.cornell.edu/ns/0.1#individual";    
    public static final String baseDirectoryForFiles = "/usr/local/vitrofiles";
    
    List<String> errorMessages = new ArrayList<String>();
	
    List<Model> requiredAssertions = null;
	List<Model> requiredRetractions = null;
	List<Model> optionalAssertions = null;
	
	/* entity to return to may be a variable */
	List<String> entToReturnTo = new ArrayList<String>(1);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
									ServletException, IOException{
		
		VitroRequest vreq = new VitroRequest(request);		
		EditConfiguration editConfiguration = processEditConfiguration(request, response);
				
		EditSubmission submission = getEditSubmission(request, editConfiguration);
		EditSubmission.putEditSubmissionInSession(request.getSession(), submission);

		processValidationErrors(vreq, editConfiguration, submission, response);
		
		boolean requestIsAnUpdate = editConfiguration.getObject() != null && editConfiguration.getObject().trim().length() > 0;
		
		OntModel queryModel = editConfiguration.getQueryModelSelector().getModel(request, getServletContext());
		OntModel resourcesModel = editConfiguration.getResourceModelSelector().getModel(request, getServletContext());
	    
		// get the model to write to here in case a preprocessor has switched the write layer
	    OntModel writeModel = editConfiguration.getWriteModelSelector().getModel(request,getServletContext());  
	    
		if(requestIsAnUpdate){
			editAnExistingResource(editConfiguration, submission);
		}else{
			createANewResource(editConfiguration, submission);
		}
		
		buildModel(editConfiguration, request, queryModel, writeModel);
		
		RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(POST_EDIT_CLEANUP_JSP);
		requestDispatcher.forward(vreq, response);
	}
	
	private void buildModel(EditConfiguration editConfiguration, HttpServletRequest request, OntModel queryModel, OntModel writeModel) {
		 
		
	    //The requiredNewModels and the optionalNewModels could be handled differently
	    //but for now we'll just do them the same
	    requiredAssertions.addAll(optionalAssertions);
	    
	    //make a model with all the assertions and a model with all the 
	    //retractions, do a diff on those and then only add those to the 
	    //jenaOntModel
	    Model allPossibleAssertions = ModelFactory.createDefaultModel();
	    Model allPossibleRetractions = ModelFactory.createDefaultModel();
	    
	    for( Model model : requiredAssertions ) {
	        allPossibleAssertions.add( model );
	    }
	    for( Model model : requiredRetractions ){
	        allPossibleRetractions.add( model );
	    }
	    
	    Model actualAssertions = allPossibleAssertions.difference( allPossibleRetractions );    
	    Model actualRetractions = allPossibleRetractions.difference( allPossibleAssertions );
	    
	    
	    if( editConfiguration.isUseDependentResourceDelete() ){
	    	Model depResRetractions = 
	    		DependentResourceDeleteJena
	    		.getDependentResourceDeleteForChange(actualAssertions,actualRetractions,queryModel);
	    	actualRetractions.add( depResRetractions );
	    }
	    
	    List<ModelChangePreprocessor> modelChangePreprocessors = editConfiguration.getModelChangePreprocessors();
	    if ( modelChangePreprocessors != null ) {
	        for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
	        	pp.preprocess( actualRetractions, actualAssertions, request );
	        }
	    }
	   
	    String editorUri = EditN3Utils.getEditorUri(new VitroRequest(request)); 
	    Lock lock = null;
	    try{
	        lock =  writeModel.getLock();
	        lock.enterCriticalSection(Lock.WRITE);
	        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,true));   
	        writeModel.add( actualAssertions );
	        writeModel.remove( actualRetractions );
	    }catch(Throwable t){
	        errorMessages.add("error adding edit change n3required model to in memory model \n"+ t.getMessage() );
	    }finally{
	        writeModel.getBaseModel().notifyEvent(new EditEvent(editorUri,false));
	        lock.leaveCriticalSection();
	    }
	    
	    if( entToReturnTo.size() >= 1 && entToReturnTo.get(0) != null){
	        request.setAttribute("entityToReturnTo",
	                entToReturnTo.get(0).trim().replaceAll("<","").replaceAll(">",""));
	    }

	    
	}

	private EditConfiguration getEditConfiguration(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		EditConfiguration editConfiguration = EditConfiguration.getConfigFromSession(session, request);
		
		return editConfiguration;
	}
	
	private EditSubmission getEditSubmission(HttpServletRequest request, EditConfiguration editConfiguration){
		
		VitroRequest vreq = new VitroRequest(request);
		EditSubmission submission = new EditSubmission(vreq.getParameterMap(), editConfiguration);
		
		return submission;
	}
	
	@SuppressWarnings("static-access")
	private void createANewResource(EditConfiguration editConfiguration , EditSubmission submission){
		
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
        entToReturnTo = n3Subber.subInUris(submission.getUrisFromForm(), entToReturnTo);
        
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
        entToReturnTo = n3Subber.subInUris(editConfiguration.getUrisInScope(), entToReturnTo);
        
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
        requiredAssertions = requiredNewModels;
        requiredRetractions = Collections.emptyList();
        
        //deal with optional N3
        List<Model> optionalNewModels = new ArrayList<Model>();
        for(String n3 : n3Optional){
            try{
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                optionalNewModels.add(model);
            }catch(Throwable t){
                errorMessages.add("error processing optional n3 string  \n"+
                        t.getMessage() + '\n' +
                        "n3: \n" + n3);
            }
        }
        optionalAssertions = optionalNewModels;            
 	}

	@SuppressWarnings("static-access")
	private void editAnExistingResource(EditConfiguration editConfiguration, EditSubmission submission) {
		
		Map<String, List<String>> fieldAssertions = Utilities.fieldsToAssertionMap(editConfiguration.getFields());
		Map<String, List<String>> fieldRetractions = Utilities.fieldsToRetractionMap(editConfiguration.getFields());
		EditN3Generator n3Subber = editConfiguration.getN3Generator();

		if(editConfiguration.getEntityToReturnTo() != null){
			entToReturnTo.add(" " + editConfiguration.getEntityToReturnTo() + " ");
		}
	
        /* ********** URIs and Literals on Form/Parameters *********** */
		fieldAssertions = n3Subber.substituteIntoValues(submission.getUrisFromForm(), submission.getLiteralsFromForm(), fieldAssertions);
        if(log.isDebugEnabled()) {
        	Utilities.logAddRetract("substituted in literals from form",fieldAssertions,fieldRetractions);
        }
        entToReturnTo = n3Subber.subInUris(submission.getUrisFromForm(),entToReturnTo); 
        
        /* ****************** URIs and Literals in Scope ************** */
        fieldAssertions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldAssertions );
        fieldRetractions = n3Subber.substituteIntoValues(editConfiguration.getUrisInScope(), editConfiguration.getLiteralsInScope(), fieldRetractions);
        if(log.isDebugEnabled()) {
        	Utilities.logAddRetract("substituted in URIs and Literals from scope",fieldAssertions,fieldRetractions);
        }
        entToReturnTo = n3Subber.subInUris(editConfiguration.getUrisInScope(),entToReturnTo);
        
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
        
        for(String fieldName : fieldAssertions.keySet()){
        	
        	if(true){
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
        }
        
        requiredAssertions = requiredFieldAssertions;
        requiredRetractions = requiredFieldRetractions;
        optionalAssertions = Collections.emptyList();

	}

	private void processValidationErrors(VitroRequest vreq,
			EditConfiguration editConfiguration, EditSubmission submission,
			HttpServletResponse response) throws ServletException, IOException {
		
		Map<String, String> errors = submission.getValidationErrors();
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));
		}
		
		RequestDispatcher requestDispatcher = vreq.getRequestDispatcher(editConfiguration.getFormUrl());
		requestDispatcher.forward(vreq, response);
		
		return;
	}

	private EditConfiguration processEditConfiguration(HttpServletRequest request, HttpServletResponse response)
							throws ServletException, IOException {
		
		EditConfiguration editConfiguration = getEditConfiguration(request);
		RequestDispatcher requestDispatcher;
		
		if(editConfiguration == null){
			requestDispatcher = request.getRequestDispatcher(NO_EDITCONFIG_FOUND_JSP);
			requestDispatcher.forward(request, response);
		}
		
		return editConfiguration;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
									ServletException, IOException{
		doGet(request, response);
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
	     
	     
	     /* What are the posibilities and what do they mean?
	     field is a Uri:
	      orgValue  formValue
	      null      null       Optional object property, maybe a un-filled out checkbox or radio button.
	      non-null  null       There was an object property and it was unset on the form
	      null      non-null   There was an objProp that was not set and is now set.
	      non-null  non-null    If they are the same then there was no edit, if the differ then form field was changed

	      field is a Literal:
	      orgValue  formValue
	      null      null      Optional value that was not set.
	      non-null  null      Optional value that was unset on form
	      null      non-null  Optional value that was unset but was set on form
	      non-null  non-null  If same, there was no edit, if different, there was a change to the form field.

	      What about checkboxes?
	    */
	    private boolean hasFieldChanged(String fieldName, EditConfiguration editConfig, EditSubmission submission) {
	        String orgValue = editConfig.getUrisInScope().get(fieldName);
	        String newValue = submission.getUrisFromForm().get(fieldName);
	               
	        // see possibilities list in comments just above
	        if (orgValue == null && newValue != null) {
	            log.debug("Note: Setting previously null object property for field '"+fieldName+"' to new value ["+newValue+"]");
	            return true;
	        }

	        if( orgValue != null && newValue != null){
	            if( orgValue.equals(newValue))
	              return false;
	            else
	              return true;
	        }
	       
	        //This does NOT use the semantics of the literal's Datatype or the language.
	        Literal orgLit = editConfig.getLiteralsInScope().get(fieldName);
	        Literal newLit = submission.getLiteralsFromForm().get(fieldName);
	        
	        if( orgLit != null ) {
	            orgValue = orgLit.getValue().toString();
	        } 
	        if( newLit != null ) {
	            newValue = newLit.getValue().toString();
	        }
	        
	        // added for links, where linkDisplayRank will frequently come in null
	        if (orgValue == null && newValue != null) {
	            return true;
	        }
	        
	        if( orgValue != null && newValue != null ){
	            if( orgValue.equals(newValue)) {
	            	return false;
	            }
	                
	            else {
	                return true;
	            }
	        }
	        //value wasn't set originally because the field is optional
	        return false;
	    }
	    
	    private void dump(String name, Object fff){
	        XStream xstream = new XStream(new DomDriver());
	        System.out.println( "*******************************************************************" );
	        System.out.println( name );
	        System.out.println(xstream.toXML( fff ));
	    } 
	     
	     
	}
}
