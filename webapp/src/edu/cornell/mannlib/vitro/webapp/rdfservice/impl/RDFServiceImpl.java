/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/*
 * API to write, read, and update Vitro's RDF store, with support 
 * to allow listening, logging and auditing.
 */

public class RDFServiceImpl implements RDFService {
	
	private static final Log log = LogFactory.getLog(RDFServiceImpl.class);
	private String endpointURI;
	private Repository repository;
	
    /**
     * Returns an RDFService for a remote repository 
     * @param endpointURI
     */
    public RDFServiceImpl(String endpointURI) {
        this.endpointURI = endpointURI;
        this.repository = new HTTPRepository(endpointURI);
    }
    	
	/**
	 * Perform a series of additions to and or removals from specified graphs
	 * in the RDF store.  preConditionSparql will be executed against the 
	 * union of all the graphs in the knowledge base before any updates are made. 
	 * If the precondition query returns a non-empty result, no updates
	 * will be made. 
	 * 
	 * @param ChangeSet - a set of changes to be performed on the RDF store.
	 * 
	 * @return boolean - indicates whether the precondition was satisfied            
	 */
	@Override
	public boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException {
				
		if (!isPreconditionSatisfied(changeSet.getPreconditionQuery(), changeSet.getPreconditionQueryType())) {
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
	 * If the given individual already exists in the default graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the default
	 * graph.
	 * 
	 * @param String individualURI - URI of the individual to be added
	 * @param String individualTypeURI - URI of the type for the individual
	 */
	@Override
	public void newIndividual(String individualURI, 
			                  String individualTypeURI) throws RDFServiceException {
		
	}

	/**
	 * If the given individual already exists in the given graph, throws an 
	 * RDFServiceException, otherwise adds one type assertion to the given
	 * graph.
	 *
	 * @param String individualURI - URI of the individual to be added
	 * @param String individualTypeURI - URI of the type for the individual
	 * @param String graphURI - URI of the graph to which to add the individual
	 */
	@Override
	public void newIndividual(String individualURI, 
			                  String individualTypeURI, 
			                  String graphURI) throws RDFServiceException {
		
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
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlSelectQuery(String queryStr) throws RDFServiceException {
		
        Query query = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, query);
        
        try {
        	ResultSet resultSet = qe.execSelect();
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
        	ResultSetFormatter.out(outputStream,resultSet);
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
	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
		List<String> list = null;
		
		return list;
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
		String graphURI = null;
		
		return graphURI;
	}
	
	/**
	 * Get the URI of the default read graph
	 * 
	 * @return String URI of default read graph
	 */
	@Override
	public String getDefaultReadGraphURI() throws RDFServiceException {
		String graphURI = null;
		
		return graphURI;
	}
	
	/**
	 * Register a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @return String URI of default read graph
	 */
	@Override
	public void registerListener(ChangeListener changeListener) throws RDFServiceException {
		
	}
	
	/**
	 * Unregister a listener to listen to changes in any graph in
	 * the RDF store.
	 * 
	 * @return String URI of default read graph
	 */
	@Override
	public void unregisterListener(ChangeListener changeListener) throws RDFServiceException {
		
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
	// Non override methods below
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
        
        //log.info("adding " + t);
        
        String updateString = "INSERT DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" )  
                + sparqlNodeUpdate(t.getSubject(), "") + " " 
                + sparqlNodeUpdate(t.getPredicate(), "") + " " 
                + sparqlNodeUpdate(t.getObject(), "") + " } " 
                + ((graphURI != null) ? " } " : "");
        
        //log.info(updateString);
        
        executeUpdate(updateString);
                
    }
    
    protected void removeTriple(Triple t, String graphURI) {
                
        String updateString = "DELETE DATA { " + ((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "" )  
                + sparqlNodeUpdate(t.getSubject(), "") + " " 
                + sparqlNodeUpdate(t.getPredicate(), "") + " " 
                + sparqlNodeUpdate(t.getObject(), "") + " } " 
                + ((graphURI != null) ? " } " : "");
        
        //log.info(updateString);
        
        executeUpdate(updateString);
    }
   
        
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
	
	protected static String getSerializationFormatString(RDFService.ModelSerializationFormat format) {
		switch (format) {
		  case RDFXML: 
		    return "RDFXML";
		  case N3: 
		    return "N3";
		  default: 
		    log.error("unexpected format in getFormatString");
		    return null;
		}
	}

    protected static String sparqlNodeUpdate(Node node, String varName) {
        if (node.isBlank()) {
            return "_:" + node.getBlankNodeLabel().replaceAll("\\W", "");
        } else {
            return sparqlNode(node, varName);
        }
    }

    protected static String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable()) {
            return varName;
        } else if (node.isBlank()) {
            return "<fake:blank>"; // or throw exception?
        } else if (node.isURI()) {
            StringBuffer uriBuff = new StringBuffer();
            return uriBuff.append("<").append(node.getURI()).append(">").toString();
        } else if (node.isLiteral()) {
            StringBuffer literalBuff = new StringBuffer();
            literalBuff.append("\"");
            pyString(literalBuff, node.getLiteralLexicalForm());
            literalBuff.append("\"");
            if (node.getLiteralDatatypeURI() != null) {
                literalBuff.append("^^<").append(node.getLiteralDatatypeURI()).append(">");
            } else if (node.getLiteralLanguage() != null && node.getLiteralLanguage() != "") {
                literalBuff.append("@").append(node.getLiteralLanguage());
            }
            return literalBuff.toString();
        } else {
            return varName;
        }
    }
       
     // see http://www.python.org/doc/2.5.2/ref/strings.html
     // or see jena's n3 grammar jena/src/com/hp/hpl/jena/n3/n3.g  
    protected static void pyString(StringBuffer sbuff, String s)  {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }            

            // Whitespace                        
            if (c == '\n'){ sbuff.append("\\n");continue; }
            if (c == '\t'){ sbuff.append("\\t");continue; }
            if (c == '\r'){ sbuff.append("\\r");continue; }
            if (c == '\f'){ sbuff.append("\\f");continue; }                            
            if (c == '\b'){ sbuff.append("\\b");continue; }
            if( c == 7 )  { sbuff.append("\\a");continue; }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            sbuff.append(c) ;
        }
    }
}
