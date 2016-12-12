/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

import java.io.IOException;

/**
 * This controller receives Ajax requests for reordering a list of individuals. 
 * Parameters:
 * predicate: the data property used for ranking
 * individuals: an ordered list of individuals to be ranked
 * @author rjy7
 *
 */
public class ReorderController extends VitroAjaxController {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ReorderController.class);

    private static String RANK_PREDICATE_PARAMETER_NAME = "predicate";
    private static String INDIVIDUAL_PREDICATE_PARAMETER_NAME = "individuals";

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
    	return SimplePermission.USE_BASIC_AJAX_CONTROLLERS.ACTION;
    }
    
   @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response) {

        String errorMsg = null;
        String rankPredicate = vreq.getParameter(RANK_PREDICATE_PARAMETER_NAME);
        if (rankPredicate == null) {
            errorMsg = "No rank parameter specified";
            log.error(errorMsg);
            doError(response, errorMsg, SC_BAD_REQUEST );
            return;
        }

        String[] individualUris = vreq.getParameterValues(INDIVIDUAL_PREDICATE_PARAMETER_NAME);
        if (individualUris == null || individualUris.length == 0) {
            errorMsg = "No individuals specified";
            log.error(errorMsg);
            doError(response, errorMsg, SC_BAD_REQUEST);  
            return;
        }

        WebappDaoFactory wadf = vreq.getWebappDaoFactory();        
        if( vreq.getWebappDaoFactory() == null) {
            errorMsg = "No WebappDaoFactory available";
            log.error(errorMsg);
            doError(response, errorMsg, SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        DataPropertyStatementDao dpsDao = wadf.getDataPropertyStatementDao();  
        if( dpsDao == null) {
            errorMsg = "No DataPropertyStatementDao available";
            log.error(errorMsg);
            doError(response, errorMsg, SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        //check permissions     
        //TODO: (bdc34)This is not yet implemented, must check the IDs against the policies for permissons before doing an edit!
        // rjy7 This should be inherited from the superclass
        boolean hasPermission = true;        
        if( !hasPermission ){
            //if not okay, send error message
            doError(response,"Insufficent permissions", SC_UNAUTHORIZED);
            return;
        }

        // This may not be the most efficient way. Should we instead build up a Model of retractions and additions, so
        // we only hit the database once?
        reorderIndividuals(individualUris, vreq, rankPredicate);

       response.setStatus(SC_OK);
       try {
           response.getWriter().write("{}");
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
    
    private void reorderIndividuals(String[] individualUris, VitroRequest vreq, String rankPredicate) {
    	//Testing new mechanism
    	OntModel writeModel = vreq.getOntModelSelector().getABoxModel();
    	Model additions = ModelFactory.createDefaultModel();
        Model retractions = ModelFactory.createDefaultModel();
    	Property rankPredicateProperty = ResourceFactory.createProperty(rankPredicate);
    	DataProperty dp = vreq.getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(rankPredicate);
    	String datapropURI = dp.getRangeDatatypeURI();
    	int counter = 1;
        for (String individualUri : individualUris) {           
        	Resource individualResource = ResourceFactory.createResource(individualUri);
        	//Deletions are all old statements with rank predicate
        	retractions.add(writeModel.listStatements(individualResource, rankPredicateProperty, (RDFNode) null));
        	log.debug("retractions = " + retractions);
        	//New statement is new literal with the data property from
        	Literal dataLiteral = null;
        	if(datapropURI != null && datapropURI.length() > 0) {
        		dataLiteral = ResourceFactory.createTypedLiteral(String.valueOf(counter), TypeMapper.getInstance().getSafeTypeByName(datapropURI));
        	} else {
            	dataLiteral = ResourceFactory.createPlainLiteral(String.valueOf(counter));

        	}
        	additions.add(individualResource, rankPredicateProperty, dataLiteral);
        	log.debug("additions = " + additions);
        	counter++;
        }
        
        Lock lock = null;
        try{
            lock =  writeModel.getLock();
            lock.enterCriticalSection(Lock.WRITE);
            writeModel.getBaseModel().notifyEvent(new EditEvent(null,true));   
            writeModel.remove( retractions );
            writeModel.add( additions );
        }catch(Throwable t){
            log.error("error adding edit change n3required model to in memory model \n"+ t.getMessage() );
        }finally{
            writeModel.getBaseModel().notifyEvent(new EditEvent(null,false));
            lock.leaveCriticalSection();
        }       
        
        
    	//old code that for some reason doesn't seem to actually commit the changes
    	/*
    	 * int counter = 1;
        for (String individualUri : individualUris) {           
            // Retract all existing rank statements for this individual
            dpsDao.deleteDataPropertyStatementsForIndividualByDataProperty(individualUri, rankPredicate);
        
            // Then add the new rank statement for this individual
            // insertNewDataPropertyStatement will insert the rangeDatatype of the property, so we don't need to set that here.
            dpsDao.insertNewDataPropertyStatement(new DataPropertyStatementImpl(individualUri, rankPredicate, String.valueOf(counter)));
            
            counter++;
        }
        
    	 */
	
    }

	protected void doError(HttpServletResponse response, String errorMsg, int httpstatus) {
        super.doError(response, "Error: " + errorMsg, httpstatus);
    }
    
}
