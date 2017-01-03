/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.sdb.StoreDesc;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

public class RDFServiceFactorySDB implements RDFServiceFactory {

    private final static Log log = LogFactory.getLog(RDFServiceFactorySDB.class);
    
    private DataSource ds;
    private StoreDesc storeDesc;
    private RDFService longTermRDFService;
    
    public RDFServiceFactorySDB(DataSource dataSource, StoreDesc storeDesc) {
        this.ds = dataSource;
        this.storeDesc = storeDesc;
        this.longTermRDFService = new RDFServiceSDB(dataSource, storeDesc);
    }
    
    @Override
    public RDFService getRDFService() {
        return this.longTermRDFService;
    }

    @Override
    public RDFService getShortTermRDFService() {
        try {
            RDFService rdfService = new RDFServiceSDB(ds.getConnection(), storeDesc);
            for (ChangeListener cl : ((RDFServiceSDB) longTermRDFService)
                    .getRegisteredListeners() ) {
                rdfService.registerListener(cl);    
            }
            for (ModelChangedListener cl : ((RDFServiceSDB) longTermRDFService)
                    .getRegisteredJenaModelChangedListeners() ) {
                rdfService.registerJenaModelChangedListener(cl);    
            }
            return rdfService;
        } catch (Exception e) {
            log.error(e,e);
            throw new RuntimeException(e);
        }
    } 

    @Override
    public void registerListener(ChangeListener changeListener)
            throws RDFServiceException {
        this.longTermRDFService.registerListener(changeListener);
    }

    @Override
    public void unregisterListener(ChangeListener changeListener)
            throws RDFServiceException {
        this.longTermRDFService.unregisterListener(changeListener);
    }
    
    @Override
    public void registerJenaModelChangedListener(ModelChangedListener changeListener)
            throws RDFServiceException {
        this.longTermRDFService.registerJenaModelChangedListener(changeListener);
    }

    @Override
    public void unregisterJenaModelChangedListener(ModelChangedListener changeListener)
            throws RDFServiceException {
        this.longTermRDFService.unregisterJenaModelChangedListener(changeListener);
    }

}
