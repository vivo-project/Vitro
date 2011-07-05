/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.listener.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.BasicValidation;

public class IndividualDataPropertyStatementProcessor implements ChangeListener {
	
	private static final Log log = LogFactory.getLog(IndividualDataPropertyStatementProcessor.class.getName());

    public void doInserted(Object newObj, EditProcessObject epo) {
        processDataprops(epo);
    }

    public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
        processDataprops(epo);
    }

    public void doDeleted(Object oldObj, EditProcessObject epo) {
        // do nothing
    }

    private void processDataprops (EditProcessObject epo) {
        HashSet<String> deletedDataPropertyURIs = new HashSet<String>();
        Map dpm = datapropParameterMap(epo.getRequestParameterMap());
        DataPropertyStatementDao dataPropertyStatementDao = (DataPropertyStatementDao)epo.getAdditionalDaoMap().get("DataPropertyStatement");
        Iterator dpmIt = dpm.keySet().iterator();
        while (dpmIt.hasNext()) {
            String key = (String) dpmIt.next();
            String[] data = (String[])dpm.get(key);
            for (int dataRow=0; dataRow<data.length; ++dataRow){
                String[] keyArg = key.split("_");
                String rowId = keyArg[2];
                DataPropertyStatement dataPropertyStmt = new DataPropertyStatementImpl();
                if (rowId != null) {
                    // dataPropertyStmt = dataPropertyStatementDao.getDataPropertyStatementByURI(rowId);
                } else
                    dataPropertyStmt = new DataPropertyStatementImpl();
                try {
                    Map beanParamMap = FormUtils.beanParamMapFromString(keyArg[3]);
                    String dataPropertyURI = (String)beanParamMap.get("DatatypePropertyURI");
                    if (!deletedDataPropertyURIs.contains(dataPropertyURI)) {
                        deletedDataPropertyURIs.add(dataPropertyURI);
                        dataPropertyStatementDao.deleteDataPropertyStatementsForIndividualByDataProperty(((Individual)epo.getNewBean()).getURI(),dataPropertyURI);
                    }
                    dataPropertyStmt.setDatapropURI(dataPropertyURI);
                } catch (Exception e) {
                    log.error("Messed up beanParamMap?");
                }
                dataPropertyStmt.setData(data[dataRow]);
                Individual individual = null;
                // need to rethink this
                if (((Individual)epo.getOriginalBean()).getURI() != null) {
                	individual = (Individual) epo.getOriginalBean();
                    dataPropertyStmt.setIndividualURI(individual.getURI());
                } else {
                	individual = (Individual) epo.getNewBean();
                    dataPropertyStmt.setIndividualURI(individual.getURI());
                }
                if (dataPropertyStmt.getData().length()>0 && rowId != null) {
                	
            		DataPropertyDao dataPropertyDao = (DataPropertyDao)epo.getAdditionalDaoMap().get("DataProperty");
            		DataProperty dp = dataPropertyDao.getDataPropertyByURI(dataPropertyStmt.getDatapropURI());
            		if (dp != null) {
	            		String rangeDatatypeURI = dataPropertyDao.getRequiredDatatypeURI(individual, dp);
	            		if (rangeDatatypeURI != null) {
	            			dataPropertyStmt.setDatatypeURI(rangeDatatypeURI);
	            			String validationMsg = BasicValidation.validateAgainstDatatype(dataPropertyStmt.getData(), rangeDatatypeURI);
	            			// Since this backend editing system is de facto deprecated,
	            			// not worrying about implementing per-field validation
	            			if (validationMsg != null) {
	            				validationMsg = "'" + dataPropertyStmt.getData() + "'"  
	            				    + " is invalid. "
	            				    + validationMsg;
	            				throw new RuntimeException(validationMsg);
	            			}
	            		}
            		}
                	
                    dataPropertyStatementDao.insertNewDataPropertyStatement(dataPropertyStmt);
                } //else if (dataPropertyStmt.getData().length()>0 && rowId != null) {
                    // dataPropertyStatementDao.updateDataPropertyStatement(dataPropertyStmt);
                //} else if (dataPropertyStmt.getData().length()==0 && rowId != null) {
                    // dataPropertyStatementDao.deleteDataPropertyStatement(dataPropertyStmt);
                //}
            }
        }
    }

    // might want to roll this into the other thing
    private HashMap datapropParameterMap(Map requestParameterMap) {
        HashMap dpm = new HashMap();
        Iterator paramIt = requestParameterMap.keySet().iterator();
        while (paramIt.hasNext()) {
            String key = (String) paramIt.next();
            if (key.startsWith("_DataPropertyStatement")) {
                dpm.put(key,requestParameterMap.get(key));
            }
        }
        return dpm;
    }


}
