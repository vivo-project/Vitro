/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper.InstanceWrapperException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance.CardinalityException;

/**
 * Tests for minOccurs, maxOccurs on the @Property annotation.
 */
public class ConfigurationBeanLoader_Cardinality_Test extends
		ConfigurationBeanLoaderTestBase {

	private static final Statement[] FOUND_NONE = new Statement[0];

	private static final Statement[] FOUND_ONE = new Statement[] { dataProperty(
			GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI, "value One") };

	private static final Statement[] FOUND_TWO = new Statement[] {
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value One"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Two") };

	private static final Statement[] FOUND_THREE = new Statement[] {
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value One"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Two"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Three") };

	private static final Statement[] FOUND_FOUR = new Statement[] {
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value One"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Two"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Three"),
			dataProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
					"value Four") };

	// --------------------------------------------

	@Test
	public void minOccursIsNegative_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(NegativeMinOccurs.class)));
		model.add(objectProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"http://some.other/uri"));

		expectSimpleFailure(
				NegativeMinOccurs.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"must not be negative"));
	}

	public static class NegativeMinOccurs {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI, minOccurs = -1)
		public void setValue(String value) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void maxOccursLessThanMinOccurs_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(MaxOccursLessThanMinOccurs.class)));
		model.add(objectProperty(GENERIC_INSTANCE_URI, GENERIC_PROPERTY_URI,
				"http://some.other/uri"));

		expectSimpleFailure(
				MaxOccursLessThanMinOccurs.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"not be less than minOccurs"));
	}

	public static class MaxOccursLessThanMinOccurs {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI, minOccurs = 2, maxOccurs = 1)
		public void setValue(String value) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void expectingSomeFoundNone_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingSome.class)));
		model.add(FOUND_NONE);

		expectSimpleFailure(
				ExpectingSome.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(CardinalityException.class, "Expecting at least 2"));
	}

	@Test
	public void expectingSomeFoundFewer_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingSome.class)));
		model.add(FOUND_ONE);

		expectSimpleFailure(
				ExpectingSome.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(CardinalityException.class, "Expecting at least 2"));
	}

	@Test
	public void expectingSomeFoundMore_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingSome.class)));
		model.add(FOUND_FOUR);

		expectSimpleFailure(
				ExpectingSome.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(CardinalityException.class,
						"Expecting no more than 3"));
	}

	@Test
	public void expectingSomeFoundSome_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingSome.class)));
		model.add(FOUND_THREE);
		Set<ExpectingSome> instances = loader.loadAll(ExpectingSome.class);
		assertEquals(1, instances.size());
		model.removeAll();

		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectingSome.class)));
		model.add(FOUND_TWO);
		instances = loader.loadAll(ExpectingSome.class);
		assertEquals(1, instances.size());
	}

	public static class ExpectingSome {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI, minOccurs = 2, maxOccurs = 3)
		public void setValue(String value) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void notSpecifiedFoundNone_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(CardinalityNotSpecified.class)));
		model.add(FOUND_NONE);

		Set<CardinalityNotSpecified> instances = loader
				.loadAll(CardinalityNotSpecified.class);
		assertEquals(1, instances.size());
	}

	@Test
	public void notSpecifiedFoundSome_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(CardinalityNotSpecified.class)));
		model.add(FOUND_FOUR);

		Set<CardinalityNotSpecified> instances = loader
				.loadAll(CardinalityNotSpecified.class);
		assertEquals(1, instances.size());
	}

	public static class CardinalityNotSpecified {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI)
		public void setValue(String value) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void expectNoneFoundNone_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectNone.class)));
		model.add(FOUND_NONE);

		Set<ExpectNone> instances = loader.loadAll(ExpectNone.class);
		assertEquals(1, instances.size());
	}

	public static class ExpectNone {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI, maxOccurs = 0)
		public void setValue(String value) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void expectExactlyFoundExactly_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ExpectTwo.class)));
		model.add(FOUND_TWO);

		Set<ExpectTwo> instances = loader.loadAll(ExpectTwo.class);
		assertEquals(1, instances.size());
	}

	public static class ExpectTwo {
		@SuppressWarnings("unused")
		@Property(uri = GENERIC_PROPERTY_URI, minOccurs = 2, maxOccurs = 2)
		public void setValue(String value) {
			// Nothing to do
		}
	}
}
