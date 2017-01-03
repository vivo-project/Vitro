/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDfloat;
import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationRdfParser.InvalidConfigurationRdfException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper.InstanceWrapperException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance.NoSuchPropertyMethodException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance.ResourceUnavailableException;

/**
 * TODO
 * 
 * Circularity prevention. Before setting properties, create a WeakMap of
 * instances by URIs, so if a property refers to a created instance, we just
 * pass it in.
 */
public class ConfigurationBeanLoaderTest extends
		ConfigurationBeanLoaderTestBase {

	// ----------------------------------------------------------------------
	// Constructor tests
	// ----------------------------------------------------------------------

	@Test
	public void constructor_modelIsNull_throwsException() {
		expectException(NullPointerException.class, "model may not be null");

		@SuppressWarnings("unused")
		Object unused = new ConfigurationBeanLoader((Model) null);
	}

	// ----------------------------------------------------------------------
	// loadInstance() failures
	// ----------------------------------------------------------------------

	@Test
	public void loadInstance_uriIsNull_throwsException()
			throws ConfigurationBeanLoaderException {
		expectException(NullPointerException.class, "uri may not be null");

		@SuppressWarnings("unused")
		Object unused = loader.loadInstance(null, SimpleSuccess.class);
	}

	@Test
	public void load_instance_resultClassIsNull_throwsException()
			throws ConfigurationBeanLoaderException {
		expectException(NullPointerException.class,
				"resultClass may not be null");

		@SuppressWarnings("unused")
		Object unused = loader.loadInstance(GENERIC_INSTANCE_URI, null);
	}

	@Test
	public void noStatementsAboutUri_throwsException()
			throws ConfigurationBeanLoaderException {
		expectSimpleFailure(
				SimpleSuccess.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"The model contains no statements about"));
	}

	@Test
	public void uriDoesNotDeclareResultClassAsType_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(dataProperty(GENERIC_INSTANCE_URI,
				"http://some.simple/property", "a value"));

		expectSimpleFailure(
				SimpleSuccess.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"A type statement is required"));
	}

	// --------------------------------------------

	@Test
	public void uriHasNoConcreteType_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SimpleInterfaceFailure.class)));

		expectSimpleFailure(
				SimpleInterfaceFailure.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"No concrete class is declared"));
	}

	public static interface SimpleInterfaceFailure {
		// This is not concrete, and there is no concrete implementation.
	}

	// --------------------------------------------

	@Test
	public void uriHasMultipleConcreteTypes_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SecondConcreteClass.class)));

		expectSimpleFailure(
				SimpleSuccess.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"more than one concrete class"));
	}

	public static class SecondConcreteClass extends SimpleSuccess {
		// Since this and SimpleSuccessClass are both concrete, they may not be
		// used together.
	}

	// --------------------------------------------

	@Test
	public void cantLoadConcreteType_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(UnloadableClass.class)));

		expectSimpleFailure(
				UnloadableClass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"Can't load this type"));
	}

	public static class UnloadableClass {
		static {
			if (true) {
				throw new IllegalStateException("This class cannot be loaded.");
			}
		}
	}

	// --------------------------------------------

	@Test
	public void concreteTypeNotAssignableToResultClass_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));
		model.add(typeStatement(GENERIC_INSTANCE_URI, toJavaUri(String.class)));

		expectSimpleFailure(
				SimpleSuccess.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"cannot be assigned to class"));
	}

	// --------------------------------------------

	@Test
	public void noNiladicConstructor_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NoNiladicConstructor.class)));

		expectSimpleFailure(
				NoNiladicConstructor.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"no zero-argument constructor."));
	}

	public static class NoNiladicConstructor {
		@SuppressWarnings("unused")
		public NoNiladicConstructor(String s) {
			// Not suitable as a bean
		}
	}

	// --------------------------------------------

	@Test
	public void niladicConstructorNotAccessible_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PrivateConstructor.class)));

		expectSimpleFailure(
				PrivateConstructor.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InvalidConfigurationRdfException.class,
						"no zero-argument constructor."));
	}

	public static class PrivateConstructor {
		private PrivateConstructor() {
			// Can't access the constructor.
		}
	}

	// --------------------------------------------

	@Test
	public void constructorThrowsException_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ConstructorFails.class)));

		expectSimpleFailure(
				ConstructorFails.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"Failed to create an instance."));
	}

	public static class ConstructorFails {
		public ConstructorFails() {
			if (true) {
				throw new IllegalStateException(
						"The constructor throws an exception.");
			}
		}
	}

	// --------------------------------------------

	@Test
	public void loaderCantSatisfyContextModelsUser_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NeedsContextModels.class)));

		loader = noContextLoader;

		expectSimpleFailure(
				NeedsContextModels.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ResourceUnavailableException.class,
						"Cannot satisfy ContextModelsUser"));
	}

	public static class NeedsContextModels implements ContextModelsUser {
		@Override
		public void setContextModels(ContextModelAccess models) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void loaderCantSatisfyRequestModelsUser_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NeedsRequestModels.class)));

		loader = noRequestLoader;

		expectSimpleFailure(
				NeedsRequestModels.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ResourceUnavailableException.class,
						"Cannot satisfy RequestModelsUser"));
	}

	public static class NeedsRequestModels implements RequestModelsUser {
		@Override
		public void setRequestModels(RequestModelAccess models) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void tripleHasUnrecognizedProperty_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));
		model.add(dataProperty(GENERIC_INSTANCE_URI,
				"http://bogus.property/name", "No place to put it."));

		expectSimpleFailure(
				SimpleSuccess.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(NoSuchPropertyMethodException.class,
						"No property method"));
	}

	// --------------------------------------------

	@Test
	public void valueTypeDoesNotMatchArgumentOfPropertyMethod_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingAString.class)));
		model.add(objectProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"http://some.other/uri"));

		expectSimpleFailure(
				ExpectingAString.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(PropertyTypeException.class,
						"type RESOURCE to a method of type STRING"));
	}

	public static class ExpectingAString {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void setString(String s) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void subordinateObjectCantBeLoaded_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NoSuchSubordinateInstance.class)));
		model.add(objectProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"http://some.other/uri"));

		expectSimpleFailure(
				NoSuchSubordinateInstance.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"));
	}

	public static class NoSuchSubordinateInstance {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void setHelper(SimpleDateFormat sdf) {
			// Nothing to do
		}
	}

	// ----------------------------------------------------------------------
	// loadInstance() successes
	// ----------------------------------------------------------------------

	/**
	 * Has a concrete result class.
	 */
	@Test
	public void simpleSuccess() throws ConfigurationBeanLoaderException {
		model.add(typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));

		SimpleSuccess instance = loader.loadInstance(
				SIMPLE_SUCCESS_INSTANCE_URI, SimpleSuccess.class);

		assertNotNull(instance);
	}

	public static class SimpleSuccess {
		// Nothing of interest.
	}

	// --------------------------------------------

	/**
	 * Exercise the full repertoire: properties of all types, validation
	 * methods. Result class is an interface; result class of helper is
	 * abstract.
	 */
	@Test
	public void fullSuccess() throws ConfigurationBeanLoaderException {
		model.add(FULL_SUCCESS_STATEMENTS);

		FullSuccessResultClass instance = loader.loadInstance(
				FULL_SUCCESS_INSTANCE_URI, FullSuccessResultClass.class);

		assertNotNull(instance);

		HashSet<String> expectedTextValues = new HashSet<>(
				Arrays.asList(new String[] { "Huey", "Dewey", "Louis" }));
		assertEquals(expectedTextValues, instance.getTextValues());

		HashSet<Float> expectedBoostValues = new HashSet<>(
				Arrays.asList(new Float[] { 1.5F, -99F }));
		assertEquals(expectedBoostValues, instance.getBoostValues());

		assertEquals(1, instance.getHelpers().size());
		assertTrue(instance.isValidated());
	}

	public static interface FullSuccessResultClass {
		Set<String> getTextValues();

		Set<Float> getBoostValues();

		Set<FullSuccessHelperResultClass> getHelpers();

		boolean isValidated();
	}

	public static class FullSuccessConcreteClass implements
			FullSuccessResultClass {
		private Set<String> textValues = new HashSet<>();
		private Set<Float> boostValues = new HashSet<>();
		private Set<FullSuccessHelperResultClass> helpers = new HashSet<>();

		private boolean validatorOneHasRun;
		private boolean validatorTwoHasRun;

		@Property(uri = FULL_SUCCESS_TEXT_PROPERTY)
		public void addText(String text) {
			textValues.add(text);
		}

		@Property(uri = FULL_SUCCESS_BOOST_PROPERTY)
		public void addBoost(float boost) {
			boostValues.add(boost);
		}

		@Property(uri = FULL_SUCCESS_HELPER_PROPERTY)
		public void addHelper(FullSuccessHelperResultClass helper) {
			helpers.add(helper);
		}

		@Validation
		public void validatorOne() {
			if (validatorOneHasRun) {
				throw new RuntimeException("validatorOne has already run.");
			}
			validatorOneHasRun = true;
		}

		@Validation
		public void validatorTwo() {
			if (validatorTwoHasRun) {
				throw new RuntimeException("validatorTwo has already run.");
			}
			validatorTwoHasRun = true;
		}

		@Override
		public Set<String> getTextValues() {
			return textValues;
		}

		@Override
		public Set<Float> getBoostValues() {
			return boostValues;
		}

		@Override
		public Set<FullSuccessHelperResultClass> getHelpers() {
			return helpers;
		}

		@Override
		public boolean isValidated() {
			return validatorOneHasRun && validatorTwoHasRun;
		}

	}

	public static abstract class FullSuccessHelperResultClass {
		// Abstract class, with concrete subclass.
	}

	public static class FullSuccessHelperConcreteClass extends
			FullSuccessHelperResultClass {
		// No properties
	}

	private static final Statement[] FULL_SUCCESS_STATEMENTS = new Statement[] {
			// Create the instance itself.
			typeStatement(FULL_SUCCESS_INSTANCE_URI,
					toJavaUri(FullSuccessResultClass.class)),
			typeStatement(FULL_SUCCESS_INSTANCE_URI,
					toJavaUri(FullSuccessConcreteClass.class)),

			// Add some boost values.
			dataProperty(FULL_SUCCESS_INSTANCE_URI,
					FULL_SUCCESS_BOOST_PROPERTY, 1.5F, XSDfloat),
			dataProperty(FULL_SUCCESS_INSTANCE_URI,
					FULL_SUCCESS_BOOST_PROPERTY, -99F, XSDfloat),

			// Add some text values: plain, typed, language
			dataProperty(FULL_SUCCESS_INSTANCE_URI, FULL_SUCCESS_TEXT_PROPERTY,
					"Huey", XSDstring),
			dataProperty(FULL_SUCCESS_INSTANCE_URI, FULL_SUCCESS_TEXT_PROPERTY,
					"Dewey", "en-US"),
			dataProperty(FULL_SUCCESS_INSTANCE_URI, FULL_SUCCESS_TEXT_PROPERTY,
					"Louis"),

			// Add a subordinate object.
			objectProperty(FULL_SUCCESS_INSTANCE_URI,
					FULL_SUCCESS_HELPER_PROPERTY,
					FULL_SUCCESS_HELPER_INSTANCE_URI),
			typeStatement(FULL_SUCCESS_HELPER_INSTANCE_URI,
					toJavaUri(FullSuccessHelperResultClass.class)),
			typeStatement(FULL_SUCCESS_HELPER_INSTANCE_URI,
					toJavaUri(FullSuccessHelperConcreteClass.class)) };

	// --------------------------------------------

	@Test
	public void irrelevantNonConcreteTypesAreIgnored()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
						toJavaUri(SimpleSuccess.class)),
				typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
						toJavaUri(IrrelevantInterface.class)),
				typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
						toJavaUri(IrrelevantAbstractClass.class)),
				typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
						"http://irrelevant.nonJava/class") });

		SimpleSuccess instance = loader.loadInstance(
				SIMPLE_SUCCESS_INSTANCE_URI, SimpleSuccess.class);

		assertNotNull(instance);
	}

	public interface IrrelevantInterface {
		// Nothing of interest.
	}

	public abstract class IrrelevantAbstractClass {
		// Nothing of interest.
	}

	// --------------------------------------------

	@Test
	public void loaderHasNoRequestButClassDoesntRequireIt_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));

		loader = noRequestLoader;

		SimpleSuccess instance = loader.loadInstance(
				SIMPLE_SUCCESS_INSTANCE_URI, SimpleSuccess.class);

		assertNotNull(instance);
	}

	// --------------------------------------------

	@Test
	public void loaderHasNoContextButClassDoesntRequireIt_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(SIMPLE_SUCCESS_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));

		loader = noContextLoader;

		SimpleSuccess instance = loader.loadInstance(
				SIMPLE_SUCCESS_INSTANCE_URI, SimpleSuccess.class);

		assertNotNull(instance);
	}

	// --------------------------------------------

	/**
	 * FullSuccess already tests for multiple validation methods.
	 * 
	 * SimpleSuccess already test for no validation methods, and for no property
	 * methods.
	 */
	@Test
	public void noValuesForProperty_success()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement(FULL_SUCCESS_INSTANCE_URI,
						toJavaUri(FullSuccessConcreteClass.class)),
				typeStatement(FULL_SUCCESS_INSTANCE_URI,
						toJavaUri(FullSuccessResultClass.class)) });

		FullSuccessResultClass instance = loader.loadInstance(
				FULL_SUCCESS_INSTANCE_URI, FullSuccessResultClass.class);

		assertNotNull(instance);
		assertEquals(Collections.emptySet(), instance.getTextValues());
		assertEquals(Collections.emptySet(), instance.getBoostValues());
		assertEquals(Collections.emptySet(), instance.getHelpers());
		assertTrue(instance.isValidated());
	}

	// ----------------------------------------------------------------------
	// loadAll() failures
	// ----------------------------------------------------------------------

	@Test
	public void loadAll_oneObjectCantBeLoaded_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement("http://apple/good",
						toJavaUri(OneBadAppleSpoilsTheBunch.class)),
				typeStatement("http://apple/good", toJavaUri(AGoodApple.class)),
				typeStatement("http://apple/bad",
						toJavaUri(OneBadAppleSpoilsTheBunch.class)) });

		expectException(ConfigurationBeanLoaderException.class,
				"Failed to load", InvalidConfigurationRdfException.class,
				"No concrete class is declared");

		loader.loadAll(OneBadAppleSpoilsTheBunch.class);
	}

	public interface OneBadAppleSpoilsTheBunch {
		// Nothing.
	}

	public static class AGoodApple implements OneBadAppleSpoilsTheBunch {
		// Nothing
	}

	// ----------------------------------------------------------------------
	// loadAll() successes
	// ----------------------------------------------------------------------

	@Test
	public void loadAll_noResults_success()
			throws ConfigurationBeanLoaderException {
		Set<SimpleSuccess> instances = loader.loadAll(SimpleSuccess.class);
		assertTrue(instances.isEmpty());
	}

	// --------------------------------------------

	@Test
	public void loadAll_oneResult_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(SimpleSuccess.class)));

		Set<SimpleSuccess> instances = loader.loadAll(SimpleSuccess.class);
		assertEquals(1, instances.size());
	}

	// --------------------------------------------

	@Test
	public void loadAll_multipleResults_success()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement("http://simple.instance/one",
						toJavaUri(InstanceWithProperty.class)),
				dataProperty("http://simple.instance/one",
						"http://simple.text/property", "FIRST"),
				typeStatement("http://simple.instance/two",
						toJavaUri(InstanceWithProperty.class)),
				dataProperty("http://simple.instance/two",
						"http://simple.text/property", "SECOND") });

		Set<InstanceWithProperty> instances = loader
				.loadAll(InstanceWithProperty.class);
		assertEquals(2, instances.size());

		Set<String> textValues = new HashSet<>();
		for (InstanceWithProperty instance : instances) {
			textValues.add(instance.getText());
		}
		assertEquals(new HashSet<>(Arrays.asList("FIRST", "SECOND")),
				textValues);
	}

	public static class InstanceWithProperty {
		private String text;

		@Property(uri = "http://simple.text/property")
		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	// ----------------------------------------------------------------------
	// Additional tests
	// ----------------------------------------------------------------------

	@SuppressWarnings("unused")
	@Test
	@Ignore
	// TODO
	public void circularReferencesAreNotFatal()
			throws ConfigurationBeanLoaderException {
		fail("circularReferencesAreNotFatal not implemented");
	}

	@SuppressWarnings("unused")
	@Test
	@Ignore
	// TODO deals with circularity.
	public void subordinateObjectCantBeLoaded_leavesNoAccessibleInstanceOfParent()
			throws ConfigurationBeanLoaderException {
		fail("subordinateObjectCantBeLoaded_leavesNoAccessibleInstanceOfParent not implemented");
	}

	@SuppressWarnings("unused")
	@Test
	@Ignore
	// TODO deals with circularity.
	public void parentObjectCantBeLoaded_leavesNoAccessibleInstanceOfSubordinate()
			throws ConfigurationBeanLoaderException {
		fail("parentObjectCantBeLoaded_leavesNoAccessibleInstanceOfSubordinate not implemented");
	}

}
