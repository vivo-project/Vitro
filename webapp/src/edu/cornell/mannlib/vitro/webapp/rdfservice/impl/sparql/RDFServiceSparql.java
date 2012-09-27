/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.ChangeSetImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.ListeningGraph;

/*
 * API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 * 
 */
public class RDFServiceSparql extends RDFServiceImpl implements RDFService {
	
	private static final Log log = LogFactory.getLog(RDFServiceImpl.class);
	private String readEndpointURI;	
	private String updateEndpointURI;
	private HTTPRepository readRepository;
	private HTTPRepository updateRepository;
    private HttpClient httpClient;
	private boolean useSesameContextQuery = true;
	                                            // the number of triples to be 
	private static final int CHUNK_SIZE = 1000; // added/removed in a single
	                                            // SPARQL UPDATE
	
    /**
     * Returns an RDFService for a remote repository 
     * @param String - URI of the read SPARQL endpoint for the knowledge base
     * @param String - URI of the update SPARQL endpoint for the knowledge base
     * @param String - URI of the default write graph within the knowledge base.
     *                   this is the graph that will be written to when a graph
     *                   is not explicitly specified.
     * 
     * The default read graph is the union of all graphs in the
     * knowledge base
     */
    public RDFServiceSparql(String readEndpointURI, String updateEndpointURI, String defaultWriteGraphURI) {
        this.readEndpointURI = readEndpointURI;
        this.updateEndpointURI = updateEndpointURI;
        this.readRepository = new HTTPRepository(readEndpointURI);
        this.updateRepository = new HTTPRepository(updateEndpointURI);

        testConnection();

        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
        mgr.getParams().setDefaultMaxConnectionsPerHost(10);
        this.httpClient = new HttpClient(mgr);
    }
    
