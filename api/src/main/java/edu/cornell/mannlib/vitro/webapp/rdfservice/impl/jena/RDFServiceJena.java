/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.lf5.util.StreamUtils;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sdb.SDB;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.Quad;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ResultSetIterators.ResultSetQuadsIterator;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ResultSetIterators.ResultSetTriplesIterator;

public abstract class RDFServiceJena extends RDFServiceImpl implements RDFService {

    private final static Log log = LogFactory.getLog(RDFServiceJena.class);
        
    protected abstract DatasetWrapper getDatasetWrapper();
    
    @Override
	public abstract boolean changeSetUpdate(ChangeSet changeSet) throws RDFServiceException;
     
    protected void notifyListenersOfPreChangeEvents(ChangeSet changeSet) {
		for (Object o : changeSet.getPreChangeEvents()) {
		    this.notifyListenersOfEvent(o);
		}
	}
    
    protected void insureThatInputStreamsAreResettable(ChangeSet changeSet) throws IOException {
		for (ModelChange modelChange: changeSet.getModelChanges()) {
            if (!modelChange.getSerializedModel().markSupported()) {
                byte[] bytes = IOUtils.toByteArray(modelChange.getSerializedModel());
                modelChange.setSerializedModel(new ByteArrayInputStream(bytes));
            }
            modelChange.getSerializedModel().mark(Integer.MAX_VALUE);
        }
	}

    protected void applyChangeSetToModel(ChangeSet changeSet, Dataset dataset) {
		for (ModelChange modelChange: changeSet.getModelChanges()) {
			dataset.getLock().enterCriticalSection(Lock.WRITE);
			try {
				Model model = (modelChange.getGraphURI() == null) ? 
						dataset.getDefaultModel() : 
						dataset.getNamedModel(modelChange.getGraphURI());
				operateOnModel(model, modelChange, dataset);
			} finally {
				dataset.getLock().leaveCriticalSection();
			}
		}
	}  

    protected void notifyListenersOfPostChangeEvents(ChangeSet changeSet) {
		for (Object o : changeSet.getPostChangeEvents()) {
		    this.notifyListenersOfEvent(o);
		}
	}

