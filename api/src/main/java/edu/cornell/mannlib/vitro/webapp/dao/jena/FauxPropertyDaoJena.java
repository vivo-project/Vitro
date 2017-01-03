/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.queryHolder;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.ResultSetParser;

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
	private static final ObjectProperty QUALIFIED_BY_ROOT = createProperty(appContext("qualifiedByRoot"));
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

	private final LockableOntModelSelector models;

	public FauxPropertyDaoJena(WebappDaoFactoryJena wadf) {
		super(wadf);
		this.models = new LockableOntModelSelector(wadf.getOntModelSelector());
	}

	/**
	 * Need to override this, so the boolean convenience methods will work off
	 * the correct model.
	 */
	@Override
	protected OntModel getOntModel() {
		return getOntModelSelector().getDisplayModel();
	}

	@Override
	public List<FauxProperty> getFauxPropertiesForBaseUri(String uri) {
		if (uri == null) {
			return Collections.emptyList();
		}

		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			Set<String> contextUris = new HashSet<>();
			ResIterator contextResources = displayModel
					.listSubjectsWithProperty(CONFIG_CONTEXT_FOR,
							createResource(uri));
			for (Resource context : contextResources.toList()) {
				if (context.isURIResource()) {
					contextUris.add(context.asResource().getURI());
				}
			}

			List<FauxProperty> fpList = new ArrayList<>();
			for (String contextUri : contextUris) {
				FauxProperty fp = getFauxPropertyFromContextUri(contextUri);
				if (fp != null) {
					fpList.add(fp);
				}
			}
			log.debug("Located " + fpList.size() + " FauxProperties.");
			return fpList;
		}
	}

	/**
	 * Returns null if contextUri does not represent a valid CONFIG_CONTEXT.
	 */
	@Override
	public FauxProperty getFauxPropertyFromContextUri(String contextUri) {
		if (contextUri == null) {
			return null;
		}

		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			OntResource context = displayModel.createOntResource(contextUri);
			if (!displayModel.contains(context, RDF.type, CONFIG_CONTEXT)) {
				log.debug("'" + contextUri + "' is not a CONFIG_CONTEXT");
				return null;
			}

			Collection<String> baseUris = getPropertyResourceURIValues(context,
					CONFIG_CONTEXT_FOR);
			if (baseUris.isEmpty()) {
				log.debug("'" + contextUri + "' has no value for '"
						+ CONFIG_CONTEXT_FOR + "'");
				return null;
			}
			String baseUri = baseUris.iterator().next();

			Collection<String> rangeUris = getPropertyResourceURIValues(
					context, QUALIFIED_BY_RANGE);
			if (rangeUris.isEmpty()) {
				log.debug("'" + contextUri + "' has no value for '"
						+ QUALIFIED_BY_RANGE + "'");
				return null;
			}
			String rangeUri = rangeUris.iterator().next();

			// domainURI is optional.
			Collection<String> domainUris = getPropertyResourceURIValues(
					context, QUALIFIED_BY_DOMAIN);
			String domainUri = domainUris.isEmpty() ? null : domainUris
					.iterator().next();

			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			fp.setContextUri(contextUri);
			populateInstance(fp);
			log.debug("Loaded FauxProperty: " + fp);
			return fp;
		}
	}

	@Override
	public FauxProperty getFauxPropertyByUris(String domainUri, String baseUri,
			String rangeUri) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(
				models.getDisplayModel(), domainUri, baseUri, rangeUri);
		if (contexts.isEmpty()) {
			log.debug("Can't find a FauxProperty for '" + domainUri + "', '"
					+ baseUri + "', '" + rangeUri + "'");
			return null;
		} else {
			FauxProperty fp = new FauxProperty(domainUri, baseUri, rangeUri);
			fp.setContextUri(contexts.iterator().next().getContextUri());
			populateInstance(fp);
			log.debug("Loaded FauxProperty: " + fp);
			return fp;
		}
	}

	@Override
	public void insertFauxProperty(FauxProperty fp) {
		if ((fp.getContextUri() != null) || (fp.getConfigUri() != null)) {
			throw new IllegalStateException(
					"ContextUri and ConfigUri must be null on insert: " + fp);
		}

		Set<ConfigContext> existingcontexts = ConfigContext.findByQualifiers(
				models.getDisplayModel(), fp.getDomainURI(), fp.getBaseURI(),
				fp.getRangeURI());
		if (!existingcontexts.isEmpty()) {
			throw new IllegalStateException(
					"FauxProperty with these qualifiers already exists: " + fp);
		}

		try (LockedOntModel displayModel = models.getDisplayModel().write()) {
			fp.setContextUri(getUnusedURI());

			OntResource context = displayModel.createOntResource(fp
					.getContextUri());
			addPropertyResourceValue(context, RDF.type, CONFIG_CONTEXT);
			addPropertyResourceURIValue(context, CONFIG_CONTEXT_FOR,
					fp.getBaseURI());
			addPropertyResourceURIValue(context, QUALIFIED_BY_RANGE,
					fp.getRangeURI());
			addPropertyResourceURINotEmpty(context, QUALIFIED_BY_DOMAIN,
					fp.getDomainURI());
			storeQualifiedByRoot(context, fp.getRangeURI());

			fp.setConfigUri(getUnusedURI());
			addPropertyResourceURIValue(context, HAS_CONFIGURATION,
					fp.getConfigUri());

			OntResource config = displayModel.createOntResource(fp
					.getConfigUri());
			addPropertyResourceValue(config, RDF.type,
					OBJECT_PROPERTY_DISPLAY_CONFIG);
			addPropertyResourceURINotEmpty(config, PROPERTY_GROUP,
					fp.getGroupURI());
			addPropertyStringValue(config, DISPLAY_NAME, fp.getDisplayName(),
					displayModel);
			addPropertyStringValue(config, PUBLIC_DESCRIPTION_ANNOT,
					fp.getPublicDescription(), displayModel);
			addPropertyIntValue(config, DISPLAY_RANK_ANNOT,
					fp.getDisplayTier(), displayModel);
			addPropertyIntValue(config, DISPLAY_LIMIT, fp.getDisplayLimit(),
					displayModel);
			addPropertyBooleanValue(config, PROPERTY_COLLATEBYSUBCLASSANNOT,
					fp.isCollateBySubclass(), displayModel);
			addPropertyBooleanValue(config, PROPERTY_SELECTFROMEXISTINGANNOT,
					fp.isSelectFromExisting(), displayModel);
			addPropertyBooleanValue(config, PROPERTY_OFFERCREATENEWOPTIONANNOT,
					fp.isOfferCreateNewOption(), displayModel);
			addPropertyStringValue(config, PROPERTY_CUSTOMENTRYFORMANNOT,
					fp.getCustomEntryForm(), displayModel);
			addPropertyStringValue(config, LIST_VIEW_FILE,
					fp.getCustomListView(), displayModel);

			updatePropertyResourceURIValue(config,
					HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, fp
							.getHiddenFromDisplayBelowRoleLevel().getURI());
			updatePropertyResourceURIValue(config,
					HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT, fp
							.getHiddenFromPublishBelowRoleLevel().getURI());
			updatePropertyResourceURIValue(config,
					PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, fp
							.getProhibitedFromUpdateBelowRoleLevel().getURI());

		} catch (InsertException e) {
			throw new RuntimeException(e);
		}
	}

	private void addPropertyResourceURINotEmpty(OntResource context,
			ObjectProperty prop, String uri) {
		if (uri != null && !uri.isEmpty()) {
			addPropertyResourceURIValue(context, prop, uri);
		}
	}

	private static final String VCARD_KIND_URI = "http://www.w3.org/2006/vcard/ns#Kind";
	private static final String VCARD_NAMESPACE = "http://www.w3.org/2006/vcard/ns#";

	private void storeQualifiedByRoot(OntResource context, String rangeURI) {
		if (rangeURI.startsWith(VCARD_NAMESPACE)) {
			updatePropertyResourceURIValue(context, QUALIFIED_BY_ROOT,
					VCARD_KIND_URI);
		} else {
			updatePropertyResourceURIValue(context, QUALIFIED_BY_ROOT, null);
		}
	}

	@Override
	public void updateFauxProperty(FauxProperty fp) {
		log.debug("Updating FauxProperty: " + fp);

		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
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

			if (!displayModel.contains(context, RDF.type, CONFIG_CONTEXT)) {
				throw new IllegalStateException("'" + context + "' is not a '"
						+ CONFIG_CONTEXT + "'");
			}
			if (!displayModel.contains(config, RDF.type,
					OBJECT_PROPERTY_DISPLAY_CONFIG)) {
				throw new IllegalStateException("'" + config + "' is not a '"
						+ OBJECT_PROPERTY_DISPLAY_CONFIG + "'");
			}
			if (!displayModel.contains(context, HAS_CONFIGURATION, config)) {
				throw new IllegalStateException("'" + config
						+ "' is not a configuration for '" + context + "'");
			}
		}

		try (LockedOntModel displayModel = models.getDisplayModel().write()) {
			OntResource context = displayModel.createOntResource(fp
					.getContextUri());
			updatePropertyResourceURIValue(context, QUALIFIED_BY_RANGE,
					fp.getRangeURI());
			updatePropertyResourceURIValue(context, QUALIFIED_BY_DOMAIN,
					fp.getDomainURI());
			storeQualifiedByRoot(context, fp.getRangeURI());

			OntResource config = displayModel.createOntResource(fp
					.getConfigUri());
			updatePropertyResourceURIValue(config, PROPERTY_GROUP,
					fp.getGroupURI());
			updatePropertyStringValue(config, DISPLAY_NAME,
					fp.getDisplayName(), displayModel);
			updatePropertyStringValue(config, PUBLIC_DESCRIPTION_ANNOT,
					fp.getPublicDescription(), displayModel);
			updatePropertyIntValue(config, DISPLAY_RANK_ANNOT,
					fp.getDisplayTier(), displayModel);
			updatePropertyIntValue(config, DISPLAY_LIMIT, fp.getDisplayLimit(),
					displayModel);
			updatePropertyBooleanValue(config, PROPERTY_COLLATEBYSUBCLASSANNOT,
					fp.isCollateBySubclass(), displayModel, true);
			updatePropertyBooleanValue(config,
					PROPERTY_SELECTFROMEXISTINGANNOT,
					fp.isSelectFromExisting(), displayModel, true);
			updatePropertyBooleanValue(config,
					PROPERTY_OFFERCREATENEWOPTIONANNOT,
					fp.isOfferCreateNewOption(), displayModel, true);
			updatePropertyStringValue(config, PROPERTY_CUSTOMENTRYFORMANNOT,
					fp.getCustomEntryForm(), displayModel);
			updatePropertyStringValue(config, LIST_VIEW_FILE,
					fp.getCustomListView(), displayModel);

			updatePropertyResourceURIValue(config,
					HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, fp
							.getHiddenFromDisplayBelowRoleLevel().getURI());
			updatePropertyResourceURIValue(config,
					HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT, fp
							.getHiddenFromPublishBelowRoleLevel().getURI());
			updatePropertyResourceURIValue(config,
					PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, fp
							.getProhibitedFromUpdateBelowRoleLevel().getURI());
		}
	}

	@Override
	public void deleteFauxProperty(FauxProperty fp) {
		Set<ConfigContext> contexts = ConfigContext.findByQualifiers(
				models.getDisplayModel(), fp.getDomainURI(), fp.getURI(),
				fp.getRangeURI());
		try (LockedOntModel displayModel = models.getDisplayModel().write()) {
			for (ConfigContext context : contexts) {
				Resource configResource = createResource(context.getConfigUri());
				displayModel.removeAll(configResource, null, null);
				displayModel.removeAll(null, null, configResource);
				Resource contextResource = createResource(context
						.getContextUri());
				displayModel.removeAll(contextResource, null, null);
				displayModel.removeAll(null, null, contextResource);
			}
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
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			return (displayModel.getOntResource(uri) != null);
		}
	}

	/**
	 * Add labels, annotations, and whatever else we can find on the
	 * ObjectPropertyDisplayConfig.
	 */
	private void populateInstance(FauxProperty fp) {
		populateLabelsFromTBox(fp);
		populateFieldsFromDisplayModel(fp);
	}

	private void populateLabelsFromTBox(FauxProperty fp) {
		fp.setBaseLabel(findLabelForClass(fp.getBaseURI()));
		fp.setRangeLabel(findLabelForClass(fp.getRangeURI()));
		fp.setDomainLabel(findLabelForClass(fp.getDomainURI()));
	}

	private String findLabelForClass(String classUri) {
		if (classUri == null) {
			return null;
		} else {
			try (LockedOntModel tboxModel = models.getTBoxModel().read()) {
				return getPropertyStringValue(
						tboxModel.createOntResource(classUri), RDFS_LABEL);
			}
		}
	}

	private void populateFieldsFromDisplayModel(FauxProperty fp) {
		String configUri = locateConfigurationFromContext(fp.getContextUri());
		fp.setConfigUri(configUri);
		if (configUri != null) {
			try (LockedOntModel displayModel = models.getDisplayModel().read()) {
				OntResource config = displayModel.createOntResource(configUri);
				fp.setDisplayName(getPropertyStringValue(config, DISPLAY_NAME));
				fp.setPublicDescription(getPropertyStringValue(config,
						PUBLIC_DESCRIPTION_ANNOT));
				fp.setGroupURI(getSingleResourceURIValue(config, PROPERTY_GROUP));
				fp.setCustomListView(getPropertyStringValue(config,
						LIST_VIEW_FILE));
				fp.setDisplayTier(getPropertyIntValue(config,
						DISPLAY_RANK_ANNOT));
				fp.setDisplayLimit(getPropertyIntValue(config, DISPLAY_LIMIT));
				fp.setCollateBySubclass(Boolean.TRUE
						.equals(getPropertyBooleanValue(config,
								PROPERTY_COLLATEBYSUBCLASSANNOT)));
				fp.setSelectFromExisting(Boolean.TRUE
						.equals(getPropertyBooleanValue(config,
								PROPERTY_SELECTFROMEXISTINGANNOT)));
				fp.setOfferCreateNewOption(Boolean.TRUE
						.equals(getPropertyBooleanValue(config,
								PROPERTY_OFFERCREATENEWOPTIONANNOT)));
				fp.setCustomEntryForm(getPropertyStringValue(config,
						PROPERTY_CUSTOMENTRYFORMANNOT));

				fp.setHiddenFromDisplayBelowRoleLevel(getMostRestrictiveRoleLevel(
						config, HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT));
				fp.setHiddenFromPublishBelowRoleLevel(getMostRestrictiveRoleLevel(
						config, HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT));
				fp.setProhibitedFromUpdateBelowRoleLevel(getMostRestrictiveRoleLevel(
						config, PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT));
			}
		}
	}

	private String locateConfigurationFromContext(String contextUri) {
		try (LockedOntModel displayModel = models.getDisplayModel().read()) {
			Collection<String> configUris = getPropertyResourceURIValues(
					displayModel.createOntResource(contextUri),
					HAS_CONFIGURATION);
			if (configUris.isEmpty()) {
				return null;
			} else {
				return configUris.iterator().next();
			}
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

	private static final String QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context ?config \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedBy ?rangeUri ; \n" //
			+ "        :hasConfiguration ?config . \n" //
			+ "     FILTER NOT EXISTS { \n" //
			+ "        ?context :qualifiedByDomain ?domainUri \n" //
			+ "     } \n" //
			+ "} \n"; //

	private static class ParserLocateConfigContext extends
			ResultSetParser<Set<ConfigContext>> {
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
				LockableOntModel lockableDisplayModel, String domainUri,
				String baseUri, String rangeUri) {
			try (LockedOntModel displayModel = lockableDisplayModel.read()) {
				QueryHolder qHolder;
				if (domainUri == null || domainUri.trim().isEmpty()
						|| domainUri.equals(OWL.Thing.getURI())) {
					qHolder = queryHolder(
							QUERY_LOCATE_CONFIG_CONTEXT_WITH_NO_DOMAIN)
							.bindToUri("baseUri", baseUri).bindToUri(
									"rangeUri", rangeUri);
				} else {
					qHolder = queryHolder(
							QUERY_LOCATE_CONFIG_CONTEXT_WITH_DOMAIN)
							.bindToUri("baseUri", baseUri)
							.bindToUri("rangeUri", rangeUri)
							.bindToUri("domainUri", domainUri);
				}
				if (log.isDebugEnabled()) {
					log.debug("domainUri=" + domainUri + ", baseUri=" + baseUri
							+ ", rangeUri=" + rangeUri + ", qHolder=" + qHolder);
				}

				ParserLocateConfigContext parser = new ParserLocateConfigContext(
						domainUri, baseUri, rangeUri);
				Set<ConfigContext> contexts = createSelectQueryContext(
						displayModel, qHolder).execute().parse(parser);

				log.debug("found " + contexts.size() + " contexts: " + contexts);
				return contexts;
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

		@Override
		public String toString() {
			return "ConfigContext[contextUri=" + contextUri + ", configUri="
					+ configUri + ", domainUri=" + domainUri + ", baseUri="
					+ baseUri + ", rangeUri=" + rangeUri + "]";
		}

	}
}