    private void testConnection() {
        try {
            this.sparqlSelectQuery(
                    "SELECT ?s WHERE { ?s a " +
                    "<http://vitro.mannlib.cornell.edu/ns/vitro/nonsense/> }", 
                            RDFService.ResultFormat.JSON);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to endpoint at " + 
                    readEndpointURI, e);
        }
    }
 
    /**
     * Returns an RDFService for a remote repository 
     * @param String - URI of the read SPARQL endpoint for the knowledge base
     * @param String - URI of the update SPARQL endpoint for the knowledge base
     * 
     * The default read graph is the union of all graphs in the
     * knowledge base
     */
    public RDFServiceSparql(String readEndpointURI, String updateEndpointURI) {
        this(readEndpointURI, updateEndpointURI, null);
    }
    
    /**
     * Returns an RDFService for a remote repository 
     * @param String - URI of the read and update SPARQL endpoint for the knowledge base
     * 
     * The default read graph is the union of all graphs in the
     * knowledge base
     */
    public RDFServiceSparql(String endpointURI) {
        this(endpointURI, endpointURI, null);
    }
    	
    public void close() {
        try {
            this.readRepository.shutDown();
            this.updateRepository.shutDown();
        } catch (RepositoryException re) {
            log.error(re, re);
        }
    }
    
	/**
	 * Perform a series of additions to and or removals from specified graphs
	 * in the RDF store.  preConditionSparql will be executed against the 
	 * union of all the graphs in the knowledge base before any updates are made. 
	 * If the precondition query returns a non-empty result no updates
	 * will be made. 
	 * 
	 * @param ChangeSet - a set of changes to be performed on the RDF store.
	 * 
	 * @return boolean - indicates whether the precondition was satisfied            
	 */
    @Override
    public boolean changeSetUpdate(ChangeSet changeSet)
            throws RDFServiceException {
             
        if (changeSet.getPreconditionQuery() != null 
                && !isPreconditionSatisfied(
                        changeSet.getPreconditionQuery(), 
                                changeSet.getPreconditionQueryType())) {
            return false;
        }
        
        try {                    
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
                performChange(modelChange);
            }
            
            // notify listeners of triple changes
            csIt = changeSet.getModelChanges().iterator();
            while (csIt.hasNext()) {
                ModelChange modelChange = csIt.next();
                modelChange.getSerializedModel().reset();
                Model model = ModelFactory.createModelForGraph(
                        new ListeningGraph(modelChange.getGraphURI(), this));
                if (modelChange.getOperation() == ModelChange.Operation.ADD) {
                    model.read(modelChange.getSerializedModel(), null, 
                            getSerializationFormatString(
                                    modelChange.getSerializationFormat()));
                } else if (modelChange.getOperation() == ModelChange.Operation.REMOVE){
                    Model temp = ModelFactory.createDefaultModel();
                    temp.read(modelChange.getSerializedModel(), null, 
                            getSerializationFormatString(
                                    modelChange.getSerializationFormat()));
                    model.remove(temp);
                } else {
                    log.error("Unsupported model change type " + 
                            modelChange.getOperation().getClass().getName());
                }
            }
            
            for (Object o : changeSet.getPostChangeEvents()) {
                this.notifyListenersOfEvent(o);
            }
            
        } catch (Exception e) {
            log.error(e, e);
            throw new RDFServiceException(e);
        }        
        return true;
    }
			
	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * @param RDFService.ModelSerializationFormat resultFormat - type of serialization for RDF result of the SPARQL query
	 * @param OutputStream outputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlConstructQuery(String queryStr,
			                                RDFServiceImpl.ModelSerializationFormat resultFormat) throws RDFServiceException {
		
		Model model = ModelFactory.createDefaultModel();
		Query query = createQuery(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);
		
		try {
			qe.execConstruct(model);
		} finally {
			qe.close();
		}

		ByteArrayOutputStream serializedModel = new ByteArrayOutputStream(); 
		model.write(serializedModel,getSerializationFormatString(resultFormat));
		InputStream result = new ByteArrayInputStream(serializedModel.toByteArray());
		return result;
	}
	
	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * @param RDFService.ModelSerializationFormat resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlDescribeQuery(String queryStr,
			                               RDFServiceImpl.ModelSerializationFormat resultFormat) throws RDFServiceException {
		
		Model model = ModelFactory.createDefaultModel();
		Query query = createQuery(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);
		
		try {
			qe.execDescribe(model);
		} finally {
			qe.close();
		}

		ByteArrayOutputStream serializedModel = new ByteArrayOutputStream(); 
		model.write(serializedModel,getSerializationFormatString(resultFormat));
		InputStream result = new ByteArrayInputStream(serializedModel.toByteArray());
		return result;
	}

	/**
	 * Performs a SPARQL select query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * @param RDFService.ResultFormat resultFormat - format for the result of the Select query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlSelectQuery(String queryStr, RDFService.ResultFormat resultFormat) throws RDFServiceException {
		
        Query query = createQuery(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);
        
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
	}
	
	/**
	 * Performs a SPARQL ASK query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param String query - the SPARQL query to be executed against the RDF store
	 * 
	 * @return  boolean - the result of the SPARQL query 
	 */
	@Override
	public boolean sparqlAskQuery(String queryStr) throws RDFServiceException {
		
	    Query query = createQuery(queryStr);
	    QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);
	    
	    try {
	         return qe.execAsk();
	    } finally {
	         qe.close();
	    }
	}
	
	/**
	 * Get a list of all the graph URIs in the RDF store.
	 * 
	 * @return  List<String> - list of all the graph URIs in the RDF store 
	 */
	//TODO - need to verify that the sesame getContextIDs method is implemented
	// in such a way that it works with all triple stores that support the
	// graph update API
	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
        if (!this.useSesameContextQuery) {
            return getGraphURIsFromSparqlQuery();
        } else {
            try {
                return getGraphURIsFromSesameContexts();
            } catch (RepositoryException re) {
                this.useSesameContextQuery = false;
                return getGraphURIsFromSparqlQuery();
            }
        } 
	}
	
	private List<String> getGraphURIsFromSesameContexts() throws RepositoryException {
	    List<String> graphURIs = new ArrayList<String>();
	    RepositoryConnection conn = getReadConnection();
        try {
            RepositoryResult<Resource> conResult = conn.getContextIDs();
            while (conResult.hasNext()) {
                Resource res = conResult.next();
                graphURIs.add(res.stringValue());   
            }
        } finally {
            conn.close();
        }
        return graphURIs;
	}
	
	private List<String> getGraphURIsFromSparqlQuery() throws RDFServiceException {
	    List<String> graphURIs = new ArrayList<String>();
	    try {
            String graphURIString = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g";
            ResultSet rs = ResultSetFactory.fromJSON(
                    sparqlSelectQuery(graphURIString, RDFService.ResultFormat.JSON));
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                graphURIs.add(qs.getResource("g").getURI());
            }
        } catch (Exception e) {
            throw new RDFServiceException("Unable to list graph URIs", e);
        }
	    return graphURIs;
	}

	/**
	 * TODO - what is the definition of this method?
	 * @return 
	 */
	@Override
	public void getGraphMetadata() throws RDFServiceException {
		
	}
		
	/**
	 * Get the URI of the default write graph
	 * 
	 * @return String URI of default write graph
	 */
	@Override
	public String getDefaultWriteGraphURI() throws RDFServiceException {
        return defaultWriteGraphURI;
	}
		
	/**
	 * Register a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 */
	@Override
	public synchronized void registerListener(ChangeListener changeListener) throws RDFServiceException {
		
		if (!registeredListeners.contains(changeListener)) {
		   registeredListeners.add(changeListener);
		}
	}
	
	/**
	 * Unregister a listener from listening to changes in any graph
	 * in the RDF store.
	 * 
	 */
	@Override
	public synchronized void unregisterListener(ChangeListener changeListener) throws RDFServiceException {
		registeredListeners.remove(changeListener);
	}

	/**
	 * Create a ChangeSet object
	 * 
	 * @return a ChangeSet object
	 */
	@Override
	public ChangeSet manufactureChangeSet() {
		return new ChangeSetImpl();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Non-override methods below
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    protected String getReadEndpointURI() {
        return readEndpointURI;
    }
    
    protected String getUpdateEndpointURI() {
        return updateEndpointURI;
    }
    
    protected RepositoryConnection getReadConnection() {
        try {
            return this.readRepository.getConnection();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected RepositoryConnection getWriteConnection() {
        try {
            return this.updateRepository.getConnection();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void executeUpdate(String updateString) throws RDFServiceException {  
        try {
            PostMethod meth = new PostMethod(updateEndpointURI);
            try {
                meth.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                NameValuePair[] body = new NameValuePair[1];
                body[0] = new NameValuePair("update", updateString);
                meth.setRequestBody(body);
                int response = httpClient.executeMethod(meth);
                if (response > 399) {
                    log.error("response " + response + " to update. \n");
                    log.debug("update string: \n" + updateString);
                    throw new RDFServiceException("Unable to perform SPARQL UPDATE");
                }
            } finally {
                meth.releaseConnection();
            } 
        } catch (Exception e) {
            throw new RDFServiceException("Unable to perform change set update", e);
        } 
    }
       
    public void addModel(Model model, String graphURI) throws RDFServiceException {
        verbModel(model, graphURI, "INSERT");
    }
    
    public void deleteModel(Model model, String graphURI) throws RDFServiceException {
        verbModel(model, graphURI, "DELETE");
    }
    
    private void verbModel(Model model, String graphURI, String verb) throws RDFServiceException {
        Model m = ModelFactory.createDefaultModel();
        StmtIterator stmtIt = model.listStatements();
        int count = 0;
        try {
            while (stmtIt.hasNext()) {
                count++;
                m.add(stmtIt.nextStatement());
                if (count % CHUNK_SIZE == 0 || !stmtIt.hasNext()) {
                    StringWriter sw = new StringWriter();
                    m.write(sw, "N-TRIPLE");
                    StringBuffer updateStringBuff = new StringBuffer();
                    updateStringBuff.append(verb + " DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" ));
                    updateStringBuff.append(sw);
                    updateStringBuff.append(((graphURI != null) ? " } " : "") + " }");
                    
                    String updateString = updateStringBuff.toString();
                    
                    executeUpdate(updateString);

                    m.removeAll();
                }
            }
        } finally {
            stmtIt.close();
        }
    }
    
    protected void addTriple(Triple t, String graphURI) throws RDFServiceException {
                
        StringBuffer updateString = new StringBuffer();
        updateString.append("INSERT DATA { ");
        updateString.append((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" );
        updateString.append(sparqlNodeUpdate(t.getSubject(), ""));
        updateString.append(" ");
        updateString.append(sparqlNodeUpdate(t.getPredicate(), ""));
        updateString.append(" ");
        updateString.append(sparqlNodeUpdate(t.getObject(), ""));
        updateString.append(" }");
        updateString.append((graphURI != null) ? " } " : "");
        
        executeUpdate(updateString.toString());
        notifyListeners(t, ModelChange.Operation.ADD, graphURI);
    }
    
    protected void removeTriple(Triple t, String graphURI) throws RDFServiceException {
                
        StringBuffer updateString = new StringBuffer();
        updateString.append("DELETE DATA { ");
        updateString.append((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" );
        updateString.append(sparqlNodeUpdate(t.getSubject(), ""));
        updateString.append(" ");
        updateString.append(sparqlNodeUpdate(t.getPredicate(), ""));
        updateString.append(" ");
        updateString.append(sparqlNodeUpdate(t.getObject(), ""));
        updateString.append(" }");
        updateString.append((graphURI != null) ? " } " : "");
                
        executeUpdate(updateString.toString());
        notifyListeners(t, ModelChange.Operation.REMOVE, graphURI);
    }
    
    @Override
	protected boolean isPreconditionSatisfied(String query, 
			                                  RDFService.SPARQLQueryType queryType)
			                                		  throws RDFServiceException {
		Model model = ModelFactory.createDefaultModel();
		
		switch (queryType) {
		   case DESCRIBE:
			   model.read(sparqlDescribeQuery(query,RDFService.ModelSerializationFormat.N3), null);
			   return !model.isEmpty();
		   case CONSTRUCT:
			   model.read(sparqlConstructQuery(query,RDFService.ModelSerializationFormat.N3), null);
			   return !model.isEmpty();
		   case SELECT:
			   return sparqlSelectQueryHasResults(query);
		   case ASK:
			   return sparqlAskQuery(query);
		   default:
			  throw new RDFServiceException("unrecognized SPARQL query type");	
		}		
	}
    
	@Override
	protected boolean sparqlSelectQueryHasResults(String queryStr) throws RDFServiceException {
		
        Query query = createQuery(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);
        
        try {
        	ResultSet resultSet = qe.execSelect();
        	return resultSet.hasNext();
        } finally {
            qe.close();
        }
	}
	
    private void performChange(ModelChange modelChange) throws RDFServiceException {
        Model model = parseModel(modelChange);
        if (modelChange.getOperation() == ModelChange.Operation.ADD) {
            Model[] separatedModel = separateStatementsWithBlankNodes(model);
            addModel(separatedModel[1], modelChange.getGraphURI());
            addBlankNodesWithSparqlUpdate(separatedModel[0], modelChange.getGraphURI());
        } else if (modelChange.getOperation() == ModelChange.Operation.REMOVE) {
            deleteModel(model, modelChange.getGraphURI());
            removeBlankNodesWithSparqlUpdate(model, modelChange.getGraphURI());
        } else {
            log.error("unrecognized operation type");
        }         
    }
    
    private void addBlankNodesWithSparqlUpdate(Model model, String graphURI) 
            throws RDFServiceException {
        updateBlankNodesWithSparqlUpdate(model, graphURI, ADD);
    }
    
    private void removeBlankNodesWithSparqlUpdate(Model model, String graphURI) 
            throws RDFServiceException {
        updateBlankNodesWithSparqlUpdate(model, graphURI, REMOVE);
    }
    
    private static final boolean ADD = true;
    private static final boolean REMOVE = false;
    
    private void updateBlankNodesWithSparqlUpdate(Model model, String graphURI, boolean add) 
            throws RDFServiceException {
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
        
        
        
        log.debug("update model size " + model.size());       
        log.debug("blank node model size " + blankNodeModel.size());
        
        if (!add && blankNodeModel.size() == 1) {
            log.warn("Deleting single triple with blank node: " + blankNodeModel);
            log.warn("This likely indicates a problem; excessive data may be deleted.");
        }
                
        Query rootFinderQuery = QueryFactory.create(BNODE_ROOT_QUERY);
        QueryExecution qe = QueryExecutionFactory.create(rootFinderQuery, blankNodeModel);
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                com.hp.hpl.jena.rdf.model.Resource s = qs.getResource("s");
                String treeFinder = makeDescribe(s);
                Query treeFinderQuery = QueryFactory.create(treeFinder);
                QueryExecution qee = QueryExecutionFactory.create(treeFinderQuery, blankNodeModel);
                try {
                    Model tree = qee.execDescribe();
                    if (s.isAnon()) {
                        if (add) {
                            addModel(tree, graphURI);
                        } else {
                            removeUsingSparqlUpdate(tree, graphURI);
                        }
                    } else {
                        StmtIterator sit = tree.listStatements(s, null, (RDFNode) null);
                        while (sit.hasNext()) {
                            Statement stmt = sit.nextStatement();
                            RDFNode n = stmt.getObject();
                            Model m2 = ModelFactory.createDefaultModel();
                            if (n.isResource()) {
                                com.hp.hpl.jena.rdf.model.Resource s2 = 
                                        (com.hp.hpl.jena.rdf.model.Resource) n;
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
                            if (add) {
                                addModel(m2, graphURI);
                            } else {
                                removeUsingSparqlUpdate(m2, graphURI);
                            }
                        }     
                    }
                } finally {
                    qee.close();
                }
            }
        } finally {
            qe.close();
        }        
    }
    
    private void removeUsingSparqlUpdate(Model model, String graphURI)
            throws RDFServiceException {
        
        StmtIterator stmtIt = model.listStatements();
        
        if (!stmtIt.hasNext()) {
            stmtIt.close();
            return;
        }
            
        StringBuffer queryBuff = new StringBuffer();
        queryBuff.append("DELETE { \n");
        if (graphURI != null) {
            queryBuff.append("    GRAPH <" + graphURI + "> { \n");
        }
        addStatementPatterns(stmtIt, queryBuff, !WHERE_CLAUSE);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} WHERE { \n");
        if (graphURI != null) {
            queryBuff.append("    GRAPH <" + graphURI + "> { \n");
        }
        stmtIt = model.listStatements();
        addStatementPatterns(stmtIt, queryBuff, WHERE_CLAUSE);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} \n");
        
        if(log.isDebugEnabled()) {
            log.debug(queryBuff.toString());
        }
        executeUpdate(queryBuff.toString());
    }
    
    private static final boolean WHERE_CLAUSE = true;
    
    private void addStatementPatterns(StmtIterator stmtIt, StringBuffer patternBuff, boolean whereClause) {
        while(stmtIt.hasNext()) {
            Triple t = stmtIt.next().asTriple();
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getSubject(), null));
            patternBuff.append(" ");
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getPredicate(), null));
            patternBuff.append(" ");
            patternBuff.append(SparqlGraph.sparqlNodeDelete(t.getObject(), null));
            patternBuff.append(" .\n");
            if (whereClause) {
                if (t.getSubject().isBlank()) {
                    patternBuff.append("    FILTER(isBlank(" + SparqlGraph.sparqlNodeDelete(t.getSubject(), null)).append(")) \n");
                }
                if (t.getObject().isBlank()) {
                    patternBuff.append("    FILTER(isBlank(" + SparqlGraph.sparqlNodeDelete(t.getObject(), null)).append(")) \n");
                }
            }
        }
    }
    
    private String makeDescribe(com.hp.hpl.jena.rdf.model.Resource s) {
        StringBuffer query = new StringBuffer("DESCRIBE <") ;
        if (s.isAnon()) {
            query.append("_:" + s.getId().toString());
        } else {
            query.append(s.getURI());
        }
        query.append(">");
        return query.toString();
    }
    
    private Model parseModel(ModelChange modelChange) {
        Model model = ModelFactory.createDefaultModel();
        model.read(modelChange.getSerializedModel(), null,
                getSerializationFormatString(modelChange.getSerializationFormat()));
        return model;
    }
}
