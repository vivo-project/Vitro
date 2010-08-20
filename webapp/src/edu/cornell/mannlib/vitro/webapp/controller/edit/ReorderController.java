/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This controller receives Ajax requests for reordering a list of individuals. 
 * Parameters:
 * predicate: the data property used for ranking
 * individuals: an ordered list of individuals to be ranked
 * @author rjy7
 *
 */
public class ReorderController extends PrimitiveRdfEdit {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ReorderController.class);

    private static String RANK_PREDICATE_PARAMETER_NAME = "predicate";
    private static String INDIVIDUAL_PREDICATE_PARAMETER_NAME = "individuals";
    
    protected void processRequest(VitroRequest vreq, HttpServletResponse response) {

        //String templateName = "autocompleteResults.ftl";
        //Map<String, Object> map = new HashMap<String, Object>();
        //Configuration config = getConfig(vreq);
        //PortalFlag portalFlag = vreq.getPortalFlag();

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
        int counter = 1;
        for (String individualUri : individualUris) {           
            // Retract all existing rank statements for this individual
            dpsDao.deleteDataPropertyStatementsForIndividualByDataProperty(individualUri, rankPredicate);
        
            // Then add the new rank statement for this individual
            // insertNewDataPropertyStatement will insert the rangeDatatype of the property, so we don't need to set that here.
            dpsDao.insertNewDataPropertyStatement(new DataPropertyStatementImpl(individualUri, rankPredicate, String.valueOf(counter)));
            
            counter++;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        
    }
    
    protected void doError(HttpServletResponse response, String errorMsg, int httpstatus) {
        super.doError(response, "Error: " + errorMsg, httpstatus);
    }
    
}
