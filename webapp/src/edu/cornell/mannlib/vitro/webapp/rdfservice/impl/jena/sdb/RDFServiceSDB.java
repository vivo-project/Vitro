/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.ListeningGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

public class RDFServiceSDB extends RDFServiceJena implements RDFService {

    private final static Log log = LogFactory.getLog(RDFServiceSDB.class);
    
    private DataSource ds;
    private StoreDesc storeDesc;
    
    public RDFServiceSDB(DataSource dataSource, StoreDesc storeDesc) {
        this.ds = dataSource;
        this.storeDesc = storeDesc;
    }
    
    @Override
    protected DatasetWrapper getDatasetWrapper() {
        try {
            SDBConnection conn = new SDBConnection(ds.getConnection());
            return new DatasetWrapper(getDataset(conn), conn);
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
            
        SDBConnection conn = null;
        try {
            conn = new SDBConnection(ds.getConnection());
        } catch (SQLException sqle) {
            log.error(sqle, sqle);
            throw new RDFServiceException(sqle);
        }
        
        Dataset dataset = getDataset(conn);
        boolean transaction = conn.getTransactionHandler().transactionsSupported();
        
        try {       
            
            if (transaction) {
                conn.getTransactionHandler().begin();
            }
            
            for (Object o : changeSet.getPreChangeEvents()) {
                this.notifyListenersOfEvent(o);
            }

            Iterator<ModelChange> csIt = changeSet.getModelChanges().iterator();
            while (csIt.hasNext()) {
                ModelChange modelChange = csIt.next();
                if (!modelChange.getSerializedModel().markSupported()) {
                    byte[] bytes = IOUtils.toByteArray(modelChange.getSerializedModel());
                    modelChange.setSerializedModel(new ByteArrayInputStream(bytes));
                }
                modelChange.getSerializedModel().mark(Integer.MAX_VALUE);
                dataset.getLock().enterCriticalSection(Lock.WRITE);
                try {
                    Model model = (modelChange.getGraphURI() == null)
                            ? dataset.getDefaultModel() 
                            : dataset.getNamedModel(modelChange.getGraphURI());
                    operateOnModel(model, modelChange, dataset);
                } finally {
                    dataset.getLock().leaveCriticalSection();
                }
            }
            
            if (transaction) {
                conn.getTransactionHandler().commit();
            }
            
            // notify listeners of triple changes
            csIt = changeSet.getModelChanges().iterator();
            while (csIt.hasNext()) {
                ModelChange modelChange = csIt.next();
                modelChange.getSerializedModel().reset();
                Model model = ModelFactory.createModelForGraph(
                        new ListeningGraph(modelChange.getGraphURI(), this));
                operateOnModel(model, modelChange, null);
            }
            
            for (Object o : changeSet.getPostChangeEvents()) {
                this.notifyListenersOfEvent(o);
            }
            
        } catch (Exception e) {
            log.error(e, e);
            if (transaction) {
                conn.getTransactionHandler().abort();
            }
            throw new RDFServiceException(e);
        } finally {
            conn.close();
        }
        
        return true;
    }  
    
    protected Dataset getDataset(SDBConnection conn) {
        Store store = SDBFactory.connectStore(conn, storeDesc);
        store.getLoader().setUseThreading(false);
        return SDBFactory.connectDataset(store);
    }
}
