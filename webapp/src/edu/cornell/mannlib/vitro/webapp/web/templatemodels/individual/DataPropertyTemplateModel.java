/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;

public class DataPropertyTemplateModel extends PropertyTemplateModel {

    private static final Log log = LogFactory.getLog(DataPropertyTemplateModel.class);  
    
    private static final String TYPE = "data";
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private List<DataPropertyStatementTemplateModel> statements;

    DataPropertyTemplateModel(DataProperty dp, Individual subject, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(dp, subject, policyHelper);

        setName(dp.getPublicName());
        
        // Determine whether a new statement can be added
        if (policyHelper != null) {
            RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri,RequestActionConstants.SOME_LITERAL, null, null);
            PolicyDecision decision = policyHelper.getPolicyDecision(action);
            if( decision != null && decision.getAuthorized() == Authorization.AUTHORIZED ) {
                addAccess = true;
            }            
        }
        
        // Get the data property statements via a sparql query
        DataPropertyStatementDao dpDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<DataPropertyStatement> dpStatements = dpDao.getDataPropertyStatementsForIndividualByProperty(subject, dp);
        statements = new ArrayList<DataPropertyStatementTemplateModel>(dpStatements.size());
        for (DataPropertyStatement dps : dpStatements) {
            statements.add(new DataPropertyStatementTemplateModel(dps, policyHelper));
        }
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
            addUrl = UrlBuilder.getUrl(EDIT_PATH, params);       
        }
        return addUrl;
    }
    
    public List<DataPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
}
