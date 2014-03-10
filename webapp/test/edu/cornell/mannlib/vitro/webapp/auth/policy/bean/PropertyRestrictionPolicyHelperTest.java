/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.CURATOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.DB_ADMIN;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.EDITOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.NOBODY;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.PUBLIC;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.SELF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Check that the bean gets built properly, and check that the bean works
 * properly.
 */
public class PropertyRestrictionPolicyHelperTest extends AbstractTestClass {
	private static final Log log = LogFactory
			.getLog(PropertyRestrictionPolicyHelperTest.class);

	private static final String PROPERTY_DISPLAY_THRESHOLD = VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT;
	private static final String PROPERTY_MODIFY_THRESHOLD = VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT;
	private static final String PROPERTY_PUBLISH_THRESHOLD = VitroVocabulary.HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT;

	private static final String[] PROHIBITED_NAMESPACES = new String[] {
			VitroVocabulary.vitroURI, "" };

	private static final String[] PERMITTED_EXCEPTIONS = new String[] { VitroVocabulary.MONIKER };

	private OntModel ontModel;
	private ModelWrapper wrapper;
	private PropertyRestrictionPolicyHelper bean;

	@Before
	public void setLoggingLevel() {
		// setLoggerLevel(PropertyRestrictionPolicyHelper.class, Level.DEBUG);
	}

	private void mapPut(String predicateURI, RoleLevel roleLevel,
			Map<Pair<String, Pair<String, String>>, RoleLevel> map) {
		map.put(new Pair<String, Pair<String, String>>(OWL.Thing.getURI(),
				new Pair<String, String>(predicateURI, OWL.Thing.getURI())),
				roleLevel);
	}

	@Before
	public void createTheBean() {
		Map<Pair<String, Pair<String, String>>, RoleLevel> displayLevels = new HashMap<>();
		mapPut("http://predicates#display_curator", CURATOR, displayLevels);
		mapPut("http://predicates#display_hidden", NOBODY, displayLevels);

		Map<Pair<String, Pair<String, String>>, RoleLevel> modifyLevels = new HashMap<>();
		mapPut("http://predicates#modify_self", SELF, modifyLevels);
		mapPut("http://predicates#modify_curator", CURATOR, modifyLevels);
		mapPut("http://predicates#modify_hidden", NOBODY, modifyLevels);

		Map<Pair<String, Pair<String, String>>, RoleLevel> publishLevels = new HashMap<>();
		mapPut("http://predicates#publish_curator", CURATOR, publishLevels);
		mapPut("http://predicates#publish_hidden", NOBODY, publishLevels);

		bean = new PropertyRestrictionPolicyHelper(
				Arrays.asList(PROHIBITED_NAMESPACES),
				Arrays.asList(PERMITTED_EXCEPTIONS), displayLevels,
				modifyLevels, publishLevels);
	}

	@Before
	public void createTheModel() {
		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		wrapper = new ModelWrapper(ontModel);

		wrapper.add("http://thresholds#display_public",
				PROPERTY_DISPLAY_THRESHOLD, PUBLIC.getURI());
		wrapper.add("http://thresholds#display_hidden",
				PROPERTY_DISPLAY_THRESHOLD, NOBODY.getURI());

		wrapper.add("http://thresholds#modify_editor",
				PROPERTY_MODIFY_THRESHOLD, EDITOR.getURI());
		wrapper.add("http://thresholds#modify_curator",
				PROPERTY_MODIFY_THRESHOLD, CURATOR.getURI());
		
		wrapper.add("http://thresholds#publish_public",
				PROPERTY_PUBLISH_THRESHOLD, PUBLIC.getURI());
		wrapper.add("http://thresholds#publish_hidden",
				PROPERTY_PUBLISH_THRESHOLD, NOBODY.getURI());
	}

	// ----------------------------------------------------------------------
	// test the bean
	// ----------------------------------------------------------------------

	@Test
	public void displayResource() {
		assertEquals("display a random resource", true,
				bean.canDisplayResource("http://someRandom#string", null));
	}

	@Test
	public void modifyResourceNoRestriction() {
		assertEquals("modify a random resource", true,
				bean.canModifyResource("http://someRandom#string", null));
	}

	@Test
	public void modifyResourceProhibitedNamespace() {
		assertEquals("modify a prohibited resource", false,
				bean.canModifyResource(PROHIBITED_NAMESPACES[0] + "random",
						null));
	}

	@Test
	public void publishResource() {
		assertEquals("publish a random resource", true,
				bean.canPublishResource("http://someRandom#string", null));
	}

	@Test
	public void modifyResourcePermittedException() {
		assertEquals("modify a exception resource", true,
				bean.canModifyResource(PERMITTED_EXCEPTIONS[0], null));
	}

	@Test
	public void displayPredicateNoRestriction() {
		assertEquals("displayPredicate: open", true, bean.canDisplayPredicate(
				createVitroProperty("http://predicates#open"), PUBLIC));
	}

	@Test
	public void displayPredicateRestrictionLower() {
		assertEquals("displayPredicate: lower restriction", true,
				bean.canDisplayPredicate(
						createVitroProperty("http://predicates#display_self"),
						CURATOR));
	}

	@Test
	public void displayPredicateRestrictionEqual() {
		assertEquals(
				"displayPredicate: equal restriction",
				true,
				bean.canDisplayPredicate(
						createVitroProperty("http://predicates#display_curator"),
						CURATOR));
	}

	@Test
	public void displayPredicateRestrictionHigher() {
		assertEquals(
				"displayPredicate: higher restriction",
				false,
				bean.canDisplayPredicate(
						createVitroProperty("http://predicates#display_hidden"),
						CURATOR));
	}

