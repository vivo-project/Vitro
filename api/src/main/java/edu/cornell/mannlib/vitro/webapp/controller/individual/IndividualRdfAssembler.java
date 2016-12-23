/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish.PublishObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RdfResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.ExtendedLinkedDataUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * Write a smaller set of Linked Data. It consists of:
 * 
 * 1) The data properties of the entity
 * 
 * 2) The object properties in which the entity is either subject or object
 * 
 * 3) The labels and types of the objects that are linked by those properties.
 * 
 * If the request comes with an Accept-language header, use an appropriately
 * language-aware data source to filter the data properties and labels.
 * Otherwise, show all triples, regardless of language.
 * 
 * Filter the result based on the policy, removing any triples that should not
 * be published to the public (or to the user, if logged in). Also remove any
 * objects which can only be reached by excluded triples.
 * 
 * ----------------
 * 
 * This still permits the use of rich export, by "include" options on the
 * request. The only difference from earlier implementations is that the result
 * may be made language-aware.
 */
public class IndividualRdfAssembler {
	private static final Log log = LogFactory
			.getLog(IndividualRdfAssembler.class);

	private static final String RICH_EXPORT_ROOT = "/WEB-INF/rich-export/";
	private static final String INCLUDE_ALL = "all";

	private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	private static final String URI_RIGHTS = NS_DC + "rights";
	private static final String URI_DATE = NS_DC + "date";
	private static final String URI_PUBLISHER = NS_DC + "publisher";

	private static final String NS_FOAF = "http://xmlns.com/foaf/0.1/";
	private static final String URI_DOCUMENT = NS_FOAF + "Document";

	private static final String URI_LABEL = VitroVocabulary.RDFS + "label";
	private static final String URI_TYPE = VitroVocabulary.RDF_TYPE;

	private final VitroRequest vreq;
	private final ServletContext ctx;
	private final String individualUri;
	private final ContentType rdfFormat;
	private final String[] richExportIncludes;
	private final RDFService rdfService;
	private final OntModel contentModel;
	private final WebappDaoFactory wadf;

	public IndividualRdfAssembler(VitroRequest vreq, String individualUri,
			ContentType rdfFormat) {
		this.vreq = vreq;
		this.ctx = vreq.getSession().getServletContext();

		this.individualUri = individualUri;
		this.rdfFormat = rdfFormat;
		String[] includes = vreq.getParameterValues("include");
		this.richExportIncludes = (includes == null) ? new String[0] : includes;

		if (isLanguageAware()) {
			this.rdfService = vreq.getRDFService();
			this.contentModel = vreq.getJenaOntModel();
		} else {
			this.rdfService = vreq.getUnfilteredRDFService();
			this.contentModel = vreq.getLanguageNeutralUnionFullModel();
		}

		wadf = vreq.getWebappDaoFactory();
	}

	public ResponseValues assembleRdf() {
		OntModel newModel = getRdf();
		newModel.add(getRichExportRdf());
		JenaOutputUtils.setNameSpacePrefixes(newModel, wadf);
		return new RdfResponseValues(rdfFormat, newModel);
	}

	private boolean isLanguageAware() {
		return StringUtils.isNotEmpty(vreq.getHeader("Accept-Language"));
	}

	private OntModel getRdf() {
		OntModel o = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		o.add(getStatementsAboutEntity());
		o.add(getLabelsAndTypesOfRelatedObjects());
		filterByPolicy(o);
		addDocumentMetadata(o);
		return o;
	}

	/**
	 * Get all statements that have the entity as either the subject or the
	 * object.
	 */
	private Model getStatementsAboutEntity() {
		Model m = runConstructQuery(String
				.format("CONSTRUCT { <%1$s> ?predicate ?object .	} "
						+ "WHERE { <%1$s> ?predicate ?object } ", individualUri));
		m.add(runConstructQuery(String.format(
				"CONSTRUCT { ?s ?predicate <%1$s> .	} "
						+ "WHERE { ?s ?predicate <%1$s> } ", individualUri)));
		if (log.isDebugEnabled()) {
			StringWriter sw = new StringWriter();
			m.write(sw);
			log.debug("Statements about '" + individualUri + "': " + sw);
		}
		return m;
	}

	/**
	 * Get the labels and types of all related objects.
	 */
	private Model getLabelsAndTypesOfRelatedObjects() {
		Model m = runConstructQuery(String
				.format("CONSTRUCT { ?object <%2$s> ?type .	} "
						+ "WHERE { <%1$s> ?predicate ?object ."
						+ " ?object <%2$s> ?type . } ", individualUri, RDF.type));
		m.add(runConstructQuery(String.format(
				"CONSTRUCT { ?object <%2$s> ?label .	} "
						+ "WHERE { <%1$s> ?predicate ?object ."
						+ " ?object <%2$s> ?label . } ", individualUri,
				RDFS.label)));
		m.add(runConstructQuery(String.format(
				"CONSTRUCT { ?subject <%2$s> ?type .	} "
						+ "WHERE { ?subject ?predicate <%1$s> ."
						+ " ?subject <%2$s> ?type . } ", individualUri,
				RDF.type)));
		m.add(runConstructQuery(String.format(
				"CONSTRUCT { ?subject <%2$s> ?label .	} "
						+ "WHERE { ?subject ?predicate <%1$s> ."
						+ " ?subject <%2$s> ?label . } ", individualUri,
				RDFS.label)));
		return m;
	}

