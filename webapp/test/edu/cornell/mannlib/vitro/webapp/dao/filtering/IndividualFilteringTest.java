/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.CURATOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.DB_ADMIN;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.EDITOR;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.PUBLIC;
import static edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel.SELF;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HiddenFromDisplayBelowRoleLevelFilter;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

/**
 * Test the filtering of IndividualFiltering.
 * 
 * There are 6 levels of data hiding - public, selfEditor, editor, curator,
 * dbAdmin and nobody.
 * 
 * The data files for this test describe an Individual with 6 data properties,
 * each with a different hiding level, and 36 object properties, showing all
 * combinations of hiding levels for the property and for the class of the
 * object.
 * 
 * There is a flag in HiddenFromDisplayBelowRoleLevelFilter which
 * enables/disables filtering based on the class of the object. These tests
 * should work regardless of how that flag is set.
 */
@RunWith(value = Parameterized.class)
public class IndividualFilteringTest extends AbstractTestClass {
	private static final Log log = LogFactory
			.getLog(IndividualFilteringTest.class);

	// ----------------------------------------------------------------------
	// Data elements and creating the model.
	// ----------------------------------------------------------------------

	/**
	 * Where the ontology statements are stored for this test.
	 */
	private static final String TBOX_DATA_FILENAME = "IndividualFilteringTest-TBoxAnnotations.n3";

	/**
	 * Where the model statements are stored for this test.
	 */
	private static final String ABOX_DATA_FILENAME = "IndividualFilteringTest-Abox.n3";

	/**
	 * The domain where all of the objects and properties are defined.
	 */
	private static final String NS = "http://vivo.mydomain.edu/individual/";

	/**
	 * The individual we are reading.
	 */
	private static final String INDIVIDUAL_URI = mydomain("bozo");

	/**
	 * Data properties to look for.
	 */
	private static final String PUBLIC_DATA_PROPERTY = mydomain("publicDataProperty");
	private static final String SELF_DATA_PROPERTY = mydomain("selfDataProperty");
	private static final String EDITOR_DATA_PROPERTY = mydomain("editorDataProperty");
	private static final String CURATOR_DATA_PROPERTY = mydomain("curatorDataProperty");
	private static final String DBA_DATA_PROPERTY = mydomain("dbaDataProperty");
	private static final String HIDDEN_DATA_PROPERTY = mydomain("hiddenDataProperty");
	private static final String[] DATA_PROPERTIES = { PUBLIC_DATA_PROPERTY,
			SELF_DATA_PROPERTY, EDITOR_DATA_PROPERTY, CURATOR_DATA_PROPERTY,
			DBA_DATA_PROPERTY, HIDDEN_DATA_PROPERTY };

	/**
	 * Object properties to look for.
	 */
	private static final String PUBLIC_OBJECT_PROPERTY = mydomain("publicObjectProperty");
	private static final String SELF_OBJECT_PROPERTY = mydomain("selfObjectProperty");
	private static final String EDITOR_OBJECT_PROPERTY = mydomain("editorObjectProperty");
	private static final String CURATOR_OBJECT_PROPERTY = mydomain("curatorObjectProperty");
	private static final String DBA_OBJECT_PROPERTY = mydomain("dbaObjectProperty");
	private static final String HIDDEN_OBJECT_PROPERTY = mydomain("hiddenObjectProperty");
	private static final String[] OBJECT_PROPERTIES = { PUBLIC_OBJECT_PROPERTY,
			SELF_OBJECT_PROPERTY, EDITOR_OBJECT_PROPERTY,
			CURATOR_OBJECT_PROPERTY, DBA_OBJECT_PROPERTY,
			HIDDEN_OBJECT_PROPERTY };

	/**
	 * Objects to look for.
	 */
	private static final String PUBLIC_OBJECT = mydomain("publicObject");
	private static final String SELF_OBJECT = mydomain("selfObject");
	private static final String EDITOR_OBJECT = mydomain("editorObject");
	private static final String CURATOR_OBJECT = mydomain("curatorObject");
	private static final String DBA_OBJECT = mydomain("dbaObject");
	private static final String HIDDEN_OBJECT = mydomain("hiddenObject");
	private static final String[] OBJECTS = { PUBLIC_OBJECT, SELF_OBJECT,
			EDITOR_OBJECT, CURATOR_OBJECT, DBA_OBJECT, HIDDEN_OBJECT };

