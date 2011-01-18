/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
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

        // Get the data property statements via a sparql query
        DataPropertyStatementDao dpDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<Literal> values = dpDao.getDataPropertyValuesForIndividualByProperty(subject, dp);
        statements = new ArrayList<DataPropertyStatementTemplateModel>(values.size());
        for (Literal value : values) {
            statements.add(new DataPropertyStatementTemplateModel(subjectUri, propertyUri, value, policyHelper));
        }
        
        // Determine whether a new statement can be added
        if (policyHelper != null) {
            // If the display limit has already been reached, we can't add a new statement
            int displayLimit = dp.getDisplayLimit();
            // Display limit of -1 (default value for new property) doesn't count
            if ( (displayLimit < 0) || (displayLimit > statements.size()) ) {
                RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri,RequestActionConstants.SOME_LITERAL, null, null);
                if (policyHelper.isAuthorizedAction(action)) {
                    addAccess = true;
                }
            }
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
