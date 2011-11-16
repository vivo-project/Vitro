/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.DefaultDataPropEmptyField;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

public class DefaultDataPropertyFormGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator {
	
	private static Log log = LogFactory.getLog(DefaultDataPropertyFormGenerator.class);

	static String literalVar =  "literal";                             
    static String literalPlaceholder = "?"+literalVar;
    
	@Override
	public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) {	    		
	    String command      = vreq.getParameter("cmd");	    	    	    
	    
	    String subjectUri   = vreq.getParameter("subjectUri");
        Individual subject = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(subjectUri);
        if( subject == null ) 
            throw new Error("In DefaultDataPropertyFormGenerator, could not find individual for URI " + subjectUri);
        
	    String predicateUri = vreq.getParameter("predicateUri");
	    WebappDaoFactory unfilteredWdf = vreq.getUnfilteredWebappDaoFactory();
	    DataProperty dataproperty = unfilteredWdf.getDataPropertyDao().getDataPropertyByURI( predicateUri );
	    if( dataproperty == null) {
	        // No dataproperty will be returned for rdfs:label, but we shouldn't throw an error.
	        // This is controlled by the Jena layer, so we can't change the behavior.
	        if (! predicateUri.equals(VitroVocabulary.LABEL)) {
	            log.error("Could not find data property '"+predicateUri+"' in model");
	            throw new Error("editDatapropStmtRequest.jsp: Could not find DataProperty in model: " + predicateUri);
	        }
	    }
	    
       String rangeDatatypeUri = dataproperty.getRangeDatatypeURI(); 
        if( rangeDatatypeUri == null || rangeDatatypeUri.trim().isEmpty() ){
            rangeDatatypeUri = vreq.getWebappDaoFactory().getDataPropertyDao().getRequiredDatatypeURI(subject, dataproperty);    
        }   
	        	    
	    EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
	    	
	    editConfiguration.setTemplate("defaultDataPropertyForm.ftl");
	    
    	editConfiguration.setN3Required(Arrays.asList( "?subject ?predicate " + literalPlaceholder + " . "));	    
    	    	    	
    	editConfiguration.setDatapropKey( EditConfigurationUtils.getDataHash(vreq) );
    	
    	editConfiguration.setVarNameForSubject("subject");
    	editConfiguration.setSubjectUri(subjectUri);
    	editConfiguration.setEntityToReturnTo( subjectUri );
    	
    	editConfiguration.setVarNameForPredicate("predicate");
    	editConfiguration.setPredicateUri(predicateUri);

    	editConfiguration.setVarNameForObject( literalVar );
    	
    	editConfiguration.setLiteralsOnForm( Arrays.asList( literalVar ));    	    	    
    	
    	editConfiguration.addField( new FieldVTwo()
    	       .setName( literalVar )
    	       .setPredicateUri(predicateUri)
    	       .setRangeDatatypeUri(rangeDatatypeUri));    	    
    	
    	//deal with empty field
    	editConfiguration.addModelChangePreprocessor( new DefaultDataPropEmptyField() );	        	
    	
		return editConfiguration;	
	}
	
	
	public static void prepareForDataPropUpdate(Model model, EditConfigurationVTwo editConfiguration, DataPropertyDao dataPropertyDao){
	      
	    String subjectUri = editConfiguration.getSubjectUri();
	    String predicateUri = editConfiguration.getPredicateUri();
	    Integer dataHash = editConfiguration.getDatapropKey();	    
	    
	    if( predicateUri == null )
	        throw new Error("predicateUri was null");
	    
	    DataProperty dataproperty = dataPropertyDao.getDataPropertyByURI( predicateUri );
	    if( dataproperty == null && ! VitroVocabulary.LABEL.equals( predicateUri ))
	        throw new Error("could not get data property for " + predicateUri);
	    
        DataPropertyStatement dps = null;
        if( dataHash == null ){
            throw new Error("prepareForDataPropUpdate() should not be called if the EditConfiguration is not a data property statement update ");
        }else{
            dps = RdfLiteralHash.getPropertyStmtByHash(subjectUri, predicateUri, dataHash, model);                                  
            if (dps==null){ 
                throw new Error("No match to existing data property \""+predicateUri+"\" statement for subject \""+subjectUri+"\" via key "+dataHash);
            }else{
                //Put data property statement's literal in scope                
                //TODO: Check if multiple statements might affect this implementation?                
                editConfiguration.addLiteralInScope(
                        editConfiguration.getVarNameForObject(), 
                        new EditLiteral(dps.getData(),dps.getDatatypeURI(), dps.getLanguage()) );
    
                dataTypeDebug( dps, dataproperty );                                               
            }
        }         
        


	}


    private static void dataTypeDebug(DataPropertyStatement dps,
            DataProperty dataproperty) {
        if( dps == null )
            return;
        
        String statementDataType = null;
        String statementLang = null;
        
        statementLang = dps.getLanguage();
        if( statementLang == null ) {
            log.debug("no language attribute on data property statement in DefaultDataPropertyFormGenerator");                
        }else{
            log.debug("language attribute of ["+statementLang+"] on data property statement in DefaultDataPropertyFormGenerator");                
        }
                
        if( dataproperty == null )
            return;
                                     
        statementDataType = dps.getDatatypeURI();
        if( statementDataType == null ){
            log.debug("no range datatype uri set on data property statement when property's range datatype is "+dataproperty.getRangeDatatypeURI()+" in DefaultDataPropertyFormGenerator");                
        } else {
            log.debug("range datatype uri of ["+statementDataType+"] on data property statement in DefaultDataPropertyFormGenerator");
        }                          
    }
	
	
}
