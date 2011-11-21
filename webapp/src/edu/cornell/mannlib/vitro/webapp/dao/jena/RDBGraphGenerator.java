/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.SQLException;
import java.sql.Connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.graph.Graph;



public class RDBGraphGenerator implements SQLGraphGenerator {

	private static final Log log = LogFactory.getLog(RDBGraphGenerator.class.getName());
	
    private BasicDataSource ds = null;
    private Connection connection = null;
    private String dbTypeStr = null;
    private String graphID = null;

    public RDBGraphGenerator(BasicDataSource bds, String dbTypeStr, String graphID) {
        this.ds = bds;
        this.dbTypeStr = dbTypeStr;
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
    	log.info("Regenerate the graph.");
        try {
        	if (log.isDebugEnabled()) {
        		log.debug(ds.getNumActive() + " active SQL connections");
        		log.debug(ds.getNumIdle() + " idle SQL connections");
        	}
        	if ( ( this.connection == null ) || ( this.connection.isClosed() ) ) {
        		this.connection = ds.getConnection();
        	}
            IDBConnection idbConn = new DBConnection(this.connection, dbTypeStr);
            Graph requestedProperties = null;
            boolean modelExists = idbConn.containsModel(graphID);
            if (!modelExists) {
            	requestedProperties = ModelRDB.getDefaultModelProperties(idbConn).getGraph();
            }
            Graph graphRDB = new GraphRDB(idbConn, graphID, requestedProperties, GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING, !modelExists);
            return graphRDB;
        } catch (SQLException e) {
        	log.error(e, e);
        	throw new RuntimeException("SQLException: unable to regenerate graph", e);
        }
    }
    
    public Connection getConnection() {
    	return connection;
    }

}


