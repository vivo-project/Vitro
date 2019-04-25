/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;

public class SDBGraphGenerator implements SQLGraphGenerator {

	private static final Log log = LogFactory.getLog(SDBGraphGenerator.class.getName());

	private SDBGraphConnectionGenerator connGen;
    private Connection connection;
    private StoreDesc storeDesc;
    private String graphID;

    public SDBGraphGenerator(DataSource dataSource, StoreDesc storeDesc,
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