	private static TestData publicTestData() {
		TestData data = new TestData(PUBLIC);
		data.addExpectedDataProperties(PUBLIC_DATA_PROPERTY);
		data.addExpectedObjectProperties(PUBLIC_OBJECT_PROPERTY);
		data.addExpectedObjects(PUBLIC_OBJECT);
		return data;
	}

	private static String mydomain(String localname) {
		return NS + localname;
	}

	private static TestData selfTestData() {
		TestData data = new TestData(SELF);
		data.addExpectedDataProperties(PUBLIC_DATA_PROPERTY, SELF_DATA_PROPERTY);
		data.addExpectedObjectProperties(PUBLIC_OBJECT_PROPERTY,
				SELF_OBJECT_PROPERTY);
		data.addExpectedObjects(PUBLIC_OBJECT, SELF_OBJECT);
		return data;
	}

	private static TestData editorTestData() {
		TestData data = new TestData(EDITOR);
		data.addExpectedDataProperties(PUBLIC_DATA_PROPERTY,
				SELF_DATA_PROPERTY, EDITOR_DATA_PROPERTY);
		data.addExpectedObjectProperties(PUBLIC_OBJECT_PROPERTY,
				SELF_OBJECT_PROPERTY, EDITOR_OBJECT_PROPERTY);
		data.addExpectedObjects(PUBLIC_OBJECT, SELF_OBJECT, EDITOR_OBJECT);
		return data;
	}

	private static TestData curatorTestData() {
		TestData data = new TestData(CURATOR);
		data.addExpectedDataProperties(PUBLIC_DATA_PROPERTY,
				SELF_DATA_PROPERTY, EDITOR_DATA_PROPERTY, CURATOR_DATA_PROPERTY);
		data.addExpectedObjectProperties(PUBLIC_OBJECT_PROPERTY,
				SELF_OBJECT_PROPERTY, EDITOR_OBJECT_PROPERTY,
				CURATOR_OBJECT_PROPERTY);
		data.addExpectedObjects(PUBLIC_OBJECT, SELF_OBJECT, EDITOR_OBJECT,
				CURATOR_OBJECT);
		return data;
	}

	private static TestData dbaTestData() {
		TestData data = new TestData(DB_ADMIN);
		data.addExpectedDataProperties(PUBLIC_DATA_PROPERTY,
				SELF_DATA_PROPERTY, EDITOR_DATA_PROPERTY,
				CURATOR_DATA_PROPERTY, DBA_DATA_PROPERTY);
		data.addExpectedObjectProperties(PUBLIC_OBJECT_PROPERTY,
				SELF_OBJECT_PROPERTY, EDITOR_OBJECT_PROPERTY,
				CURATOR_OBJECT_PROPERTY, DBA_OBJECT_PROPERTY);
		data.addExpectedObjects(PUBLIC_OBJECT, SELF_OBJECT, EDITOR_OBJECT,
				CURATOR_OBJECT, DBA_OBJECT);
		return data;
	}

	private static OntModelSelectorImpl ontModelSelector;

	@BeforeClass
	public static void createTheModels() throws IOException {
		ontModelSelector = new OntModelSelectorImpl();
		ontModelSelector.setABoxModel(createAboxModel());
		ontModelSelector.setTBoxModel(createTboxModel());
		ontModelSelector.setFullModel(mergeModels(ontModelSelector));
	}

	private static OntModel createAboxModel() throws IOException {
		OntModel ontModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		readFileIntoModel(ontModel, ABOX_DATA_FILENAME, "N3");

		dumpModel(ontModel);

		return ontModel;
	}

	private static OntModel createTboxModel() throws IOException {
		OntModel ontModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		readFileIntoModel(ontModel, TBOX_DATA_FILENAME, "N3");

		return ontModel;
	}

