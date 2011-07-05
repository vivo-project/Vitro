/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class DataPropertyTemplateModel extends PropertyTemplateModel {

    private static final Log log = LogFactory.getLog(DataPropertyTemplateModel.class);  
    
    private static final String TYPE = "data";
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private List<DataPropertyStatementTemplateModel> statements;
    
    DataPropertyTemplateModel(DataProperty dp, Individual subject, VitroRequest vreq, 
            EditingPolicyHelper policyHelper, List<DataProperty> populatedDataPropertyList) {
        
        super(dp, subject, policyHelper, vreq);
        vitroRequest = vreq;
        setName(dp.getPublicName());

        statements = new ArrayList<DataPropertyStatementTemplateModel>();
        
        // If the property is populated, get the data property statements via a sparql query
        if (populatedDataPropertyList.contains(dp)) {
            log.debug("Getting data for populated data property " + getUri());
            DataPropertyStatementDao dpDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
            List<Literal> values = dpDao.getDataPropertyValuesForIndividualByProperty(subject, dp);            
            for (Literal value : values) {
                statements.add(new DataPropertyStatementTemplateModel(subjectUri, propertyUri, value, policyHelper, vreq));
            }
        } else {
            log.debug("Data property " + getUri() + " is unpopulated.");
        }
        
        setAddAccess(policyHelper, dp);
        
    }

    // Determine whether a new statement can be added
    @Override
    protected void setAddAccess(EditingPolicyHelper policyHelper, Property property) {
        if (policyHelper != null) {
            
            DataProperty dp = (DataProperty) property;
            
            // NIHVIVO-2790 vitro:moniker now included in the display, but don't allow new statements
            if (dp.getURI().equals(VitroVocabulary.MONIKER)) {
                return;
            }
            // If the display limit has already been reached, we can't add a new statement
            int displayLimit = dp.getDisplayLimit();
            // Display limit of -1 (default value for new property) means no display limit
            if ( (displayLimit < 0) || (displayLimit > statements.size()) ) {
                RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri,RequestActionConstants.SOME_LITERAL, null, null);
                if (policyHelper.isAuthorizedAction(action)) {
                    addAccess = true;
                }
            }
        }        
    }
    
    @Override 
    protected int getPropertyDisplayTier(Property p) {
        return ((DataProperty)p).getDisplayTier();
    }

    @Override 
    protected Route getPropertyEditRoute() {
        return Route.DATA_PROPERTY_EDIT;
    }
    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }

    @Override
    public String getAddUrl() {
        String addUrl = "";
        if (addAccess) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri);
            
            //Check if special parameters being sent            
            HashMap<String, String> specialParams = UrlBuilder.getSpecialParams(vitroRequest);
            if(specialParams.size() > 0) {
            	params.putAll(specialParams);
            }
            addUrl = UrlBuilder.getUrl(EDIT_PATH, params);       
        }
        return addUrl;
    }
    
    public List<DataPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
    public DataPropertyStatementTemplateModel getFirst() {
        return ( (statements == null || statements.isEmpty()) ) ? null : statements.get(0);
    }
    
    public String getFirstValue() {
        DataPropertyStatementTemplateModel first = getFirst();
        return first == null ? null : first.getValue();
    }
    
}
