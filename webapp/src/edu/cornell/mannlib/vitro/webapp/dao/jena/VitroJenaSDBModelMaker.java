/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class VitroJenaSDBModelMaker implements ModelMaker {

    private final static Log log = LogFactory.getLog(VitroJenaSDBModelMaker.class);
    
    private StoreDesc storeDesc = null;
    private BasicDataSource bds = null;
    private SDBConnection conn = null;
    private SDBGraphConnectionGenerator connGen = null;
    
    public static final String METADATA_MODEL_URI = 
            "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/metadata";
    
    public static final String HAS_NAMED_MODEL_URI = 
            "http://vitro.mannlib.cornell.edu/ns/vitro/sdb/hasNamedModel";
    
    private Resource sdbResource; // a resource representing the SDB database 
    
    public VitroJenaSDBModelMaker(StoreDesc storeDesc, BasicDataSource bds) 
            throws SQLException {
        
        this.storeDesc = storeDesc;
        this.bds = bds;
        connGen = new SDBGraphConnectionGenerator(bds); 
        
        Store store = getStore();
        try {
            setUpMetadata(store);            
        } finally {
            store.close();
        }
    }
    
    private static final int MAX_TRIES = 10;
    
    private Store getStore() {
        Store store = null;
        boolean goodStore = false;
        int tries = 0;
        while (!goodStore && tries < MAX_TRIES) {
            tries++;
            if (conn == null) {
                try {
                    conn = new SDBConnection(connGen.generateConnection());
                } catch (SQLException sqle) {
                    throw new RuntimeException(
                            "Unable to get SQL connection", sqle);
                }
            }
            store = SDBFactory.connectStore(conn, storeDesc);
            try {
                if (!StoreUtils.isFormatted(store)) {
                    // initialize the store
                    try {
                        store.getTableFormatter().create();
                        store.getTableFormatter().truncate();
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Unable to format store for " +
                                "VitroJenaSDBModelMaker", e); 
                    }
                }
            } catch (SQLException sqle) {
                conn.close();
                conn = null;
            }
            if (conn != null) {
                if (isWorking(store)) {
                     goodStore = true;
                } else {
                    conn.close();
                    conn = null;
                }
            }
        }
        if (store == null) {
            throw new RuntimeException(
                    "Unable to connect to SDB store after " + 
                    MAX_TRIES + " attempts");
        }
        return store;
    }
    
    Model getMetadataModel() {
        return getModel(METADATA_MODEL_URI);
    }
    
    private void setUpMetadata(Store store) {
        Model metadataModel = getMetadataModel();
        
        if (metadataModel.size() == 0) {
            // set up the model name metadata to avoid expensive calls to 
            // listNames()
            Resource sdbRes = metadataModel.createResource(); 
            this.sdbResource = sdbRes;
            Iterator nameIt = SDBFactory.connectDataset(store).listNames();
            while (nameIt.hasNext()) {
                String name = (String) nameIt.next();
                metadataModel.add(sdbResource,metadataModel.getProperty(
                        HAS_NAMED_MODEL_URI),name);
            }
        } else {
            StmtIterator stmtIt = metadataModel.listStatements(
                    (Resource) null, metadataModel.getProperty(
                            HAS_NAMED_MODEL_URI),(RDFNode) null);
            if (stmtIt.hasNext()) {
                Statement stmt = stmtIt.nextStatement();
                sdbResource = stmt.getSubject();
            }
            stmtIt.close();
        }

    }
    
    private boolean isWorking(Store store) {
        Dataset d = SDBFactory.connectDataset(store);
        try {
            String validationQuery = "ASK { <" + RDFS.Resource.getURI() + "> " +
                                     "   <" + RDFS.isDefinedBy.getURI() + "> " +
                                     "   <" + RDFS.Resource.getURI() + "> }";
            Query q = QueryFactory.create(validationQuery);
            QueryExecution qe = QueryExecutionFactory.create(q, d);
            try {
                qe.execAsk();
                return true;
            } catch (Exception e) {
                log.error(e, e);
                return false;
            } finally {
                qe.close();
            }
        } finally {
            d.close();
        }
    }
    
    public void close() {
        getStore().close();
    }

    public Model createModel(String modelName) {
        Model model = getModel(modelName);
        Model metadataModel = getMetadataModel();
        try {
            metadataModel.add(
                    sdbResource,metadataModel.getProperty(
                            HAS_NAMED_MODEL_URI), modelName);
        } finally {
            metadataModel.close();
        }
        return model;
    }

    public Model createModel(String arg0, boolean arg1) {
        // TODO Figure out if we can offer a "create if not found" option using SDB
        return createModel(arg0);
    }

    public GraphMaker getGraphMaker() {
        throw new UnsupportedOperationException(
                "GraphMaker not supported by " + this.getClass().getName());
    }

    public boolean hasModel(String arg0) {
        Model metadataModel = getMetadataModel();
        try {
            StmtIterator stmtIt = metadataModel.listStatements(
                    sdbResource, metadataModel.getProperty(
                            HAS_NAMED_MODEL_URI), arg0);
            try {
                return stmtIt.hasNext();
            } finally {
                if (stmtIt != null) {
                    stmtIt.close();
                }
            }
        } finally {
            metadataModel.close();
        }
    }

    public ExtendedIterator<String> listModels() {
        Model metadataModel = getMetadataModel();
        try {
            return listModelNames(metadataModel);
        } finally {    
            metadataModel.close();
        }
    }
    
    private ExtendedIterator<String> listModelNames(Model metadataModel) {
        Set<String> modelNameSet = new HashSet<String>();
        
        Iterator<RDFNode> metadataNameIt = metadataModel.listObjectsOfProperty(
                metadataModel.getProperty(HAS_NAMED_MODEL_URI));    
        while (metadataNameIt.hasNext()) {
            RDFNode rdfNode = metadataNameIt.next();
            if (rdfNode.isLiteral()) {
                modelNameSet.add(((Literal) rdfNode).getLexicalForm());
            }
        }
        
        Iterator<Node> storeNameIt = StoreUtils.storeGraphNames(getStore());
        while (storeNameIt.hasNext()){
             Node node = storeNameIt.next();
             modelNameSet.add(node.getURI());
        }
        
        List<String> modelNameList = new ArrayList<String>();
        modelNameList.addAll(modelNameSet);
        Collections.sort(modelNameList, Collator.getInstance());
                
        return WrappedIterator.create(modelNameList.iterator());
    }

    public Model openModel(String arg0, boolean arg1) {
        return SDBFactory.connectNamedModel(getStore(),arg0);
    }

    public void removeModel(String arg0) {
        Model m = getModel(arg0);
        m.removeAll(null,null,null);
        Model metadataModel = getMetadataModel();
        try {
            metadataModel.remove(sdbResource, metadataModel.getProperty(
                    HAS_NAMED_MODEL_URI),metadataModel.createLiteral(arg0));
        } finally {
            metadataModel.close();
        }
    }

    public Model addDescription(Model arg0, Resource arg1) {
        throw new UnsupportedOperationException(
                "addDescription not supported by " + this.getClass().getName());
    }

    public Model createModelOver(String arg0) {
        throw new UnsupportedOperationException(
                "createModelOver not supported by " + this.getClass().getName());
    }

    public Model getDescription() {
        throw new UnsupportedOperationException(
                "createModelOver not supported by " + this.getClass().getName());
    }

    public Model getDescription(Resource arg0) {
        throw new UnsupportedOperationException(
                "getDescription not supported by "+this.getClass().getName());
    }

    public Model openModel() {
        return SDBFactory.connectDefaultModel(getStore());
    }

    public Model createDefaultModel() {
        return SDBFactory.connectDefaultModel(getStore());
    }

    public Model createFreshModel() {
        throw new UnsupportedOperationException(
                "createFreshModel not supported by " + this.getClass().getName());
    }

    /**
     * @deprecated
     */
    public Model createModel() {
        return SDBFactory.connectDefaultModel(getStore());
    }

    /**
     * @deprecated
     */
    public Model getModel() {
        return SDBFactory.connectDefaultModel(getStore());
    }

    public Model openModel(String arg0) {
        return SDBFactory.connectDefaultModel(getStore());
    }

    public Model openModelIfPresent(String arg0) {
        return (this.hasModel(arg0)) 
                ? SDBFactory.connectNamedModel(getStore(),arg0) 
                : null;
    }

    public Model getModel(String modelName) {
    	SDBGraphGenerator graphGen = new SDBGraphGenerator(
    			connGen, storeDesc, modelName); 	
        Graph g = new RegeneratingGraph(
                SDBFactory.connectNamedGraph(getStore(), modelName), graphGen);
        return ModelFactory.createModelForGraph(g);
    }

    public Model getModel(String arg0, ModelReader arg1) {
        throw new UnsupportedOperationException(
                "getModel(String, ModelReader) not supported by " + 
                        this.getClass().getName());
    }

}
