/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import java.sql.Connection;
import java.sql.SQLException;

public class SDBGraphGenerator implements GraphGenerator {

	private static final Log log = LogFactory.getLog(SDBGraphGenerator.class.getName());
	
    private BasicDataSource ds;
    private Connection connection;
    private StoreDesc storeDesc;
    private String graphID;
	
    public SDBGraphGenerator(BasicDataSource dataSource, StoreDesc storeDesc,
    							String graphID) {
    	this.ds = dataSource;
    	this.storeDesc = storeDesc;
    	this.graphID = graphID;
    }

    public Graph generateGraph() {
        try {
            if ( this.connection == null ) {
                this.connection = ds.getConnection();
            } else if ( this.connection.isClosed() ) {
                try {
                    this.connection.close();
                } catch (SQLException e) {                  
                    // The connection will throw an "Already closed"
                    // SQLException that we need to catch.  We need to 
                    // make this extra call to .close() in order to make
                    // sure that the connection is returned to the pool.
                    // This depends on the particular behavior of version
                    // 1.4 of the Apache Commons connection pool library.
                    // Earlier versions threw the exception right away,
                    // making this impossible. Future versions may do the
                    // same.
                }
                this.connection = ds.getConnection();
            }
            Store store = SDBFactory.connectStore(connection, storeDesc);
            return SDBFactory.connectNamedGraph(store, graphID); 
        } catch (SQLException e) {
            String errMsg = "Unable to generate SDB graph"; 
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }
    	
	public Connection getConnection() {
		return connection;
	}

}
