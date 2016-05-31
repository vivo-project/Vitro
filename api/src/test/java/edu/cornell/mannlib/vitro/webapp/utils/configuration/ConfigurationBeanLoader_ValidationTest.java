/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper.InstanceWrapperException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance.ValidationFailedException;

/**
 * Test the @Validation annotation.
 */
public class ConfigurationBeanLoader_ValidationTest extends
		ConfigurationBeanLoaderTestBase {
	// --------------------------------------------

	@Test
	public void validationMethodHasParameters_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodWithParameter.class)));

		expectSimpleFailure(
				ValidationMethodWithParameter.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"should not have parameters"));
	}

	public static class ValidationMethodWithParameter {
		@SuppressWarnings("unused")
		@Validation
		public void validateWithParameter(String s) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodDoesNotReturnVoid_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodShouldReturnVoid.class)));

		expectSimpleFailure(
				ValidationMethodShouldReturnVoid.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class, "should return void"));
	}

	public static class ValidationMethodShouldReturnVoid {
		@Validation
		public String validateWithReturnType() {
			return "Hi there!";
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodNotAccessible_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodIsPrivate.class)));

		expectSimpleFailure(
				ValidationMethodIsPrivate.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ValidationFailedException.class,
						"Error executing validation method"));
	}

	public static class ValidationMethodIsPrivate {
		@Validation
		private void validateIsPrivate() {
			// private method
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodThrowsException_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationThrowsException.class)));

		expectSimpleFailure(
				ValidationThrowsException.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ValidationFailedException.class,
						"Error executing validation method"));
	}

	public static class ValidationThrowsException {
		@Validation
		public void validateFails() {
			throw new RuntimeException("from validation method");
		}
	}

	// --------------------------------------------

	@Test
	public void superclassContainsValidationMethod_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(EmptyValidationSubclass.class)));

		EmptyValidationSubclass instance = loader.loadInstance(
				GENERIC_INSTANCE_URI, EmptyValidationSubclass.class);

		assertNotNull(instance);
		assertTrue(instance.validatorSuperHasRun);
	}

	@Test
	public void superclassAndSubclassContainValidationMethods_success()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(AdditionalValidationSubclass.class)));

		AdditionalValidationSubclass instance = loader.loadInstance(
				GENERIC_INSTANCE_URI, AdditionalValidationSubclass.class);

		assertNotNull(instance);
		assertTrue(instance.validatorSuperHasRun);
		assertTrue(instance.validatorSubHasRun);
	}

	@Test
	public void validationMethodOverridesValidationMethod_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationOverValidationSubclass.class)));

		expectSimpleFailure(
				ValidationOverValidationSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"overrides a validation method"));
	}

	@Test
	public void plainMethodOverridesValidationMethod_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(PlainOverValidationSubclass.class)));

		expectSimpleFailure(
				PlainOverValidationSubclass.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"overrides a validation method"));
	}

	public static class ValidationSuperclass {
		public boolean validatorSuperHasRun = false;

		@Validation
		public void validatorSuper() {
			if (validatorSuperHasRun) {
				throw new RuntimeException("validatorSuper has already run.");
			}
			validatorSuperHasRun = true;
		}
	}

	public static class EmptyValidationSubclass extends ValidationSuperclass {
		// Just want to see that the superclass validation is run.
	}

	public static class AdditionalValidationSubclass extends
			ValidationSuperclass {
		public boolean validatorSubHasRun = false;

		@Validation
		public void validatorSub() {
			if (validatorSubHasRun) {
				throw new RuntimeException("validatorSub has already run.");
			}
			validatorSubHasRun = true;
		}
	}

	public static class ValidationOverValidationSubclass extends
			EmptyValidationSubclass {
		@Override
		@Validation
		public void validatorSuper() {
			// Should fail (two levels down)
		}
	}

	public static class PlainOverValidationSubclass extends
			ValidationSuperclass {
		@Override
		public void validatorSuper() {
			// Should fail
		}
	}

}
