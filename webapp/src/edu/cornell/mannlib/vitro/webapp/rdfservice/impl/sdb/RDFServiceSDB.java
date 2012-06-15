/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;

public class RDFServiceSDB extends RDFServiceImpl implements RDFService {

    private final static Log log = LogFactory.getLog(RDFServiceSDB.class);
    
    private BasicDataSource bds;
    private StoreDesc storeDesc;
    
    public RDFServiceSDB(BasicDataSource dataSource, StoreDesc storeDesc) {
        this.bds = dataSource;
        this.storeDesc = storeDesc;
    }
    
    protected DatasetWrapper getDatasetWrapper() {
        try {
            SDBConnection conn = new SDBConnection(bds.getConnection());
            return new DatasetWrapper(getDataset(conn), conn);
        } catch (SQLException sqle) {
            log.error(sqle, sqle);
            throw new RuntimeException(sqle);
        }     
    }
    
    protected Dataset getDataset(SDBConnection conn) {
        Store store = SDBFactory.connectStore(conn, storeDesc);
        store.getLoader().setUseThreading(false);
        return SDBFactory.connectDataset(store);
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
            conn = new SDBConnection(bds.getConnection());
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
    
    private void operateOnModel(Model model, ModelChange modelChange, Dataset dataset) {
        model.enterCriticalSection(Lock.WRITE);
        try {
            if (modelChange.getOperation() == ModelChange.Operation.ADD) {
                model.read(modelChange.getSerializedModel(), null,
                        getSerializationFormatString(modelChange.getSerializationFormat()));  
            } else if (modelChange.getOperation() == ModelChange.Operation.REMOVE) {
                Model removal = parseModel(modelChange);
                model.remove(removal);
                if (dataset != null) {
                    removeBlankNodesWithSparqlUpdate(dataset, removal, modelChange.getGraphURI());
                }
            } else {
                log.error("unrecognized operation type");
            } 
        } finally {
            model.leaveCriticalSection();
        }
    }
    
    private void removeBlankNodesWithSparqlUpdate(Dataset dataset, Model model, String graphURI) {
               
        List<Statement> blankNodeStatements = new ArrayList<Statement>();
        StmtIterator stmtIt = model.listStatements();
        while (stmtIt.hasNext()) {
            Statement stmt = stmtIt.nextStatement();
            if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
                blankNodeStatements.add(stmt);
            }
        }
        
        if(blankNodeStatements.size() == 0) {
            return;
        }
        
        Model blankNodeModel = ModelFactory.createDefaultModel();
        blankNodeModel.add(blankNodeStatements);
        
        log.debug("removal model size " + model.size());
        log.debug("blank node model size " + blankNodeModel.size());
                
        String rootFinder = "SELECT ?s WHERE { ?s ?p ?o OPTIONAL { ?ss ?pp ?s } FILTER (!bound(?ss)) }";
        Query rootFinderQuery = QueryFactory.create(rootFinder);
        QueryExecution qe = QueryExecutionFactory.create(rootFinderQuery, blankNodeModel);
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                Resource s = qs.getResource("s");
                String treeFinder = makeDescribe(s);
                Query treeFinderQuery = QueryFactory.create(treeFinder);
                QueryExecution qee = QueryExecutionFactory.create(treeFinderQuery, blankNodeModel);
                try {
                    Model tree = qee.execDescribe();
                    StmtIterator sit = tree.listStatements(s, null, (RDFNode) null);
                    while (sit.hasNext()) {
                        Statement stmt = sit.nextStatement();
                        RDFNode n = stmt.getObject();
                        Model m2 = ModelFactory.createDefaultModel();
                        if (n.isResource()) {
                            Resource s2 = (Resource) n;
                            // now run yet another describe query
                            String smallerTree = makeDescribe(s2);
                            Query smallerTreeQuery = QueryFactory.create(smallerTree);
                            QueryExecution qe3 = QueryExecutionFactory.create(
                                    smallerTreeQuery, tree);
                            try {
                                qe3.execDescribe(m2);
                            } finally {
                                qe3.close();
                            }    
                        }
                        m2.add(stmt);
                        DataSource ds = DatasetFactory.create();
                        if (graphURI == null) {
                            ds.setDefaultModel(dataset.getDefaultModel());
                        } else {
                            ds.addNamedModel(graphURI, dataset.getNamedModel(graphURI));    
                        }      
                        removeUsingSparqlUpdate(ds, m2, graphURI);
                    }                   
                } finally {
                    qee.close();
                }
            }
        } finally {
            qe.close();
        }        
    }
    
    private String makeDescribe(Resource s) {
        StringBuffer query = new StringBuffer("DESCRIBE <") ;
        if (s.isAnon()) {
            query.append("_:" + s.getId().toString());
        } else {
            query.append(s.getURI());
        }
        query.append(">");
        return query.toString();
    }
    
    private void removeUsingSparqlUpdate(Dataset dataset, Model model, String graphURI) {
        
        StringBuffer patternBuff = new StringBuffer();
        StmtIterator stmtIt = model.listStatements();
        
        if (!stmtIt.hasNext()) {
            stmtIt.close();
            return;
        }
        
        while(stmtIt.hasNext()) {
            Triple t = stmtIt.next().asTriple();
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getSubject(), null));
            patternBuff.append(" ");
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getPredicate(), null));
            patternBuff.append(" ");
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getObject(), null));
            patternBuff.append(" .\n");
        }
        
        StringBuffer queryBuff = new StringBuffer();
        queryBuff.append("CONSTRUCT { \n");
        queryBuff.append(patternBuff);
        queryBuff.append("} WHERE { \n");
        if (graphURI != null) {
            queryBuff.append("    GRAPH <" + graphURI + "> { \n");
        }
        queryBuff.append(patternBuff);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} \n");
        
        //log.debug(queryBuff.toString());
        
        Query construct = QueryFactory.create(queryBuff.toString());
        // make a plain dataset to force the query to be run in a way that
        // won't overwhelm MySQL with too many joins
        DataSource ds = DatasetFactory.create();
        if (graphURI == null) {
            ds.setDefaultModel(dataset.getDefaultModel());
        } else {
            ds.addNamedModel(graphURI, dataset.getNamedModel(graphURI));            
        }
        QueryExecution qe = QueryExecutionFactory.create(construct, ds);
        try {
            Model m = qe.execConstruct();
            if (graphURI != null) {
                dataset.getNamedModel(graphURI).remove(m);    
            } else {
                dataset.getDefaultModel().remove(m);
            }
        } finally {
            qe.close();
        }       
    }
    
    private Model parseModel(ModelChange modelChange) {
        Model model = ModelFactory.createDefaultModel();
        model.read(modelChange.getSerializedModel(), null,
                getSerializationFormatString(modelChange.getSerializationFormat()));
        return model;
    }

    @Override
    public void newIndividual(String individualURI, String individualTypeURI,
            String graphURI) throws RDFServiceException {
        // TODO Auto-generated method stub

    }
    
    private InputStream getRDFResultStream(String query, boolean construct, 
            ModelSerializationFormat resultFormat) throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = QueryFactory.create(query);
            QueryExecution qe = QueryExecutionFactory.create(q, d);
            ByteArrayOutputStream serializedModel = new ByteArrayOutputStream();
            try {
                // TODO pipe this
                Model m = construct ? qe.execConstruct() : qe.execDescribe();
                m.write(serializedModel, getSerializationFormatString(resultFormat));
                InputStream result = new ByteArrayInputStream(serializedModel.toByteArray());
                return result;
            } finally {
                qe.close();
            }
        } finally {
            dw.close();
        }
    }

    private static final boolean CONSTRUCT = true;
    
    private static final boolean DESCRIBE = false;
    
    @Override
    public InputStream sparqlConstructQuery(String query,
            ModelSerializationFormat resultFormat) throws RDFServiceException {
        return getRDFResultStream(query, CONSTRUCT, resultFormat);
    }

    @Override
    public InputStream sparqlDescribeQuery(String query,
            ModelSerializationFormat resultFormat) throws RDFServiceException {
        return getRDFResultStream(query, DESCRIBE, resultFormat);
    }

    @Override
    public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat)
            throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = QueryFactory.create(query);
            QueryExecution qe = QueryExecutionFactory.create(q, d);
            try {
                ResultSet resultSet = qe.execSelect();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
                switch (resultFormat) {
                   case CSV:
                      ResultSetFormatter.outputAsCSV(outputStream,resultSet);
                      break;
                   case TEXT:
                      ResultSetFormatter.out(outputStream,resultSet);
                      break;
                   case JSON:
                      ResultSetFormatter.outputAsJSON(outputStream, resultSet);
                      break;
                   case XML:
                      ResultSetFormatter.outputAsXML(outputStream, resultSet);
                      break;
                   default: 
                      throw new RDFServiceException("unrecognized result format");
                }              
                InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
                return result;
            } finally {
                qe.close();
            }
        } finally {
            dw.close();
        }
    }

    @Override
    public boolean sparqlAskQuery(String query) throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = QueryFactory.create(query);
            QueryExecution qe = QueryExecutionFactory.create(q, d);
            try {
                return qe.execAsk();
            } finally {
                qe.close();
            }
        } finally {
            dw.close();
        }
    }

    @Override
    public List<String> getGraphURIs() throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            List<String> graphURIs = new ArrayList<String>();
            Iterator<String> nameIt = d.listNames();
            while (nameIt.hasNext()) {
                graphURIs.add(nameIt.next());
            }
            return graphURIs;
        } finally {
            dw.close();
        }
    }

    @Override
    public void getGraphMetadata() throws RDFServiceException {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void close() {
        // nothing
    }
    
    private class ModelListener extends StatementListener {
        
        private String graphURI;
        private RDFServiceImpl s;
        
        public ModelListener(String graphURI, RDFServiceImpl s) {
            this.graphURI = graphURI;
            this.s = s;
        }
        
        public void addedStatement(Statement stmt) {
            s.notifyListeners(stmt.asTriple(), ModelChange.Operation.ADD, graphURI);
        }
        
        public void removedStatement(Statement stmt) {
            s.notifyListeners(stmt.asTriple(), ModelChange.Operation.REMOVE, graphURI);
        }
        
    }

}
