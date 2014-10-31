/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.bindValues;
import static edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.uriValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
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
	private static final Property RDFS_LABEL = createProperty(VitroVocabulary.LABEL);

	// ----------------------------------------------------------------------
	// Queries and parsers
	// ----------------------------------------------------------------------

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
			populateInstance(fp, context);
			return fp;
		}
	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(models,
				domainUri, baseUri, rangeUri);
		for (ConfigContext context : contexts) {
			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			populateInstance(fp, createResource(context.getContextUri()));
			return fp;
		}
		log.debug("Can't find a FauxProperty for '" + domainUri + "', '"
				+ baseUri + "', '" + rangeUri + "'");
		return null;
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(models,
				fp.getDomainURI(), fp.getURI(), fp.getRangeURI());
		try (LockedOntModel displayModel = models.getDisplayModel().write()) {
			for (ConfigContext context : contexts) {
				Resource configResource = createResource(context.getConfigUri());
				displayModel.removeAll(configResource, null, null);
				displayModel.removeAll(null, null, configResource);
				Resource contextResource = createResource(context.getContextUri());
				displayModel.removeAll(contextResource, null, null);
				displayModel.removeAll(null, null, contextResource);
			}
		}
	}

	/**
	 * Add labels, annotations, and whatever else we can find on the
	 * ObjectPropertyDisplayConfig.
	 */
	private void populateInstance(FauxProperty fp, Resource context) {
		// Range label and domain label.
		try (LockedOntModel tboxModel = models.getTBoxModel().read()) {
			String rangeLabel = getStringValue(tboxModel,
					createProperty(fp.getRangeURI()), RDFS_LABEL);
			if (rangeLabel != null) {
				fp.setRangeLabel(rangeLabel);
			}

			String domainLabel = getStringValue(tboxModel,
					createProperty(fp.getDomainURI()), RDFS_LABEL);
			if (domainLabel != null) {
				fp.setDomainLabel(domainLabel);
			}

		}

		// Display name.
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			String configUri = getUriValue(displayModel, context,
					HAS_CONFIGURATION);
			if (configUri == null) {
				return;
			}
			Resource config = createResource(configUri);

			String displayName = getStringValue(displayModel, config,
					DISPLAY_NAME);
			if (displayName != null) {
				fp.setPickListName(displayName);
			}
		}

		// TODO pull all sorts of things from the configuration.
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
	// ConfigContext
	// ----------------------------------------------------------------------

	private static final String QUERY_LOCATE_CONFIG_CONTEXT_WITH_DOMAIN = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context ?config \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedByDomain ?domainUri ; \n" //
			+ "        :qualifiedBy ?rangeUri ; \n" //
			+ "        :hasConfiguration ?config . \n" //
			+ "} \n"; //

	// TODO Add a filter that will reject solutions that include
	// qualifiedByDomain
	private static final String QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedBy ?rangeUri ; \n" //
			+ "        :hasConfiguration ?config . \n" //
			+ "} \n"; //

	private static class ParserLocateConfigContext extends
			QueryParser<Set<ConfigContext>> {
		private final String domainUri;
		private final String baseUri;
		private final String rangeUri;

		public ParserLocateConfigContext(String domainUri, String baseUri,
				String rangeUri) {
			this.domainUri = domainUri;
			this.baseUri = baseUri;
			this.rangeUri = rangeUri;
		}

		@Override
		protected Set<ConfigContext> defaultValue() {
			return Collections.emptySet();
		}

		@Override
		protected Set<ConfigContext> parseResults(String queryStr,
				ResultSet results) {
			Set<ConfigContext> set = new HashSet<>();
			while (results.hasNext()) {
				QuerySolution row = results.next();
				String contextUri = ifResourcePresent(row, "context", null);
				String configUri = ifResourcePresent(row, "config", null);
				if (contextUri != null && configUri != null) {
					set.add(new ConfigContext(contextUri, configUri, domainUri,
							baseUri, rangeUri));
				}
			}
			return set;
		}
	}

	private static class ConfigContext {
		public static Set<ConfigContext> findByQualifiers(
				LockingOntModelSelector models, String domainUri,
				String baseUri, String rangeUri) {
			try (LockedOntModel displayModel = models.getDisplayModel().read()) {

				String queryString;
				if (domainUri == null) {
					queryString = bindValues(
							QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN,
							uriValue("baseUri", baseUri),
							uriValue("rangeUri", rangeUri));
				} else {
					queryString = bindValues(
							QUERY_LOCATE_CONFIG_CONTEXT_WITH_DOMAIN,
							uriValue("baseUri", baseUri),
							uriValue("rangeUri", rangeUri),
							uriValue("domainUri", domainUri));
				}

				ParserLocateConfigContext parser = new ParserLocateConfigContext(
						domainUri, baseUri, rangeUri);
				return new SparqlQueryRunner(displayModel).executeSelect(
						parser, queryString);
			}

		}

		private final String contextUri;
		private final String configUri;
		private final String domainUri;
		private final String baseUri;
		private final String rangeUri;

		public ConfigContext(String contextUri, String configUri,
				String domainUri, String baseUri, String rangeUri) {
			this.contextUri = contextUri;
			this.configUri = configUri;
			this.domainUri = domainUri;
			this.baseUri = baseUri;
			this.rangeUri = rangeUri;
		}

		public String getContextUri() {
			return contextUri;
		}

		public String getConfigUri() {
			return configUri;
		}

		public String getDomainUri() {
			return domainUri;
		}

		public String getBaseUri() {
			return baseUri;
		}

		public String getRangeUri() {
			return rangeUri;
		}

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

		public LockableOntModel getTBoxModel() {
			return new LockableOntModel(oms.getTBoxModel());
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

		public LockedOntModel write() {
			ontModel.enterCriticalSection(Lock.WRITE);
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
