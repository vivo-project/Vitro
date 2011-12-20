/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.shared.Lock;


import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseBasicAjaxControllers;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

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
    protected Actions requiredActions(VitroRequest vreq) {
    	return new Actions(new UseBasicAjaxControllers());
    }
    
   @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response) {

        String errorMsg = null;
        String rankPredicate = vreq.getParameter(RANK_PREDICATE_PARAMETER_NAME);
        if (rankPredicate == null) {
            errorMsg = "No rank parameter specified";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_BAD_REQUEST );
            return;
        }

        String[] individualUris = vreq.getParameterValues(INDIVIDUAL_PREDICATE_PARAMETER_NAME);
        if (individualUris == null || individualUris.length == 0) {
            errorMsg = "No individuals specified";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_BAD_REQUEST);  
            return;
        }

        WebappDaoFactory wadf = vreq.getWebappDaoFactory();        
        if( vreq.getWebappDaoFactory() == null) {
            errorMsg = "No WebappDaoFactory available";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        DataPropertyStatementDao dpsDao = wadf.getDataPropertyStatementDao();  
        if( dpsDao == null) {
            errorMsg = "No DataPropertyStatementDao available";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        //check permissions     
        //TODO: (bdc34)This is not yet implemented, must check the IDs against the policies for permissons before doing an edit!
        // rjy7 This should be inherited from the superclass
        boolean hasPermission = true;        
        if( !hasPermission ){
            //if not okay, send error message
            doError(response,"Insufficent permissions", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        // This may not be the most efficient way. Should we instead build up a Model of retractions and additions, so
        // we only hit the database once?
        reorderIndividuals(individualUris, vreq, rankPredicate);
       
        
        response.setStatus(HttpServletResponse.SC_OK);
        
    }
    
    private void reorderIndividuals(String[] individualUris, VitroRequest vreq, String rankPredicate) {
    	//Testing new mechanism
    	OntModel writeModel = vreq.getJenaOntModel();
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
        	//New statement is new literal with the data property from
        	Literal dataLiteral = null;
        	if(datapropURI != null && datapropURI.length() > 0) {
        		dataLiteral = ResourceFactory.createTypedLiteral(String.valueOf(counter), TypeMapper.getInstance().getSafeTypeByName(datapropURI));
        	} else {
            	dataLiteral = ResourceFactory.createPlainLiteral(String.valueOf(counter));

        	}
        	additions.add(individualResource, rankPredicateProperty, dataLiteral);
        	counter++;
        }
        
        Lock lock = null;
        try{
            lock =  writeModel.getLock();
            lock.enterCriticalSection(Lock.WRITE);
            writeModel.getBaseModel().notifyEvent(new EditEvent(null,true));   
            writeModel.add( additions );
            writeModel.remove( retractions );
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