	@Test
	public void modifyPredicateNoRestriction() {
		assertEquals("modifyPredicate: open", true, bean.canModifyPredicate(
				new edu.cornell.mannlib.vitro.webapp.beans.Property(
						"http://predicates#open"), PUBLIC));
	}

	@Test
	public void modifyPredicateRestrictionLower() {
		assertEquals("modifyPredicate: lower restriction", true,
				bean.canModifyPredicate(
						new edu.cornell.mannlib.vitro.webapp.beans.Property(
								"http://predicates#modify_self"), CURATOR));
	}

	@Test
	public void modifyPredicateRestrictionEqual() {
		assertEquals("modifyPredicate: equal restriction", true,
				bean.canModifyPredicate(
						new edu.cornell.mannlib.vitro.webapp.beans.Property(
								"http://predicates#modify_curator"), CURATOR));
	}

	@Test
	public void modifyPredicateRestrictionHigher() {
		assertEquals("modifyPredicate: higher restriction", false,
				bean.canModifyPredicate(
						new edu.cornell.mannlib.vitro.webapp.beans.Property(
								"http://predicates#modify_hidden"), CURATOR));
	}

	@Test
	public void modifyPredicateProhibitedNamespace() {
		assertEquals(
				"modifyPredicate: prohibited namespace",
				false,
				bean.canModifyPredicate(
						new edu.cornell.mannlib.vitro.webapp.beans.Property(
								PROHIBITED_NAMESPACES[0] + "randoom"), DB_ADMIN));
	}

	@Test
	public void modifyPredicatePermittedException() {
		assertEquals("modifyPredicate: permitted exception", true,
				bean.canModifyPredicate(
						new edu.cornell.mannlib.vitro.webapp.beans.Property(
								PERMITTED_EXCEPTIONS[0]), DB_ADMIN));
	}

	@Test
	public void publishPredicateNoRestriction() {
		assertEquals("publishPredicate: open", true, bean.canPublishPredicate(
				createVitroProperty("http://predicates#open"), PUBLIC));
	}

	@Test
	public void publishPredicateRestrictionLower() {
		assertEquals("publishPredicate: lower restriction", true,
				bean.canPublishPredicate(
						createVitroProperty("http://predicates#publish_self"),
						CURATOR));
	}

	@Test
	public void publishPredicateRestrictionEqual() {
		assertEquals(
				"publishPredicate: equal restriction",
				true,
				bean.canPublishPredicate(
						createVitroProperty("http://predicates#publish_curator"),
						CURATOR));
	}

	@Test
	public void publishPredicateRestrictionHigher() {
		assertEquals(
				"publishPredicate: higher restriction",
				false,
				bean.canPublishPredicate(
						createVitroProperty("http://predicates#publish_hidden"),
						CURATOR));
	}

	// ----------------------------------------------------------------------
	// test the bean builder
	// ----------------------------------------------------------------------

	@Test
	public void buildDisplayThresholds() {
		Map<Pair<String, Pair<String, String>>, BaseResourceBean.RoleLevel> expectedMap = new HashMap<>();
		mapPut("http://thresholds#display_public", PUBLIC, expectedMap);
		mapPut("http://thresholds#display_hidden", NOBODY, expectedMap);

		Map<String, RoleLevel> actualMap = populateThresholdMap(PROPERTY_DISPLAY_THRESHOLD);
		assertEquals("display thresholds", expectedMap, actualMap);
	}

	@Test
	public void buildModifyThresholds() {
		Map<Pair<String, Pair<String, String>>, BaseResourceBean.RoleLevel> expectedMap = new HashMap<>();
		mapPut("http://thresholds#modify_editor", EDITOR, expectedMap);
		mapPut("http://thresholds#modify_curator", CURATOR, expectedMap);

		Map<String, RoleLevel> actualMap = populateThresholdMap(PROPERTY_MODIFY_THRESHOLD);
		assertEquals("modify thresholds", expectedMap, actualMap);
	}

	@Test
	public void buildPublishThresholds() {
		Map<Pair<String, Pair<String, String>>, BaseResourceBean.RoleLevel> expectedMap = new HashMap<>();
		mapPut("http://thresholds#publish_public", PUBLIC, expectedMap);
		mapPut("http://thresholds#publish_hidden", NOBODY, expectedMap);

		Map<String, RoleLevel> actualMap = populateThresholdMap(PROPERTY_PUBLISH_THRESHOLD);
		assertEquals("publish thresholds", expectedMap, actualMap);
	}

	/** Invoke the private static method "populateThresholdMap" */
	private Map<String, RoleLevel> populateThresholdMap(String propertyUri) {
		Map<String, RoleLevel> map = new HashMap<String, BaseResourceBean.RoleLevel>();
		try {
			Class<?> clazz = PropertyRestrictionPolicyHelper.class;
			Method method = clazz.getDeclaredMethod("populateThresholdMap",
					OntModel.class, Map.class, String.class);
			method.setAccessible(true);
			method.invoke(null, ontModel, map, propertyUri);
			return map;
		} catch (Exception e) {
			fail("failed to populate the map: propertyUri='" + propertyUri
					+ "', " + e);
			return null;
		}
	}

	private static class ModelWrapper {
		private final OntModel model;

		public ModelWrapper(OntModel model) {
			this.model = model;
		}

		public void add(String subjectUri, String propertyUri, String objectUri) {
			Resource subject = model.getResource(subjectUri);
			Property property = model.getProperty(propertyUri);
			Resource object = model.getResource(objectUri);
			model.add(subject, property, object);
		}
	}

	private edu.cornell.mannlib.vitro.webapp.beans.Property createVitroProperty(
			String propertyURI) {
		return new edu.cornell.mannlib.vitro.webapp.beans.Property(propertyURI);
	}
}
