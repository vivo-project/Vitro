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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.RDFDataMgr;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Quad;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.ChangeSetImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.http.HttpClientFactory;
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
	// the number of triples to be
	private static final int CHUNK_SIZE = 5000; // added/removed in a single
	// SPARQL UPDATE

	protected HttpClient httpClient;

	protected boolean rebuildGraphURICache = true;
	private List<String> graphURIs = null;

	/**
	 * Returns an RDFService for a remote repository
	 * @param readEndpointURI - URI of the read SPARQL endpoint for the knowledge base
	 * @param updateEndpointURI - URI of the update SPARQL endpoint for the knowledge base
	 * @param defaultWriteGraphURI - URI of the default write graph within the knowledge base.
	 *                   this is the graph that will be written to when a graph
	 *                   is not explicitly specified.
	 *
	 * The default read graph is the union of all graphs in the
	 * knowledge base
	 */
	public RDFServiceSparql(String readEndpointURI, String updateEndpointURI, String defaultWriteGraphURI) {
		this.readEndpointURI = readEndpointURI;
		this.updateEndpointURI = updateEndpointURI;
		httpClient = HttpClientFactory.getHttpClient();

		if (RDFServiceSparql.class.getName().equals(this.getClass().getName())) {
			testConnection();
		}
	}

	protected void testConnection() {
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
	 * @param readEndpointURI - URI of the read SPARQL endpoint for the knowledge base
	 * @param updateEndpointURI - URI of the update SPARQL endpoint for the knowledge base
	 *
	 * The default read graph is the union of all graphs in the
	 * knowledge base
	 */
	public RDFServiceSparql(String readEndpointURI, String updateEndpointURI) {
		this(readEndpointURI, updateEndpointURI, null);
	}

	/**
	 * Returns an RDFService for a remote repository
	 * @param endpointURI - URI of the read and update SPARQL endpoint for the knowledge base
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
	 * @param changeSet - a set of changes to be performed on the RDF store.
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
			
			notifyListenersOfChanges(changeSet);						

			for (Object o : changeSet.getPostChangeEvents()) {
				this.notifyListenersOfEvent(o);
			}

		} catch (Exception e) {
			log.error(e, e);
			throw new RDFServiceException(e);
		} finally {
			rebuildGraphURICache = true;
		}
		return true;
	}

	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 *
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
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

	public void sparqlConstructQuery(String queryStr, Model model) throws RDFServiceException {

		Query query = createQuery(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, query);

		try {
			qe.execConstruct(model);
		} catch (Exception e) {
			log.error("Error executing CONSTRUCT against remote endpoint: " + queryStr);
		} finally {
			qe.close();
		}
	}

	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 *
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
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
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - format for the result of the Select query
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
			HttpContext context = getContext(meth);
			HttpResponse response = context != null ? httpClient.execute(meth, context) : httpClient.execute(meth);
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode > 399) {
					log.error("response " + statusCode + " to query. \n");
					log.debug("update string: \n" + queryStr);
					throw new RDFServiceException("Unable to perform SPARQL SELECT");
				}

				try (InputStream in = response.getEntity().getContent()) {
					ResultSet resultSet = ResultSetFactory.fromXML(in);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					switch (resultFormat) {
						case CSV:
							ResultSetFormatter.outputAsCSV(outputStream, resultSet);
							break;
						case TEXT:
							ResultSetFormatter.out(outputStream, resultSet);
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
					InputStream result = new ByteArrayInputStream(
							outputStream.toByteArray());
					return result;
				}
			} finally {
				EntityUtils.consume(response.getEntity());
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void sparqlSelectQuery(String queryStr, ResultSetConsumer consumer) throws RDFServiceException {

		//QueryEngineHTTP qh = new QueryEngineHTTP(readEndpointURI, queryStr);

		try {
			HttpGet meth = new HttpGet(new URIBuilder(readEndpointURI).addParameter("query", queryStr).build());
			meth.addHeader("Accept", "application/sparql-results+xml");
			HttpContext context = getContext(meth);
			HttpResponse response = context != null ? httpClient.execute(meth, context) : httpClient.execute(meth);
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode > 399) {
					log.error("response " + statusCode + " to query. \n");
					log.debug("update string: \n" + queryStr);
					throw new RDFServiceException("Unable to perform SPARQL UPDATE");
				}

				try (InputStream in = response.getEntity().getContent()) {
					consumer.processResultSet(ResultSetFactory.fromXML(in));
				}
			} finally {
				EntityUtils.consume(response.getEntity());
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
	 * @param queryStr - the SPARQL query to be executed against the RDF store
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
	 */
	@Override
	public List<String> getGraphURIs() throws RDFServiceException {
		if (graphURIs == null || rebuildGraphURICache) {
			graphURIs = getGraphURIsFromSparqlQuery();
			rebuildGraphURICache = false;
		}
		return graphURIs;
	}

	private List<String> getGraphURIsFromSparqlQuery() throws RDFServiceException {
		String fastJenaQuery = "SELECT DISTINCT ?g WHERE { GRAPH ?g {} } ORDER BY ?g";
		String standardQuery = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }";
		List<String> graphURIs = new ArrayList<String>();
		try {
			graphURIs = getGraphURIsFromSparqlQuery(fastJenaQuery);
		} catch (Exception e) {
			log.debug("Unable to use non-standard ARQ query for graph list", e);
		}
		if (graphURIs.isEmpty()) {
			graphURIs = getGraphURIsFromSparqlQuery(standardQuery);
			Collections.sort(graphURIs);
		}
		return graphURIs;
	}

	private List<String> getGraphURIsFromSparqlQuery(String queryString) throws RDFServiceException {
		final List<String> graphURIs = new ArrayList<String>();
		try {
			sparqlSelectQuery(queryString, new ResultSetConsumer() {
				@Override
				protected void processQuerySolution(QuerySolution qs) {
					if (qs != null) { // no idea how this happens, but it seems to
						RDFNode n = qs.getResource("g");
						if (n != null && n.isResource()) {
							graphURIs.add(((Resource) n).getURI());
						}
					}
				}
			});
		} catch (Exception e) {
			throw new RDFServiceException("Unable to list graph URIs", e);
		}
		return graphURIs;
	}

	/**
	 */
	@Override
	public void getGraphMetadata() throws RDFServiceException {
	    throw new UnsupportedOperationException();
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
			HttpContext context = getContext(meth);
			HttpResponse response = context != null ? httpClient.execute(meth, context) : httpClient.execute(meth);
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode > 399) {
					log.error("response " + response.getStatusLine() + " to update. \n");
					//log.debug("update string: \n" + updateString);
					throw new RDFServiceException("Unable to perform SPARQL UPDATE");
				}
			} finally {
				EntityUtils.consume(response.getEntity());
			}
		} catch (Exception e) {
			throw new RDFServiceException("Unable to perform change set update", e);
		}
	}

	public void addModel(Model model, String graphURI) throws RDFServiceException {
		try {
		    long start = System.currentTimeMillis();
			verbModel(model, graphURI, "INSERT");
			log.info((System.currentTimeMillis() - start) + " ms to insert " + model.size() + " triples");
		} finally {
			rebuildGraphURICache = true;
		}
	}

	public void deleteModel(Model model, String graphURI) throws RDFServiceException {
		try {
			verbModel(model, graphURI, "DELETE");
		} finally {
			rebuildGraphURICache = true;
		}
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

//	protected void addTriple(Triple t, String graphURI) throws RDFServiceException {
//		try {
//			StringBuffer updateString = new StringBuffer();
//			updateString.append("INSERT DATA { ");
//			updateString.append((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "");
//			updateString.append(sparqlNodeUpdate(t.getSubject(), ""));
//			updateString.append(" ");
//			updateString.append(sparqlNodeUpdate(t.getPredicate(), ""));
//			updateString.append(" ");
//			updateString.append(sparqlNodeUpdate(t.getObject(), ""));
//			updateString.append(" }");
//			updateString.append((graphURI != null) ? " } " : "");
//
//			executeUpdate(updateString.toString());
//			notifyListeners(t, ModelChange.Operation.ADD, graphURI);
//		} finally {
//			rebuildGraphURICache = true;
//		}
//	}
//
//	protected void removeTriple(Triple t, String graphURI) throws RDFServiceException {
//		try {
//			StringBuffer updateString = new StringBuffer();
//			updateString.append("DELETE DATA { ");
//			updateString.append((graphURI != null) ? "GRAPH <" + graphURI + "> { " : "");
//			updateString.append(sparqlNodeUpdate(t.getSubject(), ""));
//			updateString.append(" ");
//			updateString.append(sparqlNodeUpdate(t.getPredicate(), ""));
//			updateString.append(" ");
//			updateString.append(sparqlNodeUpdate(t.getObject(), ""));
//			updateString.append(" }");
//			updateString.append((graphURI != null) ? " } " : "");
//
//			executeUpdate(updateString.toString());
//			notifyListeners(t, ModelChange.Operation.REMOVE, graphURI);
//		} finally {
//			rebuildGraphURICache = true;
//		}
//	}

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
				org.apache.jena.rdf.model.Resource s = qs.getResource("s");
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
								org.apache.jena.rdf.model.Resource s2 =
										(org.apache.jena.rdf.model.Resource) n;
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
		if (graphURI != null) {
			queryBuff.append("WITH <" + graphURI + "> \n");
		}
		queryBuff.append("DELETE { \n");
		List<Statement> stmts = stmtIt.toList();
		sort(stmts);
		addStatementPatterns(stmts, queryBuff, !WHERE_CLAUSE);
		queryBuff.append("} WHERE { \n");
		stmtIt = model.listStatements();
		stmts = stmtIt.toList();
		sort(stmts);
		addStatementPatterns(stmts, queryBuff, WHERE_CLAUSE);
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
		ConcurrentLinkedQueue<org.apache.jena.rdf.model.Resource> subjQueue =
				new ConcurrentLinkedQueue<org.apache.jena.rdf.model.Resource>();
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
				org.apache.jena.rdf.model.Resource subj = subjQueue.poll();
				List<Statement> temp = new ArrayList<Statement>();
				for (Statement stmt : remaining) {
					if(stmt.getSubject().equals(subj)) {
						output.add(stmt);
						if (stmt.getObject().isResource()) {
							subjQueue.add((org.apache.jena.rdf.model.Resource) stmt.getObject());
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

	private String makeDescribe(org.apache.jena.rdf.model.Resource s) {
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

	/**
	 * The basic version. Parse the model from the file, read the model from the
	 * tripleStore, and ask whether they are isomorphic.
	 */
	@Override
	public boolean isEquivalentGraph(String graphURI, Model graph) throws RDFServiceException {
		Model tripleStoreModel = new RDFServiceDataset(this).getNamedModel(graphURI);
		Model fromTripleStoreModel = ModelFactory.createDefaultModel().add(tripleStoreModel);
		return graph.isIsomorphicWith(fromTripleStoreModel);
	}

	protected HttpContext getContext(HttpRequestBase request) {
		UsernamePasswordCredentials credentials = getCredentials();
		if (credentials != null) {
			try {
				request.addHeader(new BasicScheme().authenticate(credentials, request, null));

				CredentialsProvider provider = new BasicCredentialsProvider();
				provider.setCredentials(AuthScope.ANY, getCredentials());

				BasicHttpContext context = new BasicHttpContext();
				context.setAttribute(ClientContext.CREDS_PROVIDER, provider);
				return context;
			} catch (AuthenticationException e) {
				log.error("Unable to set credentials");
			}
		}

		return new BasicHttpContext();
	}

	protected UsernamePasswordCredentials getCredentials() {
		return null;
	}
}
