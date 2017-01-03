/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import static edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext.BROWSE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter;

/**
 * Read a config file that describes the short views. Read it into a model and
 * scan the model to determine what each view consists of (data getter URIs,
 * template names), what context each view applies to, and what classes map to
 * each view.
 * 
 * Data getters must be SparqlQueryDataGetters, and must be described in the
 * same config file.
 * 
 * TODO Get rid of this when the Application Ontology is implemented.
 */
public class FakeApplicationOntologyService {
	private static final Log log = LogFactory
			.getLog(FakeApplicationOntologyService.class);

	public static final String FILE_OF_SHORT_VIEW_INFO = "/WEB-INF/resources/shortview_config.n3";

	private static final String NS = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#";
	private static final String HAS_TEMPLATE = NS + "hasTemplate";
	private static final String CUSTOM_VIEW = NS + "customViewForIndividual";
	private static final String APPLIES_TO = NS + "appliesToContext";
	private static final String HAS_DATA_GETTER = NS + "hasDataGetter";
	private static final String HAS_VIEW = NS + "hasCustomView";
	private static final String RDF_TYPE = VitroVocabulary.RDF_TYPE;

	private final OntModel viewModel;
	private final Map<String, List<ViewSpec>> classUriToViewSpecs;

	/**
	 * Load the model from the config file, and inspect it for Views and
	 * mappings.
	 * 
	 * Keep the model - we'll need it when its time to create the DataGetters
	 * (on each request).
	 */
	public FakeApplicationOntologyService(ServletContext ctx)
			throws ShortViewConfigException {
		this.viewModel = createModelFromFile(ctx);

		Map<String, ViewSpec> viewSpecsByUri = createViewSpecs();
		this.classUriToViewSpecs = createClassMappings(viewSpecsByUri);

		if (log.isDebugEnabled()) {
			log.debug("Mapping: " + classUriToViewSpecs);
		}
	}

	/**
	 * If we fail to parse the config file, use this constructor instead, to
	 * simulate an empty config file.
	 */
	public FakeApplicationOntologyService() {
		this.viewModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		this.classUriToViewSpecs = new HashMap<String, List<ViewSpec>>();

		log.debug("Created empty FakeApplicationOntologyService.");
	}