	/**
	 * Remove any triples that we aren't allowed to see. Then remove any objects
	 * that we no longer have access to.
	 */
	private void filterByPolicy(OntModel o) {
		removeProhibitedTriples(o);
		Set<String> okObjects = determineAccessibleUris(o);
		removeOrphanedObjects(o, okObjects);
	}

	/**
	 * Remove the triples that we aren't allowed to see.
	 */
	private void removeProhibitedTriples(OntModel o) {
		StmtIterator stmts = o.listStatements();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			String subjectUri = stmt.getSubject().getURI();
			String predicateUri = stmt.getPredicate().getURI();
			if (stmt.getObject().isLiteral()) {
				String value = stmt.getObject().asLiteral().getString();
				DataPropertyStatement dps = new DataPropertyStatementImpl(
						subjectUri, predicateUri, value);
				RequestedAction pdps = new PublishDataPropertyStatement(o, dps);
				if (!PolicyHelper.isAuthorizedForActions(vreq, pdps)) {
					log.debug("not authorized: " + pdps);
					stmts.remove();
				}
			} else if (stmt.getObject().isURIResource()) {
				String objectUri = stmt.getObject().asResource().getURI();
				RequestedAction pops = new PublishObjectPropertyStatement(o,
						subjectUri, predicateUri, objectUri);
				if (!PolicyHelper.isAuthorizedForActions(vreq, pops)) {
					log.debug("not authorized: " + pops);
					stmts.remove();
				}
			} else {
				log.warn("blank node: " + stmt);
				stmts.remove();
			}
		}
	}

	/**
	 * Collect the URIs of all objects that are accessible through permitted
	 * triples.
	 */
	private Set<String> determineAccessibleUris(OntModel o) {
		Resource i = o.getResource(individualUri);
		Set<String> uris = new HashSet<>();
		uris.add(individualUri);

		StmtIterator stmts;

		stmts = o.listStatements(i, null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			if (stmt.getObject().isURIResource()) {
				uris.add(stmt.getObject().asResource().getURI());
			}
		}

		stmts = o.listStatements(null, null, i);
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			uris.add(stmt.getSubject().getURI());
		}

		return uris;
	}

	/**
	 * Remove any statements about objects that cannot be reached through
	 * permitted triples.
	 */
	private void removeOrphanedObjects(OntModel o, Set<String> okObjects) {
		StmtIterator stmts = o.listStatements();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			if (!okObjects.contains(stmt.getSubject().getURI())) {
				log.debug("removing orphan triple: " + stmt);
				stmts.remove();
			}
		}
	}

	private Model runConstructQuery(String query) {
		try {
			return RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(
					query, RDFService.ModelSerializationFormat.N3),
					RDFService.ModelSerializationFormat.N3);
		} catch (RDFServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private Model getRichExportRdf() {
		Model richExportModel = ModelFactory.createDefaultModel();

		for (String include : richExportIncludes) {
			String rootDir = RICH_EXPORT_ROOT;
			if (!INCLUDE_ALL.equals(include)) {
				rootDir += include + "/";
			}

			long start = System.currentTimeMillis();
			richExportModel.add(ExtendedLinkedDataUtils.createModelFromQueries(
					ctx, rootDir, contentModel, individualUri));
			long elapsedTimeMillis = System.currentTimeMillis() - start;
			log.debug("Time to create rich export model: msecs = "
					+ elapsedTimeMillis);
		}

		return richExportModel;
	}

	/**
	 * Add info about the RDF itself.
	 * 
	 * It will look something like this:
	 * 
	 * <pre>
	 * <http://vivo.cornell.edu/individual/n6628/n6628.rdf>
	 *     rdfs:label "RDF description of Baker, Able - http://vivo.cornell.edu/individual/n6628" ;
	 *     rdf:type foaf:Document ;
	 *     dc:publisher <http://vivo.cornell.edu> ;
	 *     dc:date "2007-07-13"^^xsd:date ;
	 *     dc:rights <http://vivo.cornell.edu/termsOfUse> .
	 * </pre>
	 */
	private void addDocumentMetadata(OntModel o) {
		String baseUrl = figureBaseUrl();
		String documentUri = createDocumentUri();
		String label = createDocumentLabel(o);
		Literal dateLiteral = createDateLiteral(o);

		Resource md = o.getResource(documentUri);

		o.add(md, o.getProperty(URI_LABEL), label);
		o.add(md, o.getProperty(URI_TYPE), o.getResource(URI_DOCUMENT));
		o.add(md, o.getProperty(URI_PUBLISHER), o.getResource(baseUrl));
		o.add(md, o.getProperty(URI_DATE), dateLiteral);
		o.add(md, o.getProperty(URI_RIGHTS),
				o.getResource(baseUrl + "/termsOfUse"));
	}

	private String figureBaseUrl() {
		int cutHere = individualUri.indexOf("/individual");
		return (cutHere > 0) ? individualUri.substring(0, cutHere)
				: individualUri;
	}

	private String createDocumentUri() {
		return vreq.getRequestURL().toString();
	}

	private String createDocumentLabel(OntModel o) {
		String label = null;
		NodeIterator nodes = o.listObjectsOfProperty(
				o.getResource(individualUri), o.getProperty(URI_LABEL));
		while (nodes.hasNext()) {
			RDFNode n = nodes.nextNode();
			if (n.isLiteral()) {
				label = n.asLiteral().getString();
			}
		}
		if (label == null) {
			return "RDF description of " + individualUri;
		} else {
			return "RDF description of " + label + " - " + individualUri;
		}
	}

	private Literal createDateLiteral(OntModel o) {
		String date = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss")
				.format(new Date());
		return o.createTypedLiteral(date, XSDDatatype.XSDdateTime);
	}

}
