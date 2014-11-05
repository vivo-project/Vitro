/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.bindValues;
import static edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.uriValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.QueryParser;

/**
 * TODO
 */
public class FauxPropertyDaoJena extends JenaBaseDao implements FauxPropertyDao {
	private static final Log log = LogFactory.getLog(FauxPropertyDaoJena.class);

	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	private static final String APPLICATION_CONTEXT_NS = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#";
	private static OntModel _constModel = ModelFactory
			.createOntologyModel(OntModelSpec.OWL_DL_MEM);

	private static final Resource CONFIG_CONTEXT = createResource(appContext("ConfigContext"));
	private static final Resource OBJECT_PROPERTY_DISPLAY_CONFIG = createResource(appContext("ObjectPropertyDisplayConfig"));
	private static final ObjectProperty HAS_CONFIGURATION = createProperty(appContext("hasConfiguration"));
	private static final ObjectProperty CONFIG_CONTEXT_FOR = createProperty(appContext("configContextFor"));

	private static final ObjectProperty QUALIFIED_BY_RANGE = createProperty(appContext("qualifiedBy"));
	private static final ObjectProperty QUALIFIED_BY_DOMAIN = createProperty(appContext("qualifiedByDomain"));
	private static final ObjectProperty LIST_VIEW_FILE = createProperty(appContext("listViewConfigFile"));
	private static final ObjectProperty DISPLAY_NAME = createProperty(appContext("displayName"));
	private static final ObjectProperty PROPERTY_GROUP = createProperty(appContext("propertyGroup"));

	private static final ObjectProperty RDFS_LABEL = createProperty(VitroVocabulary.LABEL);

	private static final String SITE_CONFIG_NAMESPACE = "http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/";

	private static String appContext(String localName) {
		return APPLICATION_CONTEXT_NS + localName;
	}

