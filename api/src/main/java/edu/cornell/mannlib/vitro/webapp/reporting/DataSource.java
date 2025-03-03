/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_DISTRIBUTORNAME;
import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_DISTRIBUTORRANK;
import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_OUTPUTNAME;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import edu.cornell.library.scholars.webapp.controller.api.DistributeDataApiController;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextImpl;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;


/**
 * Defines a datasource for the report
 */
public class DataSource {

    private static final Log log = LogFactory.getLog(DataSource.class);
    private String distributorName;
    private String outputName;
    private int rank;

    public String getBody(Map<String, String[]> parameters, RequestModelAccess request, UserAccount account) {
        Model model = request.getOntModel(DISPLAY);
        try {
            String uri = DistributeDataApiController.findDistributorUri(model, distributorName);
            DataDistributor instance = DistributeDataApiController.instantiateDistributor(uri, model);
            instance.init(new DataDistributorContextImpl(request, parameters, account));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            instance.writeOutput(baos);
            return baos.toString();
        } catch (Exception e) {
            log.error(e, e);
        }
        return null;
    }

    @Property(uri = PROPERTY_DISTRIBUTORNAME, minOccurs = 1, maxOccurs = 1)
    public void setDistributorName(String name) {
        distributorName = name;
    }

    @Property(uri = PROPERTY_DISTRIBUTORRANK, minOccurs = 0, maxOccurs = 1)
    public void setRank(Integer rank) {
        if (rank != null) {
            this.rank = rank;
        }
    }

    @Property(uri = PROPERTY_OUTPUTNAME, minOccurs = 1, maxOccurs = 1)
    public void setOutputName(String name) {
        outputName = name;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public int getRank() {
        return rank;
    }
}