	private static OntModel mergeModels(OntModelSelectorImpl selector) {
		OntModel ontModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		ontModel.add(selector.getABoxModel());
		ontModel.add(selector.getTBoxModel());

		return ontModel;
	}

	private static void readFileIntoModel(OntModel ontModel, String filename,
			String format) throws IOException {
		InputStream stream = IndividualFilteringTest.class
				.getResourceAsStream(filename);
		ontModel.read(stream, null, format);
		stream.close();
	}

	// ----------------------------------------------------------------------
	// Set up for each test
	// ----------------------------------------------------------------------

	@Parameters
	public static Collection<Object[]> data() {
		// return Arrays.asList(new Object[][] { { dbaTestData() } });
		return Arrays.asList(new Object[][] { { publicTestData() },
				{ selfTestData() }, { editorTestData() },
				{ curatorTestData() }, { dbaTestData() } });
	}

	private final TestData testData;
	private WebappDaoFactory wadf;
	private Individual ind;

	@Before
	public void createTheFilteredIndividual() {
		WebappDaoFactory rawWadf = new WebappDaoFactoryJena(ontModelSelector);
		wadf = new WebappDaoFactoryFiltering(rawWadf,
				new HiddenFromDisplayBelowRoleLevelFilter(testData.loginRole,
						rawWadf));
		ind = wadf.getIndividualDao().getIndividualByURI(INDIVIDUAL_URI);
	}