	private static ObjectProperty createProperty(String uri) {
		return _constModel.createObjectProperty(uri);
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	public FauxPropertyDaoJena(WebappDaoFactoryJena wadf) {
		super(wadf);
	}

	@Override
	protected OntModel getOntModel() {
		return getOntModelSelector().getDisplayModel();
	}

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		List<FauxProperty> list = new ArrayList<>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			if (uri != null) {
				ResIterator contextResources = getOntModel()
						.listSubjectsWithProperty(CONFIG_CONTEXT_FOR,
								createResource(uri));
				for (Resource context : contextResources.toList()) {
					if (!context.isURIResource()) {
						continue;
					}

					FauxProperty fp = getFauxPropertyFromContextUri(context
							.getURI());
					if (fp == null) {
						continue;
					}

					list.add(fp);
				}
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}
		return list;
	}

	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		getOntModel().enterCriticalSection(Lock.READ);
		try {
			if (contextUri == null) {
				return null;
			}

			Resource context = createResource(contextUri);
			if (!getOntModel().contains(context, RDF.type, CONFIG_CONTEXT)) {
				log.debug("'" + contextUri + "' is not a CONFIG_CONTEXT");
				return null;
			}

			String baseUri = getUriValue(getOntModel(), context,
					CONFIG_CONTEXT_FOR);
			if (baseUri == null) {
				log.debug("'" + contextUri + "' has no value for '"
						+ CONFIG_CONTEXT_FOR + "'");
				return null;
			}

			String rangeUri = getUriValue(getOntModel(), context,
					QUALIFIED_BY_RANGE);
			if (rangeUri == null) {
				log.debug("'" + contextUri + "' has no value for '"
						+ QUALIFIED_BY_RANGE + "'");
				return null;
			}

			// domainURI is optional.
			String domainUri = getUriValue(getOntModel(), context,
					QUALIFIED_BY_DOMAIN);

			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			populateInstance(fp, context);
			return fp;
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(
				getOntModelSelector().getDisplayModel(), domainUri, baseUri,
				rangeUri);
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
	public void insertFauxProperty(FauxProperty fp) {
		if ((fp.getContextUri() != null) || (fp.getConfigUri() == null)) {
			throw new IllegalStateException(
					"ContextUri and ConfigUri must be null on insert: contextUri="
							+ fp.getContextUri() + ", configUri="
							+ fp.getConfigUri());
		}

		Set<ConfigContext> existingcontexts = ConfigContext.findByQualifiers(
				getOntModelSelector().getDisplayModel(), fp.getDomainURI(),
				fp.getBaseURI(), fp.getRangeURI());
		if (!existingcontexts.isEmpty()) {
			throw new IllegalStateException(
					"FauxProperty already exists with domainUri="
							+ fp.getDomainURI() + ", baseUri="
							+ fp.getBaseURI() + ", rangeUri="
							+ fp.getRangeURI());
		}

		try {
			fp.setContextUri(getUnusedURI());
			fp.setConfigUri(getUnusedURI());
		} catch (InsertException e) {
			throw new RuntimeException(e);
		}

		getOntModel().enterCriticalSection(Lock.WRITE);
		try {
			OntResource context = getOntModel().createOntResource(
					fp.getContextUri());
			addPropertyResourceValue(context, RDF.type, CONFIG_CONTEXT);
			addPropertyResourceURIValue(context, HAS_CONFIGURATION,
					fp.getConfigUri());
			addPropertyResourceURIValue(context, CONFIG_CONTEXT_FOR,
					fp.getBaseURI());
			addPropertyResourceURIValue(context, QUALIFIED_BY_RANGE,
					fp.getRangeURI());
			addPropertyResourceURIValue(context, QUALIFIED_BY_DOMAIN,
					fp.getDomainURI());

			OntResource config = getOntModel().createOntResource(
					fp.getConfigUri());
			addPropertyResourceValue(config, RDF.type,
					OBJECT_PROPERTY_DISPLAY_CONFIG);
			addPropertyResourceURIValue(config, PROPERTY_GROUP,
					fp.getGroupURI());
			addPropertyStringValue(config, DISPLAY_NAME, fp.getDisplayName(),
					getOntModel());
			addPropertyStringValue(config, PUBLIC_DESCRIPTION_ANNOT,
					fp.getPublicDescription(), getOntModel());
			addPropertyIntValue(config, DISPLAY_RANK_ANNOT,
					fp.getDisplayTier(), getOntModel());
			addPropertyIntValue(config, DISPLAY_LIMIT, fp.getDisplayLimit(),
					getOntModel());
			addPropertyBooleanValue(config, PROPERTY_COLLATEBYSUBCLASSANNOT,
					fp.isCollateBySubclass(), getOntModel());
			addPropertyBooleanValue(config, PROPERTY_SELECTFROMEXISTINGANNOT,
					fp.isSelectFromExisting(), getOntModel());
			addPropertyBooleanValue(config, PROPERTY_OFFERCREATENEWOPTIONANNOT,
					fp.isOfferCreateNewOption(), getOntModel());
			addPropertyStringValue(config, PROPERTY_CUSTOMENTRYFORMANNOT,
					fp.getCustomEntryForm(), getOntModel());
			addPropertyStringValue(config, LIST_VIEW_FILE,
					fp.getCustomListView(), getOntModel());
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public void updateFauxProperty(FauxProperty fp) {
		getOntModel().enterCriticalSection(Lock.READ);
		try {
			if (fp.getContextUri() == null) {
				throw new IllegalStateException("ContextURI may not be null: "
						+ fp);
			}
			Resource context = createResource(fp.getContextUri());

			if (fp.getConfigUri() == null) {
				throw new IllegalStateException("ConfigURI may not be null: "
						+ fp);
			}
			Resource config = createResource(fp.getConfigUri());

			if (!getOntModel().contains(context, RDF.type, CONFIG_CONTEXT)) {
				throw new IllegalStateException("'" + context + "' is not a '"
						+ CONFIG_CONTEXT + "'");
			}
			if (!getOntModel().contains(config, RDF.type,
					OBJECT_PROPERTY_DISPLAY_CONFIG)) {
				throw new IllegalStateException("'" + config + "' is not a '"
						+ OBJECT_PROPERTY_DISPLAY_CONFIG + "'");
			}
			if (!getOntModel().contains(context, HAS_CONFIGURATION, config)) {
				throw new IllegalStateException("'" + config
						+ "' is not a configuration for '" + context + "'");
			}

		} finally {
			getOntModel().leaveCriticalSection();
		}

		getOntModel().enterCriticalSection(Lock.WRITE);
		try {
			OntResource context = getOntModel().createOntResource(
					fp.getContextUri());
			updatePropertyResourceURIValue(context, QUALIFIED_BY_RANGE,
					fp.getRangeURI());
			updatePropertyResourceURIValue(context, QUALIFIED_BY_DOMAIN,
					fp.getDomainURI());

			OntResource config = getOntModel().createOntResource(
					fp.getConfigUri());
			updatePropertyResourceURIValue(config, PROPERTY_GROUP,
					fp.getGroupURI());
			updatePropertyStringValue(config, DISPLAY_NAME, fp.getDisplayName(),
					getOntModel());
			updatePropertyStringValue(config, PUBLIC_DESCRIPTION_ANNOT,
					fp.getPublicDescription(), getOntModel());
			updatePropertyIntValue(config, DISPLAY_RANK_ANNOT,
					fp.getDisplayTier(), getOntModel());
			updatePropertyIntValue(config, DISPLAY_LIMIT, fp.getDisplayLimit(),
					getOntModel());
			updatePropertyBooleanValue(config, PROPERTY_COLLATEBYSUBCLASSANNOT,
					fp.isCollateBySubclass(), getOntModel(), true);
			updatePropertyBooleanValue(config, PROPERTY_SELECTFROMEXISTINGANNOT,
					fp.isSelectFromExisting(), getOntModel(), true);
			updatePropertyBooleanValue(config, PROPERTY_OFFERCREATENEWOPTIONANNOT,
					fp.isOfferCreateNewOption(), getOntModel(), true);
			updatePropertyStringValue(config, PROPERTY_CUSTOMENTRYFORMANNOT,
					fp.getCustomEntryForm(), getOntModel());
			updatePropertyStringValue(config, LIST_VIEW_FILE,
					fp.getCustomListView(), getOntModel());
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(
				getOntModelSelector().getDisplayModel(), fp.getDomainURI(),
				fp.getURI(), fp.getRangeURI());
		getOntModel().enterCriticalSection(Lock.READ);
		try {
			for (ConfigContext context : contexts) {
				Resource configResource = createResource(context.getConfigUri());
				getOntModel().removeAll(configResource, null, null);
				getOntModel().removeAll(null, null, configResource);
				Resource contextResource = createResource(context
						.getContextUri());
				getOntModel().removeAll(contextResource, null, null);
				getOntModel().removeAll(null, null, contextResource);
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	private String getUnusedURI() throws InsertException {
		String errMsg = null;

		String namespace = SITE_CONFIG_NAMESPACE;
		String uri = null;

		Random random = new Random(System.currentTimeMillis());
		for (int attempts = 0; attempts < 30; attempts++) {
			int upperBound = (int) Math.pow(2, attempts + 13);
			uri = namespace + ("fp" + random.nextInt(upperBound));
			if (!isUriUsed(uri)) {
				return uri;
			}
		}

		throw new InsertException("Could not create URI for individual: "
				+ errMsg);
	}

	private boolean isUriUsed(String uri) {
		getOntModel().enterCriticalSection(Lock.READ);
		try {
			return (getOntModel().getOntResource(uri) != null);
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	/**
	 * Add labels, annotations, and whatever else we can find on the
	 * ObjectPropertyDisplayConfig.
	 */
	private void populateInstance(FauxProperty fp, Resource context) {
		populateLabelsFromTBox(fp);
		populateFieldsFromDisplayModel(fp, context);
	}

	private void populateLabelsFromTBox(FauxProperty fp) {
		fp.setRangeLabel(findLabelForClass(fp.getRangeURI()));
		fp.setDomainLabel(findLabelForClass(fp.getDomainURI()));
	}

	private String findLabelForClass(String classUri) {
		if (classUri == null) {
			return null;
		} else {
			OntModel tboxModel = getOntModelSelector().getTBoxModel();
			tboxModel.enterCriticalSection(Lock.READ);
			try {
				return getStringValue(tboxModel, createResource(classUri),
						RDFS_LABEL);
			} finally {
				tboxModel.leaveCriticalSection();
			}
		}
	}

	private void populateFieldsFromDisplayModel(FauxProperty fp,
			Resource context) {
		OntResource config = locateConfigurationFromContext(context);
		if (config != null) {
			getOntModel().enterCriticalSection(Lock.READ);
			try {
				fp.setDisplayName(getPropertyStringValue(config, DISPLAY_NAME));
				fp.setPublicDescription(getPropertyStringValue(config,
						PUBLIC_DESCRIPTION_ANNOT));
				fp.setGroupURI(getSingleResourceURIValue(config, PROPERTY_GROUP));
				fp.setCustomListView(getPropertyStringValue(config,
						LIST_VIEW_FILE));
				fp.setDisplayTier(getPropertyIntValue(config,
						DISPLAY_RANK_ANNOT));
				fp.setDisplayLimit(getPropertyIntValue(config, DISPLAY_LIMIT));
				fp.setCollateBySubclass(getPropertyBooleanValue(config,
						PROPERTY_COLLATEBYSUBCLASSANNOT));
				fp.setSelectFromExisting(getPropertyBooleanValue(config,
						PROPERTY_SELECTFROMEXISTINGANNOT));
				fp.setOfferCreateNewOption(getPropertyBooleanValue(config,
						PROPERTY_OFFERCREATENEWOPTIONANNOT));
				fp.setCustomEntryForm(getPropertyStringValue(config,
						PROPERTY_CUSTOMENTRYFORMANNOT));
			} finally {
				getOntModel().leaveCriticalSection();
			}
		}
	}

	private OntResource locateConfigurationFromContext(Resource context) {
		getOntModel().enterCriticalSection(Lock.READ);
		try {
			String configUri = getUriValue(getOntModel(), context,
					HAS_CONFIGURATION);
			if (configUri == null) {
				return null;
			} else {
				return getOntModel().createOntResource(configUri);
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}

	private String getSingleResourceURIValue(OntResource config,
			ObjectProperty prop) {
		Collection<String> values = getPropertyResourceURIValues(config, prop);
		if (values.isEmpty()) {
			return null;
		} else {
			return values.iterator().next();
		}
	}

	/**
	 * Returns a single URI that is the object of this subject and property.
	 * Returns null if no valid statement is found.
	 * 
	 * The model should already be locked.
	 */
	private String getUriValue(OntModel model, Resource subject,
			Property property) {
		List<RDFNode> nodeList = model.listObjectsOfProperty(subject, property)
				.toList();
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
	 * 
	 * The model should already be locked.
	 */
	private String getStringValue(OntModel model, Resource subject,
			Property property) {
		List<RDFNode> nodeList = model.listObjectsOfProperty(subject, property)
				.toList();
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
				OntModel displayModel, String domainUri, String baseUri,
				String rangeUri) {
			displayModel.enterCriticalSection(Lock.READ);
			try {
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
			} finally {
				displayModel.leaveCriticalSection();
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

}
