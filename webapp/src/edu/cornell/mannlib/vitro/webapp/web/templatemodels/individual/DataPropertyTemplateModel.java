/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class DataPropertyTemplateModel extends PropertyTemplateModel {

    private static final Log log = LogFactory.getLog(DataPropertyTemplateModel.class);  
    
    private static final String TYPE = "data";
    private List<DataPropertyStatementTemplateModel> statements;

    DataPropertyTemplateModel(DataProperty dp, Individual subject, VitroRequest vreq) {
        super(dp);
        setName(dp.getPublicName());
        
        // Get the data property statements via a sparql query
        DataPropertyStatementDao dpDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<DataPropertyStatement> dpStatements = dpDao.getDataPropertyStatementsForIndividualByProperty(subject, dp);
        statements = new ArrayList<DataPropertyStatementTemplateModel>(dpStatements.size());
        for (DataPropertyStatement dps : dpStatements) {
            statements.add(new DataPropertyStatementTemplateModel(dps));
        }
    }
    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }

    @Override
    public String getAddLink() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEditLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getDeleteLink() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public List<DataPropertyStatementTemplateModel> getStatements() {
        return statements;
    }

}