	public IndividualFilteringTest(TestData testData) {
		this.testData = testData;
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void testGetDataPropertyList() {
		assertEqualSets("data property list",
				testData.expectedDataPropertyUris,
				extractDataPropUris(ind.getDataPropertyList()));
	}

	@Test
	public void testGetPopulatedDataPropertyList() {
		assertEqualSets("populated data property list",
				testData.expectedDataPropertyUris,
				extractDataPropUris(ind.getPopulatedDataPropertyList()));
	}

	@Test
	public void testDataPropertyStatements() {
		assertEqualSets("data property statments",
				testData.expectedDataPropertyUris,
				extractDataPropStmtUris(ind.getDataPropertyStatements()));
	}

	@Test
	public void testDataPropertyStatements2() {
		for (String propUri : DATA_PROPERTIES) {
			Set<String> uris = extractDataPropStmtUris(ind
					.getDataPropertyStatements(propUri));
			if (testData.expectedDataPropertyUris.contains(propUri)) {
				assertEquals("selected data property: " + propUri,
						Collections.singleton(propUri), uris);
			} else {
				assertEquals("selected data property: " + propUri,
						Collections.emptySet(), uris);
			}
		}
	}

	@Test
	public void testDataPropertyMap() {
		assertEqualSets("data property map", testData.expectedDataPropertyUris,
				ind.getDataPropertyMap().keySet());
	}

	@Test
	public void testObjectPropertyList() {
		assertEqualSets("object properties",
				testData.expectedObjectPropertyUris,
				extractObjectPropUris(ind.getObjectPropertyList()));
	}

	@Test
	public void testPopulatedObjectPropertyList() {
		assertEqualSets("populated object properties",
				testData.expectedObjectPropertyUris,
				extractObjectPropUris(ind.getPopulatedObjectPropertyList()));
	}

	/**
	 * We expect to see an object property statment for each permitted property
	 * and each permitted object. If class filtering is disabled, then all
	 * objects are permitted.
	 */
	@Test
	public void testObjectPropertyStatements() {
		Collection<String> expectedObjects = filteringOnClasses() ? testData.expectedObjectUris
				: Arrays.asList(OBJECTS);
		assertExpectedObjectPropertyStatements("object property statements",
				testData.expectedObjectPropertyUris, expectedObjects,
				ind.getObjectPropertyStatements());
	}

	/**
	 * We expect to see an object property statment for each permitted property
	 * and each permitted object. If class filtering is disabled, then all
	 * objects are permitted.
	 */
	@Test
	public void testObjectPropertyStatements2() {
		Collection<String> expectedObjects = filteringOnClasses() ? testData.expectedObjectUris
				: Arrays.asList(OBJECTS);
		for (String propUri : OBJECT_PROPERTIES) {
			if (testData.expectedObjectPropertyUris.contains(propUri)) {
				assertExpectedObjectPropertyStatements(
						"object property statements for " + propUri,
						Collections.singleton(propUri), expectedObjects,
						ind.getObjectPropertyStatements(propUri));
			} else {
				assertExpectedObjectPropertyStatements(
						"object property statements for " + propUri,
						Collections.<String> emptySet(), expectedObjects,
						ind.getObjectPropertyStatements(propUri));
			}
		}
	}

	@Test
	public void testObjectPropertyMap() {
		assertEqualSets("object property map",
				testData.expectedObjectPropertyUris, ind.getObjectPropertyMap()
						.keySet());
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	/**
	 * Are we filtering on VClasses? Use reflection to read that protected
	 * constant.
	 */
	private boolean filteringOnClasses() {
		try {
			Class<?> clazz = HiddenFromDisplayBelowRoleLevelFilter.class;
			Field field = clazz
					.getDeclaredField("FILTER_ON_INDIVIDUAL_VCLASSES");
			field.setAccessible(true);
			return (Boolean) field.get(null);
		} catch (Exception e) {
			fail("Can't decide on class filtering: " + e);
			return false;
		}
	}

	/** Get the URIs from these DataProperties */
	private Set<String> extractDataPropUris(
			Collection<DataProperty> dataProperties) {
		Set<String> uris = new TreeSet<String>();
		if (dataProperties != null) {
			for (DataProperty dp : dataProperties) {
				uris.add(dp.getURI());
			}
		}
		return uris;
	}

	/** Get the URIs from these DataPropertyStatements */
	private Set<String> extractDataPropStmtUris(
			Collection<DataPropertyStatement> dataPropertyStatements) {
		Set<String> uris = new TreeSet<String>();
		if (dataPropertyStatements != null) {
			for (DataPropertyStatement dps : dataPropertyStatements) {
				uris.add(dps.getDatapropURI());
			}
		}
		return uris;
	}

	/** Get the URIs from these ObjectProperties */
	private Set<String> extractObjectPropUris(
			Collection<ObjectProperty> objectProperties) {
		Set<String> uris = new TreeSet<String>();
		if (objectProperties != null) {
			for (ObjectProperty op : objectProperties) {
				uris.add(op.getURI());
			}
		}
		return uris;
	}

	/** Get the URIs from these DataPropertyStatements */
	private Set<String> extractObjectPropStmtUris(
			Collection<ObjectPropertyStatement> objectPropertyStatements) {
		Set<String> uris = new TreeSet<String>();
		if (objectPropertyStatements != null) {
			for (ObjectPropertyStatement ops : objectPropertyStatements) {
				uris.add(ops.getPropertyURI());
			}
		}
		return uris;
	}

	/**
	 * We expect one statement for each combination of expected object
	 * properties and expected object.
	 */
	private void assertExpectedObjectPropertyStatements(String label,
			Collection<String> expectedPropertyUris,
			Collection<String> expectedObjectUris,
			List<ObjectPropertyStatement> actualStmts) {
		Set<ObjectPropertyStatementUris> actualStmtUris = new HashSet<ObjectPropertyStatementUris>();
		for (ObjectPropertyStatement actualStmt : actualStmts) {
			actualStmtUris.add(new ObjectPropertyStatementUris(actualStmt));
		}

		Set<ObjectPropertyStatementUris> expectedStmtUris = new HashSet<ObjectPropertyStatementUris>();
		for (String propertyUri : expectedPropertyUris) {
			for (String objectUri : expectedObjectUris) {
				expectedStmtUris.add(new ObjectPropertyStatementUris(ind
						.getURI(), propertyUri, objectUri));
			}
		}

		assertEqualSets(label, expectedStmtUris, actualStmtUris);
	}

	private static void dumpModel(OntModel ontModel) {
		if (log.isDebugEnabled()) {
			log.debug("Dumping the model:");
			StmtIterator stmtIt = ontModel.listStatements();
			while (stmtIt.hasNext()) {
				Statement stmt = stmtIt.next();
				log.debug("stmt: " + stmt);
			}
		}
	}

	/**
	 * The testing parameter. Each different role level will have different
	 * expectations of visible properties and objects.
	 */
	private static class TestData {
		final RoleLevel loginRole;
		final Set<String> expectedDataPropertyUris = new TreeSet<String>();
		final Set<String> expectedObjectPropertyUris = new TreeSet<String>();
		final Set<String> expectedObjectUris = new TreeSet<String>();

		public TestData(RoleLevel loginRole) {
			this.loginRole = loginRole;

		}

		public void addExpectedDataProperties(String... uris) {
			expectedDataPropertyUris.addAll(Arrays.asList(uris));
		}

		public void addExpectedObjectProperties(String... uris) {
			expectedObjectPropertyUris.addAll(Arrays.asList(uris));
		}

		public void addExpectedObjects(String... uris) {
			expectedObjectUris.addAll(Arrays.asList(uris));
		}
	}

	/**
	 * Capture the essence of an DataPropertyStatement for comparison and
	 * display.
	 */
	private static class DataPropertyStatementUris implements
			Comparable<DataPropertyStatementUris> {
		private final String subjectUri;
		private final String propertyUri;
		private final String data;

		DataPropertyStatementUris(DataPropertyStatement stmt) {
			this.subjectUri = stmt.getIndividualURI();
			this.propertyUri = stmt.getDatapropURI();
			this.data = stmt.getData();
		}

		public DataPropertyStatementUris(String subjectUri, String propertyUri,
				String data) {
			this.subjectUri = subjectUri;
			this.propertyUri = propertyUri;
			this.data = data;
		}

		@Override
		public int compareTo(DataPropertyStatementUris that) {
			int first = this.subjectUri.compareTo(that.subjectUri);
			if (first != 0) {
				return first;
			}

			int second = this.propertyUri.compareTo(that.propertyUri);
			if (second != 0) {
				return second;
			}

			int third = this.data.compareTo(that.data);
			return third;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof DataPropertyStatementUris)) {
				return false;
			}
			DataPropertyStatementUris that = (DataPropertyStatementUris) o;
			return this.compareTo(that) == 0;
		}

		@Override
		public int hashCode() {
			return subjectUri.hashCode() ^ propertyUri.hashCode()
					^ data.hashCode();
		}

		@Override
		public String toString() {
			return "[" + subjectUri + " ==> " + propertyUri + " ==> " + data
					+ "]";
		}

	}

