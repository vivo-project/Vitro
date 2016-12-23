/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.XMLChar;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProfile;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsUser;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;

public class N3EditUtils {
    final static Log log = LogFactory.getLog(N3EditUtils.class);

    
    /**
     * Execute any modelChangePreprocessors in the editConfiguration; 
     */
    public static void preprocessModels(
            AdditionsAndRetractions changes, 
            EditConfigurationVTwo editConfiguration, 
            VitroRequest request){

        List<ModelChangePreprocessor> modelChangePreprocessors = editConfiguration.getModelChangePreprocessors();
        //Check if there is a default set of preprocessors for the whole application
        List<ModelChangePreprocessor> defaultPreprocessors = getDefaultModelChangePreprocessors(request, ModelAccess.on(request).getOntModel(DISPLAY));
        if(modelChangePreprocessors != null) {
        	//if preprocessors exist for the configuration, add default preprocessors to the end
        	modelChangePreprocessors.addAll(defaultPreprocessors);
        } else {
        	//if configuration specific preprocessors are null, use default preprocessors instead
        	modelChangePreprocessors = defaultPreprocessors;
        }
        
       if(modelChangePreprocessors != null) {
            for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
                //these work by side effect
                pp.preprocess( changes.getRetractions(), changes.getAdditions(), request );
            }
       }                   
    }
    
    /**
     * Find which default model preprocessors are associated with the application.  These will 
     * be run everytime an edit/addition occurs, i.e. whenever the preprocessModels method is called. 
     */
    
    public static List<ModelChangePreprocessor> getDefaultModelChangePreprocessors(VitroRequest vreq, Model displayModel) {
    	List<ModelChangePreprocessor> preprocessors = new ArrayList<ModelChangePreprocessor>();
    	
    	//From the display model, find which preprocessors have been declared
    	String preprocessorOwlClass = "java:edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor";
    	String prefixes =        "PREFIX rdf:   <" + VitroVocabulary.RDF +"> \n" +
    	        "PREFIX rdfs:  <" + VitroVocabulary.RDFS +"> \n" + 
    	        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
    	        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n";
    	String query = prefixes + 
                "SELECT ?modelChangePreprocessor  WHERE { ?modelChangePreprocessor a <" + preprocessorOwlClass + "> . }";
        Query preprocessorQuery = QueryFactory.create(query);
        displayModel.enterCriticalSection(false);
           try{
               QueryExecution qexec = QueryExecutionFactory.create(preprocessorQuery,displayModel );
               try{                                                    
                   ResultSet results = qexec.execSelect();                
                   while (results.hasNext()) {
                       QuerySolution soln = results.nextSolution();
                       Resource modelChangePreprocessor = soln.getResource("modelChangePreprocessor");
                       if( modelChangePreprocessor != null && modelChangePreprocessor.getURI() != null){
                          String preprocessorClass = modelChangePreprocessor.getURI();
                          //Get rid of the "java:"
                          try {
                        	  ModelChangePreprocessor p = preprocessorForURI(vreq, displayModel, preprocessorClass);
                        	  if(p != null) {
                        		  preprocessors.add(p);
                        	  }
                          
                          } catch(Exception ex) {
                        	  log.error("Retrieving model change preprocessor resulted in an error", ex);
                          }
                       }
                   }
               }finally{ qexec.close(); }
           }finally{ displayModel.leaveCriticalSection(); }
                   
   	
    	return preprocessors;
    }
    
    //Copied this from DataGetterUtils - will need to refactor to put all of this in one place
    /**
     * Returns a DataGetter using information in the 
     * displayModel for the individual with the URI given by dataGetterURI
     * to configure it. 
     * 
     * May return null.
     * This should not throw an exception if the URI exists and has a type
     * that does not implement the DataGetter interface.
     */
    public static ModelChangePreprocessor preprocessorForURI(VitroRequest vreq, Model displayModel, String preprocessorURI) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException 
    {
        //get java class for dataGetterURI
    	
        String preprocessorClassName = DataGetterUtils.getClassNameFromUri(preprocessorURI);
        
        //figure out if it implements interface DataGetter
        Class<?> clz = Class.forName(preprocessorClassName);
        if( ! ModelChangePreprocessor.class.isAssignableFrom(clz) ){
    		log.debug("Class doesn't implement ModelChangePreprocessor: '" + preprocessorClassName + "'");
            return null;
        }
        
       //We should get the arguments from the constructor from the display model as well
        //So we don't need to check or constrain what can be passed here
       //Right now, this supports a preprocessor with no arguments
        //TO DO: Start populating with potential arguments based on n3 itself
        /*
        Object[] argList =  new Object[]{};
       	for (Constructor<?> ct: clz.getConstructors()) {
       		return (ModelChangePreprocessor) ct.newInstance(argList);
        }
        	*/
        return (ModelChangePreprocessor) clz.newInstance();
    }

    /** 
     * Process Entity to Return to - substituting uris etc. 
     * May return null.  */
    public static String processEntityToReturnTo(
            EditConfigurationVTwo configuration, 
            MultiValueEditSubmission submission, 
            VitroRequest vreq) {      
        String returnTo = null;
        
        //usually the submission should have a returnTo that is
        // already substituted in with values during ProcessRdfForm.process()
        if( submission != null && submission.getEntityToReturnTo() != null 
                && !submission.getEntityToReturnTo().trim().isEmpty()){
            returnTo = submission.getEntityToReturnTo();            
        }else{
            //If submission doesn't have it, do the best that we can do.
            //this will not have the new resource URIs.
            List<String> entityToReturnTo = new ArrayList<String>();
            String entity = configuration.getEntityToReturnTo();
            entityToReturnTo.add(entity);
            EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
        
            //Substitute URIs and literals from form
            n3Subber.subInMultiUris(submission.getUrisFromForm(), entityToReturnTo);
            n3Subber.subInMultiLiterals(submission.getLiteralsFromForm(), entityToReturnTo);
        
            //TODO: this won't work to get new resoruce URIs,
            //must the same new resources as in ProcessRdfForm.process
            //setVarToNewResource(configuration, vreq);
            //entityToReturnTo = n3Subber.subInMultiUris(varToNewResource, entityToReturnTo);
            
            returnTo = entityToReturnTo.get(0);
        }

        //remove brackets from sub in of URIs 
        if(returnTo != null) {            
            returnTo = returnTo.trim().replaceAll("<","").replaceAll(">","");       
        }
        return returnTo;
    }
    
    /**
     * If the edit was a data property statement edit, then this updates the EditConfiguration to
     * be an edit of the new post-edit statement.  This allows a back button to the form to get the
     * edit key and be associated with the new edit state.
     * TODO: move this to utils
     */
    public static void updateEditConfigurationForBackButton(
            EditConfigurationVTwo editConfig,
            MultiValueEditSubmission submission, 
            VitroRequest vreq, 
            Model writeModel) {
        
        //now setup an EditConfiguration so a single back button submissions can be handled
        //Do this if data property
        if(EditConfigurationUtils.isDataProperty(editConfig.getPredicateUri(), vreq)) {
            EditConfigurationVTwo copy = editConfig.copy();
            
            //need a new DataPropHash and a new editConfig that uses that, and replace 
            //the editConfig used for this submission in the session.  The same thing
            //is done for an update or a new insert since it will convert the insert
            //EditConfig into an update EditConfig.                       
             
            DataPropertyStatement dps = new DataPropertyStatementImpl();
            List<Literal> submitted = submission.getLiteralsFromForm().get(copy.getVarNameForObject());
            if( submitted != null && submitted.size() > 0){
                for(Literal submittedLiteral: submitted) {
                    dps.setIndividualURI( copy.getSubjectUri() );
                    dps.setDatapropURI( copy.getPredicateUri() );
                    dps.setDatatypeURI( submittedLiteral.getDatatypeURI());
                    dps.setLanguage( submittedLiteral.getLanguage() );
                    dps.setData( submittedLiteral.getLexicalForm() );
                   
                    copy.setDatapropKey( RdfLiteralHash.makeRdfLiteralHash(dps) );                    
                    copy.prepareForDataPropUpdate(writeModel, vreq.getWebappDaoFactory().getDataPropertyDao());                    
                }
                EditConfigurationVTwo.putConfigInSession(copy,vreq.getSession());
            }
        }
        
    }


    /** Several places could give an editor URI. Return the first one. */
    public static String getEditorUri(HttpServletRequest request) {
        IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(request);
    
        List<String> uris = new ArrayList<String>();
        uris.addAll(IsUser.getUserUris(ids));
        uris.addAll(HasProfile.getProfileUris(ids));
        uris.add("Unknown N3 Editor");
        return uris.get(0);
    }


    /**
     * Strips from a string any characters that are not valid in XML 1.0
     * @param in String to strip characters from
     */
    public static String stripInvalidXMLChars(String in) {
        if (in == null) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (!XMLChar.isInvalid(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }    
    
   
}
