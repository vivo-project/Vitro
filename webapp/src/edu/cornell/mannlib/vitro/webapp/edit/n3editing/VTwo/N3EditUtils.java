/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.ModelChangePreprocessor;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

public class N3EditUtils {

    
    /**
     * Execute any modelChangePreprocessors in the editConfiguration; 
     */
    public static void preprocessModels(
            AdditionsAndRetractions changes, 
            EditConfigurationVTwo editConfiguration, 
            VitroRequest request){

        List<ModelChangePreprocessor> modelChangePreprocessors = editConfiguration.getModelChangePreprocessors();
        if ( modelChangePreprocessors != null ) {
            for ( ModelChangePreprocessor pp : modelChangePreprocessors ) {
                //these work by side effect
                pp.preprocess( changes.getRetractions(), changes.getAdditions(), request );
            }
        }                   
    }
    


    /** Process Entity to Return to - substituting uris etc. */
    //TODO: move this to utils or contorller
    public static String processEntityToReturnTo(EditConfigurationVTwo configuration, 
            MultiValueEditSubmission submission, VitroRequest vreq) {
        List<String> entityToReturnTo = new ArrayList<String>();
        String entity = configuration.getEntityToReturnTo();
        entityToReturnTo.add(entity);
        //Substitute uris and literals on form
        //Substitute uris and literals in scope
        //Substite var to new resource
        EditN3GeneratorVTwo n3Subber = configuration.getN3Generator();
        
        //Substitute URIs and literals from form
        n3Subber.subInMultiUris(submission.getUrisFromForm(), entityToReturnTo);
        n3Subber.subInMultiLiterals(submission.getLiteralsFromForm(), entityToReturnTo);
        
        //TODO: this won't work, must the same new resources as in ProcessRdfForm.process
        //setVarToNewResource(configuration, vreq);
        //entityToReturnTo = n3Subber.subInMultiUris(varToNewResource, entityToReturnTo);
        
        String processedEntity = entityToReturnTo.get(0);
        if(processedEntity != null) {
            
            processedEntity = processedEntity.trim().replaceAll("<","").replaceAll(">","");       
        }
        return processedEntity;

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
            OntModel writeModel) {
        
        //now setup an EditConfiguration so a single back button submissions can be handled
        //Do this if data property
        if(EditConfigurationUtils.isDataProperty(editConfig.getPredicateUri(), vreq)) {
            EditConfigurationVTwo copy = editConfig.copy();
            
            //need a new DataPropHash and a new editConfig that uses that, and replace 
            //the editConfig used for this submission in the session.  The same thing
            //is done for an update or a new insert since it will convert the insert
            //EditConfig into an update EditConfig.            
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