	/**
	 * Capture the essence of an ObjectPropertyStatement for comparison and
	 * display.
	 */
	private static class ObjectPropertyStatementUris implements
			Comparable<ObjectPropertyStatementUris> {
		private final String subjectUri;
		private final String propertyUri;
		private final String objectUri;

		ObjectPropertyStatementUris(ObjectPropertyStatement stmt) {
			this.subjectUri = stmt.getSubjectURI();
			this.propertyUri = stmt.getPropertyURI();
			this.objectUri = stmt.getObjectURI();
		}

		public ObjectPropertyStatementUris(String subjectUri,
				String propertyUri, String objectUri) {
			this.subjectUri = subjectUri;
			this.propertyUri = propertyUri;
			this.objectUri = objectUri;
		}

		@Override
		public int compareTo(ObjectPropertyStatementUris that) {
			int first = this.subjectUri.compareTo(that.subjectUri);
			if (first != 0) {
				return first;
			}

			int second = this.propertyUri.compareTo(that.propertyUri);
			if (second != 0) {
				return second;
			}

			int third = this.objectUri.compareTo(that.objectUri);
			return third;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ObjectPropertyStatementUris)) {
				return false;
			}
			ObjectPropertyStatementUris that = (ObjectPropertyStatementUris) o;
			return this.compareTo(that) == 0;
		}

		@Override
		public int hashCode() {
			return subjectUri.hashCode() ^ propertyUri.hashCode()
					^ objectUri.hashCode();
		}

		@Override
		public String toString() {
			return "[" + subjectUri + " ==> " + propertyUri + " ==> "
					+ objectUri + "]";
		}

	}
}
