/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.listener.impl;

import java.util.HashMap;
import java.util.HashSet;
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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.BasicValidationVTwo;

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
        Map<String, String[]> dpm = datapropParameterMap(epo.getRequestParameterMap());
        DataPropertyStatementDao dataPropertyStatementDao = (DataPropertyStatementDao)epo.getAdditionalDaoMap().get("DataPropertyStatement");
        for (String key : dpm.keySet()) {
            String[] data = (String[]) dpm.get(key);
            for (String aData : data) {
                String[] keyArg = key.split("_");
                String rowId = keyArg[2];
                DataPropertyStatement dataPropertyStmt = new DataPropertyStatementImpl();
                if (rowId != null) {
                    // dataPropertyStmt = dataPropertyStatementDao.getDataPropertyStatementByURI(rowId);
                } else
                    dataPropertyStmt = new DataPropertyStatementImpl();
                try {
                    Map beanParamMap = FormUtils.beanParamMapFromString(keyArg[3]);
                    String dataPropertyURI = (String) beanParamMap.get("DatatypePropertyURI");
                    if (!deletedDataPropertyURIs.contains(dataPropertyURI)) {
                        deletedDataPropertyURIs.add(dataPropertyURI);
                        dataPropertyStatementDao.deleteDataPropertyStatementsForIndividualByDataProperty(((Individual) epo.getNewBean()).getURI(), dataPropertyURI);
                    }
                    dataPropertyStmt.setDatapropURI(dataPropertyURI);
                } catch (Exception e) {
                    log.error("Messed up beanParamMap?");
                }
                dataPropertyStmt.setData(aData);
                Individual individual = null;
                // need to rethink this
                if (((Individual) epo.getOriginalBean()).getURI() != null) {
                    individual = (Individual) epo.getOriginalBean();
                    dataPropertyStmt.setIndividualURI(individual.getURI());
                } else {
                    individual = (Individual) epo.getNewBean();
                    dataPropertyStmt.setIndividualURI(individual.getURI());
                }
                if (dataPropertyStmt.getData().length() > 0 && rowId != null) {

                    DataPropertyDao dataPropertyDao = (DataPropertyDao) epo.getAdditionalDaoMap().get("DataProperty");
                    DataProperty dp = dataPropertyDao.getDataPropertyByURI(dataPropertyStmt.getDatapropURI());
                    if (dp != null) {
                        String rangeDatatypeURI = dataPropertyDao.getRequiredDatatypeURI(individual, dp);
                        if (rangeDatatypeURI != null) {
                            dataPropertyStmt.setDatatypeURI(rangeDatatypeURI);
                            String validationMsg = BasicValidationVTwo.validateAgainstDatatype(dataPropertyStmt.getData(), rangeDatatypeURI);
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
    private Map<String, String[]> datapropParameterMap(Map<String, String[]> requestParameterMap) {
        Map<String, String[]> dpm = new HashMap<String, String[]>();
        for (String key : requestParameterMap.keySet()) {
            if (key.startsWith("_DataPropertyStatement")) {
                dpm.put(key, requestParameterMap.get(key));
            }
        }
        return dpm;
    }


}
