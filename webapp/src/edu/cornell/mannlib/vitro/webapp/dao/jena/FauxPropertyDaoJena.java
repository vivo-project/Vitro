/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.AbstractOntModelDecorator;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.QueryParser;

/**
 * TODO
 */
public class FauxPropertyDaoJena implements FauxPropertyDao {
	private static final Log log = LogFactory.getLog(FauxPropertyDaoJena.class);

	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	private static final String APPLICATION_CONTEXT_NS = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#";
	private static final Resource CONFIG_CONTEXT = createResource(APPLICATION_CONTEXT_NS
			+ "ConfigContext");
	private static final Property HAS_CONFIGURATION = createProperty(APPLICATION_CONTEXT_NS
			+ "hasConfiguration");
	private static final Property CONFIG_CONTEXT_FOR = createProperty(APPLICATION_CONTEXT_NS
			+ "configContextFor");
	private static final Property QUALIFIED_BY_RANGE = createProperty(APPLICATION_CONTEXT_NS
			+ "qualifiedBy");
	private static final Property QUALIFIED_BY_DOMAIN = createProperty(APPLICATION_CONTEXT_NS
			+ "qualifiedByDomain");

	private static final Property DISPLAY_NAME = createProperty(APPLICATION_CONTEXT_NS
			+ "displayName");

	// ----------------------------------------------------------------------
	// Queries and parsers
	// ----------------------------------------------------------------------

	private static final String QUERY_LOCATE_CONFIG_CONTEXT_WITH_DOMAIN = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedByDomain ?domainUri ; \n" //
			+ "        :qualifiedBy ?rangeUri . \n" //
			+ "} \n"; //

	// TODO Add a filter that will reject solutions that include qualifiedByDomain
	private static final String QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedBy ?rangeUri . \n" //
			+ "} \n"; //

	private static final QueryParser<String> PARSER_LOCATE_CONFIG_CONTEXT = new QueryParser<String>() {
		@Override
		protected String defaultValue() {
			return null;
		}

		@Override
		protected String parseResults(String queryStr, ResultSet results) {
			if (results.hasNext()) {
				return ifResourcePresent(results.next(), "context", null);
			} else {
				return null;
			}
		}
	};

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private final LockingOntModelSelector models;