	/**
	 * Load the short view config file into an OntModel.
	 */
	private OntModel createModelFromFile(ServletContext ctx)
			throws ShortViewConfigException {
		InputStream stream = ctx.getResourceAsStream(FILE_OF_SHORT_VIEW_INFO);
		if (stream == null) {
			throw new ShortViewConfigException("The short view config file "
					+ "doesn't exist in the servlet context: '"
					+ FILE_OF_SHORT_VIEW_INFO + "'");
		}

		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		try {
			m.read(stream, null, "N3");
		} catch (Exception e) {
			throw new ShortViewConfigException(
					"Parsing error in the short view config file.", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.debug("Loaded " + m.size() + " statements");
		return m;
	}

	/**
	 * Find all of the views.
	 */
	private Map<String, ViewSpec> createViewSpecs()
			throws ShortViewConfigException {
		Property rdfType = viewModel.getProperty(RDF_TYPE);
		Property appliesTo = viewModel.getProperty(APPLIES_TO);
		Property dataGetter = viewModel.getProperty(HAS_DATA_GETTER);
		Property template = viewModel.getProperty(HAS_TEMPLATE);
		Resource customView = viewModel.getResource(CUSTOM_VIEW);

		ResIterator views = viewModel.listResourcesWithProperty(rdfType,
				customView);
		try {
			Map<String, ViewSpec> map = new HashMap<String, ViewSpec>();
			while (views.hasNext()) {
				Resource view = views.next();
				List<String> contextNames = getDataPropertyValues(view,
						appliesTo, "context");
				List<ShortViewContext> contexts = contextsFromNames(view,
						contextNames);
				List<String> dataGetterUris = getObjectPropertyValues(view,
						dataGetter, "data getter");
				String tn = getDataProperty(view, template);
				map.put(view.getURI(), new ViewSpec(view.getURI(), contexts,
						dataGetterUris, tn));
			}
			return map;
		} finally {
			views.close();
		}
	}

	/**
	 * Got a list of context names. Make sure that each one actually represents
	 * a known context.
	 */
	private List<ShortViewContext> contextsFromNames(Resource view,
			List<String> contextNames) throws ShortViewConfigException {
		List<ShortViewContext> list = new ArrayList<ShortViewContext>();
		for (String name : contextNames) {
			ShortViewContext context = ShortViewContext.fromString(name);
			if (context == null) {
				throw new ShortViewConfigException("Unrecognized context '"
						+ name + "' for view '" + view.getURI() + "'");
			}
			list.add(context);
		}
		return list;
	}

	/**
	 * Create a map of classes to views.
	 */
	private Map<String, List<ViewSpec>> createClassMappings(
			Map<String, ViewSpec> viewSpecsByUri)
			throws ShortViewConfigException {
		Property hasView = viewModel.getProperty(HAS_VIEW);

		StmtIterator stmts = viewModel.listStatements(null, hasView,
				(RDFNode) null);
		try {
			Map<String, List<ViewSpec>> map = new HashMap<String, List<ViewSpec>>();
			while (stmts.hasNext()) {
				Statement s = stmts.next();

				String classUri = s.getSubject().getURI();

				RDFNode node = s.getObject();
				if (!node.isResource()) {
					throw new ShortViewConfigException("The hasCustomView"
							+ " property for '" + classUri
							+ "' must be a resource.");
				}
				String viewUri = node.asResource().getURI();

				ViewSpec view = viewSpecsByUri.get(viewUri);
				if (view == null) {
					throw new ShortViewConfigException("On '" + classUri
							+ "', the view '" + viewUri + "' does not exist.");
				}

				if (!map.containsKey(classUri)) {
					map.put(classUri, new ArrayList<ViewSpec>());
				}
				map.get(classUri).add(view);
			}
			return map;
		} finally {
			stmts.close();
		}
	}

	private String getDataProperty(Resource subject, Property predicate)
			throws ShortViewConfigException {
		Statement s = viewModel.getProperty(subject, predicate);
		if (s == null) {
			throw new ShortViewConfigException("The required property '"
					+ predicate.getURI() + "' is not present for '"
					+ subject.getURI() + "'");
		}
		RDFNode node = s.getObject();
		if (!node.isLiteral())
			throw new ShortViewConfigException("The value of '"
					+ predicate.getURI() + "' for '" + subject.getURI()
					+ "' must be a literal.");
		return node.asLiteral().getString();
	}

	private List<String> getDataPropertyValues(Resource subject,
			Property predicate, String label) throws ShortViewConfigException {
		StmtIterator stmts = viewModel.listStatements(subject, predicate,
				(RDFNode) null);
		try {
			List<String> list = new ArrayList<String>();
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode node = stmt.getObject();
				if (!node.isLiteral()) {
					throw new ShortViewConfigException("The " + label
							+ " property for '" + subject.getURI()
							+ "' must be a literal.");
				}
				list.add(node.asLiteral().getString());
			}
			return list;
		} finally {
			stmts.close();
		}
	}

	private List<String> getObjectPropertyValues(Resource subject,
			Property predicate, String label) throws ShortViewConfigException {
		StmtIterator stmts = viewModel.listStatements(subject, predicate,
				(RDFNode) null);
		try {
			List<String> list = new ArrayList<String>();
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode node = stmt.getObject();
				if (!node.isResource()) {
					throw new ShortViewConfigException("The " + label
							+ " property for '" + subject.getURI()
							+ "' must be a resource.");
				}
				list.add(node.asResource().getURI());
			}
			return list;
		} finally {
			stmts.close();
		}
	}

	/**
	 * Return the template name and DataGetter instances associated with this
	 * class and this short view context. If none, return null.
	 */
	public TemplateAndDataGetters getShortViewProperties(VitroRequest vreq,
			Individual individual, String classUri, String contextName) {
		/*
		 * If we have a mapping for this class that applies to this context,
		 * construct the DataGetter instances and return them with the template.
		 */
		if (classUriToViewSpecs.containsKey(classUri)) {
			for (ViewSpec view : classUriToViewSpecs.get(classUri)) {
				for (ShortViewContext context : view.getContexts()) {
					if (context.name().equalsIgnoreCase(contextName)) {
						List<DataGetter> dgList = new ArrayList<DataGetter>();
						for (String dgUri : view.getDataGetterUris()) {
							dgList.add(new SparqlQueryDataGetter(vreq,
									viewModel, dgUri));
						}
						return new TemplateAndDataGetters(
								view.getTemplateName(),
								dgList.toArray(new DataGetter[0]));
					}
				}
			}
		}

		/*
		 * Otherwise, check for this hard-coded kluge. Any class in the People
		 * class group gets a special view with preferred title.
		 */
		if ((BROWSE.name().equals(contextName))
				&& (isClassInPeopleClassGroup(vreq.getWebappDaoFactory(),
						classUri))) {
			return new TemplateAndDataGetters("view-browse-people.ftl",
					new FakeVivoPeopleDataGetter(vreq, individual.getURI()));
		}

		/*
		 * Otherwise, no custom view.
		 */
		return null;
	}

