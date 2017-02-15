/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper.InstanceWrapperException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;

/**
 * Tests of the @Property annotation.
 */
public class ConfigurationBeanLoader_PropertyTest extends
		ConfigurationBeanLoaderTestBase {
	protected static final String OTHER_PROPERTY_URI = "http://mytest.edu/different_property";

	// --------------------------------------------

	@Test
	public void propertyMethodHasNoParameter_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NoParameterOnPropertyMethod.class)));

		expectSimpleFailure(
				NoParameterOnPropertyMethod.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"must accept exactly one parameter"));
	}

	public static class NoParameterOnPropertyMethod {
		@Property(uri = GENERIC_PROPERTY_URI)
		public void methodTakesNoParameters() {
			// Not suitable as a property method.
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodHasMultipleParameters_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(MultipleParametersOnPropertyMethod.class)));

		expectSimpleFailure(
				MultipleParametersOnPropertyMethod.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"must accept exactly one parameter"));
	}

	public static class MultipleParametersOnPropertyMethod {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void methodTakesMultipleParameters(String s, Float f) {
			// Not suitable as a property method.
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodHasInvalidParameter_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(InvalidParameterOnPropertyMethod.class)));

		expectSimpleFailure(
				InvalidParameterOnPropertyMethod.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"Failed to create the PropertyMethod"));
	}

	public static class InvalidParameterOnPropertyMethod {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void methodTakesInvalidParameters(byte b) {
			// Not suitable as a property method.
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodDoesNotReturnVoid_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PropertyMethodMustReturnVoid.class)));

		expectSimpleFailure(
				PropertyMethodMustReturnVoid.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class, "should return void"));
	}

	public static class PropertyMethodMustReturnVoid {
		@Property(uri = GENERIC_PROPERTY_URI)
		public String methodReturnIsNotVoid(String s) {
			// Not suitable as a property method.
			return s;
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodNotAccessible_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PropertyMethodIsPrivate.class)));
		model.add(dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"can't store in a private method."));

		expectSimpleFailure(
				PropertyMethodIsPrivate.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(PropertyTypeException.class,
						"Property method failed."));
	}

	public static class PropertyMethodIsPrivate {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		private void methodReturnIsNotVoid(String s) {
			// Not suitable as a property method.
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodThrowsException_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PropertyMethodFails.class)));
		model.add(dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"exception while loading."));

		expectSimpleFailure(
				PropertyMethodFails.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(PropertyTypeException.class,
						"Property method failed."));
	}

	public static class PropertyMethodFails {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void methodThrowsException(String s) {
			if (true) {
				throw new RuntimeException("property method fails.");
			}
		}
	}

	// --------------------------------------------

	@Test
	public void propertyMethodDuplicateUri_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(TwoMethodsWithSameUri.class)));

		expectSimpleFailure(
				TwoMethodsWithSameUri.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"methods have the same URI"));
	}

	public static class TwoMethodsWithSameUri {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void firstProperty(String s) {
			// Nothing to do
		}

		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void secondProperty(String s) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void superclassContainsPropertyAnnotation_success()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement(GENERIC_INSTANCE_URI,
						toJavaUri(EmptyPropertyMethodSubclass.class)),
				dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
						"Value") });

		EmptyPropertyMethodSubclass instance = loader.loadInstance(
				GENERIC_INSTANCE_URI, EmptyPropertyMethodSubclass.class);

		assertNotNull(instance);
		assertEquals("Value", instance.value);
	}

	@Test
	public void propertyMethodOverridesPropertyMethod_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PropertyMethodOverPropertyMethodSubclass.class)));

		expectSimpleFailure(
				PropertyMethodOverPropertyMethodSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"conflicts with a property method"));
	}

	@Test
	public void plainMethodOverridesPropertyMethod_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PlainOverPropertyMethodSubclass.class)));

		expectSimpleFailure(
				PlainOverPropertyMethodSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"conflicts with a property method"));
	}

	@Test
	public void uriConflictsBetweenSubclassAndSuperclassPropertyMethods_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ConflictingUriPropertyMethodSubclass.class)));

		expectSimpleFailure(
				ConflictingUriPropertyMethodSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"Two property methods have the same URI"));
	}

	@Test
	public void propertyMethodSameNameButDoesNotOverride_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(new Statement[] {
				typeStatement(GENERIC_INSTANCE_URI,
						toJavaUri(DistinctPropertyMethodSubclass.class)),
				dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
						"Value"),
				dataProperty(GENERIC_INSTANCE_URI, OTHER_PROPERTY_URI,
						100.0F) });
		
		expectSimpleFailure(
				DistinctPropertyMethodSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"conflicts with a property method"));
	}

	public static class PropertyMethodSuperclass {
		public String value = null;

		@Property(uri = GENERIC_PROPERTY_URI)
		public void propertySuper(String v) {
			if (value != null) {
				throw new RuntimeException("propertySuper has already run.");
			}
			value = v;
		}
	}

	public static class EmptyPropertyMethodSubclass extends
			PropertyMethodSuperclass {
		// Just want to see that the superclass method is run.
	}

	public static class DistinctPropertyMethodSubclass extends
			PropertyMethodSuperclass {
		public float fvalue;

		@Property(uri = OTHER_PROPERTY_URI)
		public void propertySuper(Float f) {
			if (fvalue != 0.0) {
				throw new RuntimeException("propertySub has already run.");
			}
			fvalue = f;
		}
	}

	public static class ConflictingUriPropertyMethodSubclass extends
			PropertyMethodSuperclass {

		@Property(uri = GENERIC_PROPERTY_URI)
		public void propertyConflict(String v) {
			// nothing to do.
		}
	}

	public static class PropertyMethodOverPropertyMethodSubclass extends
			EmptyPropertyMethodSubclass {
		@Override
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void propertySuper(String v) {
			// Should fail (two levels down)
		}
	}

	public static class PlainOverPropertyMethodSubclass extends
			PropertyMethodSuperclass {
		@SuppressWarnings("unused")
		public void propertySuper(Float f) {
			// nothing to do
		}
	}

}