	public FauxPropertyDaoJena(WebappDaoFactoryJena wadf) {
		this.models = new LockingOntModelSelector(wadf.getOntModelSelector());
	}

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		List<FauxProperty> list = new ArrayList<>();
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			if (uri != null) {
				ResIterator contextResources = displayModel
						.listSubjectsWithProperty(CONFIG_CONTEXT_FOR,
								createResource(uri));
				for (Resource context : contextResources.toList()) {
					if (!context.isURIResource()) {
						continue;
					}

					FauxProperty fp = getFauxPropertyFromConfigContextUri(context
							.getURI());
					if (fp == null) {
						continue;
					}

					list.add(fp);
				}
			}
		}
		return list;
	}

	@Override
	public FauxProperty getFauxPropertyFromConfigContextUri(String contextUri) {
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			if (contextUri == null) {
				return null;
			}

			Resource context = createResource(contextUri);
			if (!displayModel.contains(context, RDF.type, CONFIG_CONTEXT)) {
				log.debug("'" + contextUri + "' is not a CONFIG_CONTEXT");
				return null;
			}

			String baseUri = getUriValue(displayModel, context,
					CONFIG_CONTEXT_FOR);
			if (baseUri == null) {
				log.debug("'" + contextUri + "' has no value for '"
						+ CONFIG_CONTEXT_FOR + "'");
				return null;
			}

			String rangeUri = getUriValue(displayModel, context,
					QUALIFIED_BY_RANGE);
			if (rangeUri == null) {
				log.debug("'" + contextUri + "' has no value for '"
						+ QUALIFIED_BY_RANGE + "'");
				return null;
			}

			// domainURI is optional.
			String domainUri = getUriValue(displayModel, context,
					QUALIFIED_BY_DOMAIN);

			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			populateInstance(fp, displayModel, context);
			return fp;
		}
	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			String queryString;
			if (domainUri == null) {
				queryString = substituteUri(
						substituteUri(
								QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN,
								baseUri, "baseUri"), rangeUri, "rangeUri");
			} else {
				queryString = substituteUri(
						substituteUri(
								substituteUri(
										QUERY_LOCATE_CONFIG_CONTEXT_WITH_DOMAIN,
										baseUri, "baseUri"), rangeUri,
								"rangeUri"), domainUri, "domainUri");
			}

			String contextUri = new SparqlQueryRunner(displayModel)
					.executeSelect(PARSER_LOCATE_CONFIG_CONTEXT, queryString);

			if (contextUri == null) {
				log.debug("Can't find a ContextConfig for '" + domainUri
						+ "', '" + baseUri + "', '" + rangeUri + "'");
				return null;
			}

			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			populateInstance(fp, displayModel, createResource(contextUri));
			return fp;
		}
	}

	/**
	 * Add labels, annotations, and whatever else we can find on the
	 * ConfigContext.
	 */
	private void populateInstance(FauxProperty fp, LockedOntModel model,
			Resource context) {
		String configUri = getUriValue(model, context, HAS_CONFIGURATION);
		if (configUri == null) {
			return;
		}
		Resource config = createResource(configUri);

		String displayName = getStringValue(model, config, DISPLAY_NAME);
		if (displayName != null) {
			fp.setPickListName(displayName);
		}
		
		// TODO pull all sorts of things from the configuration.
		// TODO pull labels for the domain and range classes.
	}

	private String substituteUri(String queryString, String variableName,
			String uri) {
		return queryString.replace("?" + variableName, "<" + uri + ">");
	}
	
	/**
	 * Returns a single URI that is the object of this subject and property.
	 * Returns null if no valid statement is found.
	 */
	private String getUriValue(LockedOntModel displayModel, Resource subject,
			Property property) {
		List<RDFNode> nodeList = displayModel.listObjectsOfProperty(subject,
				property).toList();
		if (nodeList.isEmpty()) {
			log.warn("'" + subject.getURI() + "' has no value for '"
					+ property.getURI() + "'.");
			return null;
		}

		RDFNode node = nodeList.get(0);
		if (nodeList.size() > 1) {
			log.warn("'" + subject.getURI() + "' has " + nodeList.size()
					+ " values for ''. Using '" + node + "'");
		}
		if (!node.isURIResource()) {
			log.warn("Value of '" + subject.getURI() + property.getURI()
					+ "' '' is not a URI resource.");
			return null;
		}
		return node.asResource().getURI();
	}

	/**
	 * Returns a single String value that is the object of this subject and
	 * property. Returns null if no valid statement is found.
	 */
	private String getStringValue(LockedOntModel displayModel,
			Resource subject, Property property) {
		List<RDFNode> nodeList = displayModel.listObjectsOfProperty(subject,
				property).toList();
		if (nodeList.isEmpty()) {
			log.warn("'" + subject.getURI() + "' has no value for '"
					+ property.getURI() + "'.");
			return null;
		}

		RDFNode node = nodeList.get(0);
		if (nodeList.size() > 1) {
			log.warn("'" + subject.getURI() + "' has " + nodeList.size()
					+ " values for ''. Using '" + node + "'");
		}
		if (!node.isLiteral()) {
			log.warn("Value of '" + subject.getURI() + property.getURI()
					+ "' '' is not a Literal.");
			return null;
		}
		return node.asLiteral().getString();
	}

	// ----------------------------------------------------------------------
	// Helper classes. Are they worth it, just to use try-with-resources?
	// ----------------------------------------------------------------------

	private static class LockingOntModelSelector {
		private final OntModelSelector oms;

		public LockingOntModelSelector(OntModelSelector oms) {
			this.oms = oms;
		}

		public LockableOntModel getDisplayModel() {
			return new LockableOntModel(oms.getDisplayModel());
		}
	}

	private static class LockableOntModel {
		private final OntModel ontModel;

		public LockableOntModel(OntModel ontModel) {
			this.ontModel = ontModel;
		}

		public LockedOntModel read() {
			ontModel.enterCriticalSection(Lock.READ);
			return new LockedOntModel(ontModel);
		}
	}

	private static class LockedOntModel extends AbstractOntModelDecorator
			implements AutoCloseable {

		protected LockedOntModel(OntModel m) {
			super(m);
		}

		/**
		 * Just unlocks the model. Doesn't actually close it, because we may
		 * want to use it again.
		 */
		@Override
		public void close() {
			super.leaveCriticalSection();
		}
	}
}
