/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.dao.jena.StaticDatasetFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

public class RDFServiceSDB extends RDFServiceJena implements RDFService {

    private final static Log log = LogFactory.getLog(RDFServiceSDB.class);
    
    private DataSource ds;
    private StoreDesc storeDesc;
    private Connection conn;
    private StaticDatasetFactory staticDatasetFactory;
    
    public RDFServiceSDB(DataSource dataSource, StoreDesc storeDesc) {
        this.ds = dataSource;
        this.storeDesc = storeDesc;
    }
    
    public RDFServiceSDB(Connection conn, StoreDesc storeDesc) {
        this.conn = conn;
        this.storeDesc = storeDesc;
        this.staticDatasetFactory = new StaticDatasetFactory(getDataset(
                new SDBConnection(conn)));
    }
    
    @Override
    protected DatasetWrapper getDatasetWrapper() {
        try {
            if (staticDatasetFactory != null) {
                return staticDatasetFactory.getDatasetWrapper();
            }
            SDBConnection sdbConn = new SDBConnection(ds.getConnection());
            return new DatasetWrapper(getDataset(sdbConn), sdbConn);
        } catch (SQLException sqle) {
            log.error(sqle, sqle);
            throw new RuntimeException(sqle);
        }     
    }
       
    @Override
    public boolean changeSetUpdate(ChangeSet changeSet)
            throws RDFServiceException {
             
        if (changeSet.getPreconditionQuery() != null 
                && !isPreconditionSatisfied(
                        changeSet.getPreconditionQuery(), 
                                changeSet.getPreconditionQueryType())) {
            return false;
        }
            
        SDBConnection sdbConn = getSDBConnection();
        Dataset dataset = getDataset(sdbConn);
        
        try {       
        	insureThatInputStreamsAreResettable(changeSet);
            
        	beginTransaction(sdbConn);

        	notifyListenersOfPreChangeEvents(changeSet);
            applyChangeSetToModel(changeSet, dataset);
            
            commitTransaction(sdbConn);
            
            notifyListenersOfChanges(changeSet);
            notifyListenersOfPostChangeEvents(changeSet);
            
            return true;
        } catch (Exception e) {
            log.error(e, e);
            abortTransaction(sdbConn);
            throw new RDFServiceException(e);
        } finally {
            close(sdbConn);
        }
    }
    
	private SDBConnection getSDBConnection() throws RDFServiceException  {
		try {
			Connection c = (conn != null) ? conn : ds.getConnection();
			return new SDBConnection(c);
		} catch (SQLException sqle) {
			log.error(sqle, sqle);
			throw new RDFServiceException(sqle);
		}        
    }

    private void close(SDBConnection sdbConn) {
        if (!sdbConn.getSqlConnection().equals(conn)) {
            sdbConn.close();
        }
    }
    
    private Dataset getDataset(SDBConnection sdbConn) {
        Store store = SDBFactory.connectStore(sdbConn, storeDesc);
        store.getLoader().setUseThreading(false);
        return SDBFactory.connectDataset(store);
    }
    
	private void beginTransaction(SDBConnection sdbConn) {
		if (sdbConn.getTransactionHandler().transactionsSupported()) {
		    sdbConn.getTransactionHandler().begin();
		}
	}

	private void commitTransaction(SDBConnection sdbConn) {
		if (sdbConn.getTransactionHandler().transactionsSupported()) {
			sdbConn.getTransactionHandler().commit();
		}
	}
	
	private void abortTransaction(SDBConnection sdbConn) {
		if (sdbConn.getTransactionHandler().transactionsSupported()) {
			sdbConn.getTransactionHandler().abort();
		}
	}
	
    private static final Pattern OPTIONAL_PATTERN = Pattern.compile("optional", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern GRAPH_PATTERN = Pattern.compile("graph", Pattern.CASE_INSENSITIVE);
    
    @Override
    protected QueryExecution createQueryExecution(String queryString, Query q, Dataset d) {
        // query performance with OPTIONAL can be dramatically improved on SDB by 
        // using the default model (union model) instead of the dataset, so long as 
        // we're not querying particular named graphs
                
        Matcher optional = OPTIONAL_PATTERN.matcher(queryString);
        Matcher graph = GRAPH_PATTERN.matcher(queryString);
        
        if (optional.find() && !graph.find()) { 
            return QueryExecutionFactory.create(q, d.getDefaultModel());
        } else {
            return QueryExecutionFactory.create(q, d); 
        }       
    }
    
    @Override 
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e,e);
            }
        }
    }

}
