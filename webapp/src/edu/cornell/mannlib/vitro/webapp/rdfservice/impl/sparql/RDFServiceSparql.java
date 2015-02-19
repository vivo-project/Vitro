/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.riot.RDFDataMgr;

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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.ChangeSetImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.ListeningGraph;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ResultSetIterators.ResultSetQuadsIterator;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ResultSetIterators.ResultSetTriplesIterator;

/*
 * API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 * 
 */
public class RDFServiceSparql extends RDFServiceImpl implements RDFService {
	
	private static final Log log = LogFactory.getLog(RDFServiceImpl.class);
	protected String readEndpointURI;	
	protected String updateEndpointURI;
    private CloseableHttpClient httpClient;
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

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(50);
        this.httpClient = HttpClients.custom().setConnectionManager(cm).build();
        
        testConnection();
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
        // nothing for now
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
		} catch (Exception e) {
		    log.error("Error executing CONSTRUCT against remote endpoint: " + queryStr);
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
			    
        //QueryEngineHTTP qh = new QueryEngineHTTP(readEndpointURI, queryStr);
		
        try {
        	HttpGet meth = new HttpGet(new URIBuilder(readEndpointURI).addParameter("query", queryStr).build());
            meth.addHeader("Accept", "application/sparql-results+xml");
            CloseableHttpResponse response = httpClient.execute(meth);
            try {
	            int statusCode = response.getStatusLine().getStatusCode();
	            if (statusCode > 399) {
	                log.error("response " + statusCode + " to query. \n");
	                log.debug("update string: \n" + queryStr);
	                throw new RDFServiceException("Unable to perform SPARQL UPDATE");
	            }
	        
	            InputStream in = response.getEntity().getContent(); 
	            ResultSet resultSet = ResultSetFactory.fromXML(in);
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
            	response.close();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (URISyntaxException e) {
        	throw new RuntimeException(e);
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
	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
        return getGraphURIsFromSparqlQuery();
	}
	
	private List<String> getGraphURIsFromSparqlQuery() throws RDFServiceException {	    
	    String fastJenaQuery = "SELECT DISTINCT ?g WHERE { GRAPH ?g {} } ORDER BY ?g";
	    String standardQuery = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g";
	    List<String> graphURIs = new ArrayList<String>();
	    try {
	        graphURIs = getGraphURIsFromSparqlQuery(fastJenaQuery);
	    } catch (Exception e) {
	        log.debug("Unable to use non-standard ARQ query for graph list", e);
	    }
	    if (graphURIs.isEmpty()) {
	        graphURIs = getGraphURIsFromSparqlQuery(standardQuery);
	    }
	    return graphURIs;
	}
	
	private List<String> getGraphURIsFromSparqlQuery(String queryString) throws RDFServiceException {
	    List<String> graphURIs = new ArrayList<String>();
	    try {
            
            ResultSet rs = ResultSetFactory.fromJSON(
                    sparqlSelectQuery(queryString, RDFService.ResultFormat.JSON));
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                if (qs != null) { // no idea how this happens, but it seems to 
                    RDFNode n = qs.getResource("g");
                    if (n != null && n.isResource()) {
                        graphURIs.add(((Resource) n).getURI());
                    }
                }
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
    
    protected void executeUpdate(String updateString) throws RDFServiceException {  
        try {
            HttpPost meth = new HttpPost(updateEndpointURI);
            meth.addHeader("Content-Type", "application/x-www-form-urlencoded");
            meth.setEntity(new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair("update", updateString))));
            CloseableHttpResponse response = httpClient.execute(meth);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode > 399) {
                    log.error("response " + statusCode + " to update. \n");
                    //log.debug("update string: \n" + updateString);
                    throw new RDFServiceException("Unable to perform SPARQL UPDATE");
                }
            } finally {
                response.close();
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
        Model[] separatedModel = separateStatementsWithBlankNodes(model);
        if (modelChange.getOperation() == ModelChange.Operation.ADD) {
            addModel(separatedModel[1], modelChange.getGraphURI());
            addBlankNodesWithSparqlUpdate(separatedModel[0], modelChange.getGraphURI());
        } else if (modelChange.getOperation() == ModelChange.Operation.REMOVE) {
            deleteModel(separatedModel[1], modelChange.getGraphURI());
            removeBlankNodesWithSparqlUpdate(separatedModel[0], modelChange.getGraphURI());
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
        List<Statement> stmts = stmtIt.toList();
        sort(stmts);
        addStatementPatterns(stmts, queryBuff, !WHERE_CLAUSE);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} WHERE { \n");
        if (graphURI != null) {
            queryBuff.append("    GRAPH <" + graphURI + "> { \n");
        }
        stmtIt = model.listStatements();
        stmts = stmtIt.toList();
        sort(stmts);
        addStatementPatterns(stmts, queryBuff, WHERE_CLAUSE);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} \n");
        
        if(log.isDebugEnabled()) {
            log.debug(queryBuff.toString());
        }
        executeUpdate(queryBuff.toString());
    }
    
    private List<Statement> sort(List<Statement> stmts) {
        List<Statement> output = new ArrayList<Statement>();
        int originalSize = stmts.size();
        if (originalSize == 1) 
            return stmts;
        List <Statement> remaining = stmts;
        ConcurrentLinkedQueue<com.hp.hpl.jena.rdf.model.Resource> subjQueue = 
                new ConcurrentLinkedQueue<com.hp.hpl.jena.rdf.model.Resource>();
        for(Statement stmt : remaining) {
            if(stmt.getSubject().isURIResource()) {
                subjQueue.add(stmt.getSubject());
                break;
            }
        }
        if (subjQueue.isEmpty()) {
            throw new RuntimeException("No named subject in statement patterns");
        }
        while(remaining.size() > 0) {
            if(subjQueue.isEmpty()) {
                subjQueue.add(remaining.get(0).getSubject());
            }
            while(!subjQueue.isEmpty()) {
                com.hp.hpl.jena.rdf.model.Resource subj = subjQueue.poll();
                List<Statement> temp = new ArrayList<Statement>();
                for (Statement stmt : remaining) {
                    if(stmt.getSubject().equals(subj)) {
                        output.add(stmt);
                        if (stmt.getObject().isResource()) {
                            subjQueue.add((com.hp.hpl.jena.rdf.model.Resource) stmt.getObject()); 
                        }
                    } else {
                        temp.add(stmt);
                    }
                }
                remaining = temp;
            }
        }
        if(output.size() != originalSize) {
            throw new RuntimeException("original list size was " + originalSize + 
                    " but sorted size is " + output.size());      
        }
        return output;
    }
    
    private static final boolean WHERE_CLAUSE = true;
    
    private void addStatementPatterns(List<Statement> stmts, StringBuffer patternBuff, boolean whereClause) {
        for(Statement stmt : stmts) {
            Triple t = stmt.asTriple();
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

    @Override
	public void serializeAll(OutputStream outputStream)
			throws RDFServiceException {
    	String query = "SELECT * WHERE { GRAPH ?g {?s ?p ?o}}";
		serialize(outputStream, query);
	}

	@Override
	public void serializeGraph(String graphURI, OutputStream outputStream)
			throws RDFServiceException {
		String query = "SELECT * WHERE { GRAPH <" + graphURI + "> {?s ?p ?o}}";
		serialize(outputStream, query);
	}

	private void serialize(OutputStream outputStream, String query) throws RDFServiceException {
        InputStream resultStream = sparqlSelectQuery(query, RDFService.ResultFormat.JSON);
        ResultSet resultSet = ResultSetFactory.fromJSON(resultStream);
		if (resultSet.getResultVars().contains("g")) {
			Iterator<Quad> quads = new ResultSetQuadsIterator(resultSet);
			RDFDataMgr.writeQuads(outputStream, quads);
		} else {
			Iterator<Triple> triples = new ResultSetTriplesIterator(resultSet);
			RDFDataMgr.writeTriples(outputStream, triples);
		}
	}

	/**
	 * The basic version. Parse the model from the file, read the model from the
	 * tripleStore, and ask whether they are isomorphic.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI, InputStream serializedGraph, 
			ModelSerializationFormat serializationFormat) throws RDFServiceException {
		Model fileModel = RDFServiceUtils.parseModel(serializedGraph, serializationFormat);
		Model tripleStoreModel = new RDFServiceDataset(this).getNamedModel(graphURI);
		Model fromTripleStoreModel = ModelFactory.createDefaultModel().add(tripleStoreModel);
		return fileModel.isIsomorphicWith(fromTripleStoreModel);
	}

}
