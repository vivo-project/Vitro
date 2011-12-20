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

public class SDBGraphGenerator implements SQLGraphGenerator {

	private static final Log log = LogFactory.getLog(SDBGraphGenerator.class.getName());
	
	private SDBGraphConnectionGenerator connGen;
    private Connection connection;
    private StoreDesc storeDesc;
    private String graphID;
    
    public SDBGraphGenerator(BasicDataSource dataSource, StoreDesc storeDesc,
    							String graphID) {
    	this.connGen = new SDBGraphConnectionGenerator(dataSource);
    	this.storeDesc = storeDesc;
    	this.graphID = graphID;
    }
    
    public SDBGraphGenerator(SDBGraphConnectionGenerator connectionGenerator, 
            StoreDesc storeDesc, String graphID) {
    	this.connGen = connectionGenerator;
    	this.storeDesc = storeDesc;
    	this.graphID = graphID;
    }
    
    public boolean isGraphClosed() {
    	try {
    		return (connection == null || connection.isClosed());
        } catch (SQLException e) {
        	throw new RuntimeException(e);
        }
    }

    public Graph generateGraph() {
        try {
        	this.connection = connGen.generateConnection();
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
