/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.services.shortview;

import static edu.cornell.mannlib.vitro.webapp.services.shortview.ShortViewService.ShortViewContext.BROWSE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter;

/**
 * TODO
 * 
 * Get rid of this when the Application Ontology is implemented.
 */
public class FakeApplicationOntologyService {
	private static final Log log = LogFactory
			.getLog(FakeApplicationOntologyService.class);

	private static final String FACULTY_MEMBER_CLASS_URI = "http://vivoweb.org/ontology/core#FacultyMember";
	private static final String PEOPLE_CLASSGROUP_URI = "http://vivoweb.org/ontology#vitroClassGrouppeople";

	/**
	 * Return the template name and DataGetter instances associated with this
	 * class and this short view context. If none, return null.
	 */
	public TemplateAndDataGetters getShortViewProperties(VitroRequest vreq,
			Individual individual, String classUri, String contextName) {
		if ((BROWSE.name().equals(contextName))
				&& (isClassInPeopleClassGroup(vreq.getWebappDaoFactory(),
						classUri))) {
			return new TemplateAndDataGetters("view-browse-people.ftl",
					new FakeVivoPeopleDataGetter(vreq, individual.getURI()));
		}
		// A mockup of Tammy's use case.
		// if ((SEARCH.name().equals(contextName))
		// && (classUri.equals(FACULTY_MEMBER_CLASS_URI))) {
		// return new TemplateAndDataGetters("view-search-faculty.ftl", new
		// FakeFacultyDataGetter());
		// } else {
		return null;
		// }
	}

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

	private static class FakeFacultyDataGetter implements DataGetter {
		@Override
		public Map<String, Object> getData(Map<String, Object> valueMap) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> extras = new HashMap<String, Object>();
			extras.put("departmentName", "Department of Redundancy Department");
			map.put("extra", extras);
			return map;
		}
	}

	/**
	 * A SPARQL query data getter that initializes itself from its own private
	 * "display model". The query finds a preferred title for the individual.
	 */
	private static class FakeVivoPeopleDataGetter extends SparqlQueryDataGetter {
		private static final String QUERY_STRING = "SELECT ?uri ?pt WHERE {\n"
				+ "   ?uri <http://vivoweb.org/ontology/core#preferredTitle> ?pt\n"
				+ "} LIMIT 1";

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
			Map<String, String[]> parms = new HashMap<String, String[]>();
			parms.put("uri", new String[] { individualUri });

			return doQuery(parms, getModel(ctx, vreq, null));
		}

	}

}
