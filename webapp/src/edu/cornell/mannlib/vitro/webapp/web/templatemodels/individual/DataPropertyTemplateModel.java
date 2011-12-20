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
    private static final String EDIT_PATH = "editRequestDispatch";  
    
    private final List<DataPropertyStatementTemplateModel> statements;
    
    DataPropertyTemplateModel(DataProperty dp, Individual subject, VitroRequest vreq, 
            EditingPolicyHelper policyHelper, List<DataProperty> populatedDataPropertyList) {
        
        super(dp, subject, policyHelper, vreq);
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
        
        setAddUrl(policyHelper, dp);
    }


    @Override
    protected void setAddUrl(EditingPolicyHelper policyHelper, Property property) {

        if (policyHelper == null) {
            return;
        }
           
        DataProperty dp = (DataProperty) property;        
        // NIHVIVO-2790 vitro:moniker now included in the display, but don't allow new statements
        if (dp.getURI().equals(VitroVocabulary.MONIKER)) {
            return;
        }
        
        // If the display limit has already been reached, we can't add a new statement.
        // NB This appears to be a misuse of a value called "display limit". Note that it's 
        // not used to limit display, either, so should be renamed.
        int displayLimit = dp.getDisplayLimit();
        // Display limit of -1 (default value for new property) means no display limit
        if ( displayLimit >= 0 && statements.size() >= displayLimit ) {
            return;
        }
          
        // Determine whether a new statement can be added
        RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri, RequestActionConstants.SOME_LITERAL, null, null);
        if ( ! policyHelper.isAuthorizedAction(action) ) {
            return;
        }
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", propertyUri);
        
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        addUrl = UrlBuilder.getUrl(EDIT_PATH, params);       
    }
    
    @Override 
    protected int getPropertyDisplayTier(Property p) {
        return ((DataProperty)p).getDisplayTier();
    }

    @Override 
    protected Route getPropertyEditRoute() {
        return Route.DATA_PROPERTY_EDIT;
    }
    
    /* Template properties */
    
    public String getType() {
        return TYPE;
    }

    public List<DataPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
    
    /* Template methods */
    
    public DataPropertyStatementTemplateModel first() {
        return ( (statements == null || statements.isEmpty()) ) ? null : statements.get(0);
    }
    
    public String firstValue() {
        DataPropertyStatementTemplateModel first = first();
        return first == null ? null : first.getValue();
    }
    
}