    protected void operateOnModel(Model model, ModelChange modelChange, Dataset dataset) {
        model.enterCriticalSection(Lock.WRITE);
        try {
			if (log.isDebugEnabled()) {
				dumpOperation(model, modelChange);
			}
            if (modelChange.getOperation() == ModelChange.Operation.ADD) {
            	Model addition = parseModel(modelChange);
            	model.add(addition);
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
    
	/**
	 * As a debug statement, log info about the model change operation: add or
	 * delete, model URI, model class, punctuation count, beginning of the
	 * string.
	 */
	private void dumpOperation(Model model, ModelChange modelChange) {
		String op = String.valueOf(modelChange.getOperation());

		byte[] changeBytes = new byte[0];
		try {
			modelChange.getSerializedModel().mark(Integer.MAX_VALUE);
			changeBytes = StreamUtils
					.getBytes(modelChange.getSerializedModel());
			modelChange.getSerializedModel().reset();
		} catch (IOException e) {
			// leave it empty.
		}

		int puncCount = 0;
		boolean inUri = false;
		boolean inQuotes = false;
		for (byte b : changeBytes) {
			if (inQuotes) {
				if (b == '"') {
					inQuotes = false;
				}
			} else if (inUri) {
				if (b == '>') {
					inUri = false;
				}
			} else {
				if (b == '"') {
					inQuotes = true;
				} else if (b == '<') {
					inUri = true;
				} else if ((b == ',') || (b == ';') || (b == '.')) {
					puncCount++;
				}
			}
		}

		String changeString = new String(changeBytes).replace('\n', ' ');

		log.debug(String.format(
				">>>>OPERATION: %3.3s %03dpunc, format=%s, graphUri='%s'\n"
						+ "    start=%.200s\n" + "    model=%s",
				modelChange.getOperation(), puncCount,
				modelChange.getSerializationFormat(),
				modelChange.getGraphURI(), changeString,
				ToString.modelToString(model)));
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
                
        if (blankNodeModel.size() == 1) {
            log.warn("Deleting single triple with blank node: " + blankNodeModel);
            log.warn("This likely indicates a problem; excessive data may be deleted.");
        }
        
        Query rootFinderQuery = QueryFactory.create(BNODE_ROOT_QUERY);
        QueryExecution qe = QueryExecutionFactory.create(rootFinderQuery, blankNodeModel);
        try {
            ResultSet rs = qe.execSelect();
            if (!rs.hasNext()) {
                log.warn("No rooted blank node trees; deletion is not possible.");
            }
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                Resource s = qs.getResource("s");
                String treeFinder = makeDescribe(s);
                Query treeFinderQuery = QueryFactory.create(treeFinder);
                QueryExecution qee = QueryExecutionFactory.create(treeFinderQuery, blankNodeModel);
                try {
                    Model tree = qee.execDescribe();
                    Dataset ds = DatasetFactory.createMem();
                    if (graphURI == null) {
                        ds.setDefaultModel(dataset.getDefaultModel());
                    } else {
                        ds.addNamedModel(graphURI, dataset.getNamedModel(graphURI));    
                    }  
                    if (s.isAnon()) {
                        removeUsingSparqlUpdate(ds, tree, graphURI);
                    } else {
                        StmtIterator sit = tree.listStatements(s, null, (RDFNode) null);
                        while (sit.hasNext()) {
                            Statement stmt = sit.nextStatement();
                            RDFNode n = stmt.getObject();
                            Model m2 = ModelFactory.createDefaultModel();
                            if (n.isResource()) {
                                Resource s2 = (Resource) n;
                                // now run yet another describe query
                                String smallerTree = makeDescribe(s2);
                                log.debug(smallerTree);
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
                            removeUsingSparqlUpdate(ds, m2, graphURI);
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
        StmtIterator stmtIt = model.listStatements();
        
        if (!stmtIt.hasNext()) {
            stmtIt.close();
            return;
        }
        
        StringBuffer queryBuff = new StringBuffer();
        queryBuff.append("CONSTRUCT { \n");
        List<Statement> stmts = stmtIt.toList();
        stmts = sort(stmts);
        addStatementPatterns(stmts, queryBuff, !WHERE_CLAUSE);
        queryBuff.append("} WHERE { \n");
        if (graphURI != null) {
            queryBuff.append("    GRAPH <" + graphURI + "> { \n");
        }
        stmtIt = model.listStatements();
        stmts = stmtIt.toList();
        stmts = sort(stmts);
        addStatementPatterns(stmts, queryBuff, WHERE_CLAUSE);
        if (graphURI != null) {
            queryBuff.append("    } \n");
        }
        queryBuff.append("} \n");
        
        log.debug(queryBuff.toString());
        
        Query construct = QueryFactory.create(queryBuff.toString());
        // make a plain dataset to force the query to be run in a way that
        // won't overwhelm MySQL with too many joins
        Dataset ds = DatasetFactory.createMem();
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
    
    private List<Statement> sort(List<Statement> stmts) {
        List<Statement> output = new ArrayList<Statement>();
        int originalSize = stmts.size();
        if(originalSize == 1) {
            return stmts;
        }
        List <Statement> remaining = stmts;
        ConcurrentLinkedQueue<Resource> subjQueue = new ConcurrentLinkedQueue<Resource>();
        for(Statement stmt : remaining) {
            if(stmt.getSubject().isURIResource()) {
                subjQueue.add(stmt.getSubject());
                break;
            }
        }
        if (subjQueue.isEmpty()) {
            log.warn("No named subject in statement patterns");
            return stmts;
        }
        while(remaining.size() > 0) {
            if(subjQueue.isEmpty()) {
                subjQueue.add(remaining.get(0).getSubject());
            }
            while(!subjQueue.isEmpty()) {
                Resource subj = subjQueue.poll();
                List<Statement> temp = new ArrayList<Statement>();
                for (Statement stmt : remaining) {
                    if(stmt.getSubject().equals(subj)) {
                        output.add(stmt);
                        if (stmt.getObject().isResource()) {
                            subjQueue.add((Resource) stmt.getObject()); 
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
    
    private Model parseModel(ModelChange modelChange) {
        Model model = ModelFactory.createDefaultModel();
        model.read(modelChange.getSerializedModel(), null,
                getSerializationFormatString(modelChange.getSerializationFormat()));
        return model;
    }
    
    private InputStream getRDFResultStream(String query, boolean construct, 
            ModelSerializationFormat resultFormat) throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = createQuery(query);
            QueryExecution qe = createQueryExecution(query, q, d);
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

    private void getRDFModel(String query, boolean construct, Model model) throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = createQuery(query);
            QueryExecution qe = createQueryExecution(query, q, d);
            try {
                Model m = construct ? qe.execConstruct(model) : qe.execDescribe(model);
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

    public void sparqlConstructQuery(String query, Model model) throws RDFServiceException {
        getRDFModel(query, CONSTRUCT, model);
    }

    @Override
    public InputStream sparqlDescribeQuery(String query,
            ModelSerializationFormat resultFormat) throws RDFServiceException {
        return getRDFResultStream(query, DESCRIBE, resultFormat);
    }

	/**
	 * TODO Is there a way to accomplish this without buffering the entire result?
	 */
    @Override
    public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat)
            throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = createQuery(query);
            QueryExecution qe = createQueryExecution(query, q, d);
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
    public void sparqlSelectQuery(String query, ResultSetConsumer consumer)
            throws RDFServiceException {
        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            Query q = createQuery(query);
            QueryExecution qe = createQueryExecution(query, q, d);
            try {
                consumer.processResultSet(qe.execSelect());
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
            Query q = createQuery(query);
            QueryExecution qe = createQueryExecution(query, q, d);
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
    	// nothing to do
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
		DatasetWrapper dw = getDatasetWrapper();
		try {
			Dataset d = dw.getDataset();
			Query q = createQuery(query);
			QueryExecution qe = createQueryExecution(query, q, d);
			// These properties only help for SDB, but shouldn't hurt for TDB.
			qe.getContext().set(SDB.jdbcFetchSize, Integer.MIN_VALUE);
			qe.getContext().set(SDB.jdbcStream, true);
			qe.getContext().set(SDB.streamGraphAPI, true);
			try {
				ResultSet resultSet = qe.execSelect();
				if (resultSet.getResultVars().contains("g")) {
					Iterator<Quad> quads = new ResultSetQuadsIterator(resultSet);
					RDFDataMgr.writeQuads(outputStream, quads);
				} else {
					Iterator<Triple> triples = new ResultSetTriplesIterator(resultSet);
					RDFDataMgr.writeTriples(outputStream, triples);
				}
			} finally {
				qe.close();
			}
		} finally {
			dw.close();
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

    /**
     * The basic version. Parse the model from the file, read the model from the
     * tripleStore, and ask whether they are isomorphic.
     */
    @Override
    public boolean isEquivalentGraph(String graphURI, Model graph) throws RDFServiceException {
        // Retrieve the graph to compare
        Model tripleStoreModel = new RDFServiceDataset(this).getNamedModel(graphURI);

        // Load the entire graph into memory (faster comparison)
        Model fromTripleStoreModel = ModelFactory.createDefaultModel().add(tripleStoreModel);

        return graph.isIsomorphicWith(fromTripleStoreModel);
    }

    @Override
    public long countTriples(RDFNode subject, RDFNode predicate, RDFNode object) throws RDFServiceException {
        Query countQuery = QueryFactory.create("SELECT (COUNT(?s) AS ?count) WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o", Syntax.syntaxSPARQL_11);
        QuerySolutionMap map = new QuerySolutionMap();
        if ( subject != null ) {
            map.add("s", subject);
        }
        if ( predicate != null ) {
            map.add("p", predicate);
        }
        if ( object != null ) {
            map.add("o", object);
        }

        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            try (QueryExecution qexec = QueryExecutionFactory.create(countQuery, d, map)) {
                ResultSet results = qexec.execSelect();
                if (results.hasNext()) {
                    QuerySolution soln = results.nextSolution() ;
                    Literal literal = soln.getLiteral("count");
                    return literal.getLong();
                }
            }
        } finally {
            dw.close();
        }

        return 0;
    }

    @Override
    public Model getTriples(RDFNode subject, RDFNode predicate, RDFNode object, long limit, long offset) throws RDFServiceException {
        Query query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }", Syntax.syntaxSPARQL_11);
        QuerySolutionMap map = new QuerySolutionMap();
        if ( subject != null ) {
            map.add("s", subject);
        }
        if ( predicate != null ) {
            map.add("p", predicate);
        }
        if ( object != null ) {
            map.add("o", object);
        }

        query.setOffset(offset);
        query.setLimit(limit);

        Model triples = ModelFactory.createDefaultModel();

        DatasetWrapper dw = getDatasetWrapper();
        try {
            Dataset d = dw.getDataset();
            try (QueryExecution qexec = QueryExecutionFactory.create(query, d, map)) {
                qexec.execConstruct(triples);
            }

            return triples;
        } finally {
            dw.close();
        }
    }


    @Override
    public void close() {
        // nothing
    }
    
    protected QueryExecution createQueryExecution(String queryString, Query q, Dataset d) {
        return QueryExecutionFactory.create(q, d);
    }
}
