/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.ChangeSetImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;

/*
 * API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 */

public class RDFServiceSparql extends RDFServiceImpl implements RDFService {
	
	private static final Log log = LogFactory.getLog(RDFServiceImpl.class);
	private String endpointURI;
	private Repository repository;
	
    /**
     * Returns an RDFService for a remote repository 
     * @param String - URI of the SPARQL endpoint for the knowledge base
     * @param String - URI of the default write graph within the knowledge base.
     *                   this is the graph that will be written to when a graph
     *                   is not explicitly specified.
     * 
     * The default read graph is the union of all graphs in the
     * knowledge base
     */
    public RDFServiceSparql(String endpointURI, String defaultWriteGraphURI) {
        this.endpointURI = endpointURI;
        this.repository = new HTTPRepository(endpointURI);
    }
    	
    public void close() {
        try {
            this.repository.shutDown();
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
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException {
				
	    if (changeSet.getPreconditionQuery() != null 
                && !isPreconditionSatisfied(
                        changeSet.getPreconditionQuery(), 
                                changeSet.getPreconditionQueryType())) {
            return false;
        }
		
		Iterator<ModelChange> csIt = changeSet.getModelChanges().iterator();
		
		while (csIt.hasNext()) {
			
		    ModelChange modelChange = csIt.next();
		    
		    if (modelChange.getOperation() == ModelChange.Operation.ADD) {
		          performAdd(modelChange);	
		    } else if (modelChange.getOperation() == ModelChange.Operation.REMOVE) {
		    	  performRemove(modelChange);
		    } else {
		    	  log.error("unrecognized operation type");
		    }
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
		Query query = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
		
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
		Query query = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
		
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
		
        Query query = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
        
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
		
	    Query query = QueryFactory.create(queryStr);
	    QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
	    
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
		
        List<String> graphNodeList = new ArrayList<String>();
        
        try {
            RepositoryConnection conn = getConnection();
            try {
                RepositoryResult<Resource> conResult = conn.getContextIDs();
                while (conResult.hasNext()) {
                    Resource res = conResult.next();
                    graphNodeList.add(res.stringValue());   
                }
            } finally {
                conn.close();
            }
        } catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
        
        return graphNodeList;		
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
    protected String getEndpointURI() {
        return endpointURI;
    }
    
    protected RepositoryConnection getConnection() {
        try {
            return this.repository.getConnection();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
	
    protected void executeUpdate(String updateString) {    
        try {
            RepositoryConnection conn = getConnection();
            try {
                Update u = conn.prepareUpdate(QueryLanguage.SPARQL, updateString);
                u.execute();
            } catch (MalformedQueryException e) {
                throw new RuntimeException(e);
            } catch (UpdateExecutionException e) {
                log.error(e,e);
                log.error("Update command: \n" + updateString);
                throw new RuntimeException(e);
            } finally {
                conn.close();
            }
        } catch (RepositoryException re) {
            throw new RuntimeException(re);
        }
    }
       
    protected void addTriple(Triple t, String graphURI) {
                
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
    
    protected void removeTriple(Triple t, String graphURI) {
                
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
		
        Query query = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
        
        try {
        	ResultSet resultSet = qe.execSelect();
        	return resultSet.hasNext();
        } finally {
            qe.close();
        }
	}
	
	protected void performAdd(ModelChange modelChange) throws RDFServiceException {
	
		Model model = ModelFactory.createDefaultModel();
		model.read(modelChange.getSerializedModel(),getSerializationFormatString(modelChange.getSerializationFormat()));
		
		StmtIterator stmtIt = model.listStatements();
		
		while (stmtIt.hasNext()) {
		   Statement stmt = stmtIt.next();
		   Triple triple = new Triple(stmt.getSubject().asNode(), stmt.getPredicate().asNode(), stmt.getObject().asNode());
	       addTriple(triple, modelChange.getGraphURI());
		}	
	}
	
	protected void performRemove(ModelChange modelChange) throws RDFServiceException {
		
		Model model = ModelFactory.createDefaultModel();
		model.read(modelChange.getSerializedModel(),getSerializationFormatString(modelChange.getSerializationFormat()));
		
		StmtIterator stmtIt = model.listStatements();
		
		while (stmtIt.hasNext()) {
		   Statement stmt = stmtIt.next();
		   Triple triple = new Triple(stmt.getSubject().asNode(), stmt.getPredicate().asNode(), stmt.getObject().asNode());
	       removeTriple(triple, modelChange.getGraphURI());
		}	
	}

}