	private boolean isClassInPeopleClassGroup(WebappDaoFactory wadf,
			String classUri) {
		if (wadf == null) {
			log.debug("isClassInPeopleClassGroup: WebappDaoFactory is null.");
			return false;
		}

		VClassDao vcDao = wadf.getVClassDao();
		if (vcDao == null) {
			log.debug("isClassInPeopleClassGroup: VClassDao is null.");
			return false;
		}

		VClass vclass = vcDao.getVClassByURI(classUri);
		if (vclass == null) {
			log.debug("isClassInPeopleClassGroup: VClass is null.");
			return false;
		}

		String vclassGroupUri = vclass.getGroupURI();
		if (vclassGroupUri == null) {
			log.debug("isClassInPeopleClassGroup: vclassGroupUri is null.");
			return false;
		}

		boolean isPeople = PEOPLE_CLASSGROUP_URI.equals(vclassGroupUri);
		log.debug("isClassInPeopleClassGroup: isPeople = " + isPeople);
		return isPeople;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/** The info associated with a short view. */
	public static class TemplateAndDataGetters {
		private final String templateName;
		private final Set<DataGetter> dataGetters;

		public TemplateAndDataGetters(String templateName,
				DataGetter... dataGetters) {
			this.templateName = templateName;
			this.dataGetters = new HashSet<DataGetter>(
					Arrays.asList(dataGetters));
		}

		public String getTemplateName() {
			return templateName;
		}

		public Set<DataGetter> getDataGetters() {
			return dataGetters;
		}

		@Override
		public String toString() {
			return "[template=" + templateName + ", dataGetters=" + dataGetters
					+ "]";
		}

	}

	/** The view specifications that we read from the config file. */
	private class ViewSpec {
		private final String uri;
		private final List<ShortViewContext> contexts;
		private final List<String> dataGetterUris;
		private final String templateName;

		public ViewSpec(String uri, List<ShortViewContext> contexts,
				List<String> dataGetterUris, String templateName) {
			this.uri = uri;
			this.contexts = contexts;
			this.dataGetterUris = dataGetterUris;
			this.templateName = templateName;
		}

		public List<ShortViewContext> getContexts() {
			return contexts;
		}

		public List<String> getDataGetterUris() {
			return dataGetterUris;
		}

		public String getTemplateName() {
			return templateName;
		}

		@Override
		public String toString() {
			return "ViewSpec[uri='" + uri + "', contexts=" + contexts
					+ ", dataGetterUris=" + dataGetterUris + ", templateName='"
					+ templateName + "']";
		}

	}

	/** A custom exception that says something was wrong with the config file. */
	public class ShortViewConfigException extends Exception {
		public ShortViewConfigException(String message) {
			super(message);
		}

		public ShortViewConfigException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final String PEOPLE_CLASSGROUP_URI = "http://vivoweb.org/ontology#vitroClassGrouppeople";

	/**
	 * A special data getter to support the kluge case of browsing an individual
	 * that belongs to the People class group.
	 * 
	 * A SPARQL query data getter that initializes itself from its own private
	 * "display model". The query finds a preferred title for the individual.
	 */
	private static class FakeVivoPeopleDataGetter extends SparqlQueryDataGetter {
		private static String QUERY_STRING = ""
				+ "PREFIX obo: <http://purl.obolibrary.org/obo/> \n"
				+ "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>  \n"
				+ "SELECT ?pt  \n" + "WHERE {  \n"
				+ "    ?uri obo:ARG_2000028 ?vIndividual .  \n"
				+ "    ?vIndividual vcard:hasTitle ?vTitle . \n"
				+ "    ?vTitle vcard:title ?pt . \n" + "} LIMIT 1";

		private static final String FAKE_VIVO_PEOPLE_DATA_GETTER_URI = "http://FakeVivoPeopleDataGetter";

		private static OntModel fakeDisplayModel = initializeFakeDisplayModel();

		private static OntModel initializeFakeDisplayModel() {
			OntModel m = ModelFactory
					.createOntologyModel(OntModelSpec.OWL_DL_MEM);

			Resource dataGetter = m
					.getResource(FAKE_VIVO_PEOPLE_DATA_GETTER_URI);
			Property queryProperty = m.getProperty(DisplayVocabulary.QUERY);
			Property saveToVarProperty = m
					.getProperty(DisplayVocabulary.SAVE_TO_VAR);

			m.add(dataGetter, queryProperty, QUERY_STRING);
			m.add(dataGetter, saveToVarProperty, "extra");
			return m;
		}

		private String individualUri;
		private VitroRequest vreq;
		private ServletContext ctx;

		public FakeVivoPeopleDataGetter(VitroRequest vreq, String individualUri) {
			super(vreq, fakeDisplayModel, "http://FakeVivoPeopleDataGetter");
			this.individualUri = individualUri;
			this.vreq = vreq;
			this.ctx = vreq.getSession().getServletContext();
		}

		@Override
		public Map<String, Object> getData(Map<String, Object> pageData) {
			Map<String, Object> parms = new HashMap<>();
			parms.put("uri", individualUri);

			return super.getData(parms);
		}

	}

}
