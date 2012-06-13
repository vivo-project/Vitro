/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sdb;

import org.apache.commons.dbcp.BasicDataSource;

import com.hp.hpl.jena.sdb.StoreDesc;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class RDFServiceFactorySDB {

    private BasicDataSource bds;
    private StoreDesc storeDesc;
    
    public RDFServiceFactorySDB(BasicDataSource dataSource, StoreDesc storeDesc) {
        this.bds = dataSource;
        this.storeDesc = storeDesc;
    }
    
    public RDFService getRDFService() {
        return new RDFServiceSDB(bds, storeDesc);
    }
    
}
