/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toPossibleJavaUris;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

/**
 * Assure that we can use "namespaces" for Java URIs. The namespace must end
 * with a '#'.
 */
public class ConfigurationBeanLoader_NamespacesTest
		extends ConfigurationBeanLoaderTestBase {

	// ----------------------------------------------------------------------
	// toPossibleJavaUris()
	// ----------------------------------------------------------------------

	@Test
	public void possibleForJavaLangString() {
		Set<String> expected = new HashSet<>();
		expected.add("java:java.lang.String");
		expected.add("java:java#lang.String");
		expected.add("java:java.lang#String");
		assertEquals(expected, toPossibleJavaUris(String.class));
	}

	// ----------------------------------------------------------------------
	// loadAll()
	// ----------------------------------------------------------------------

	@Test
	public void loadAllForJavaUtilRandom()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement("http://noPound", "java:java.util.Random"));
		model.add(typeStatement("http://firstPound", "java:java#util.Random"));
		model.add(typeStatement("http://secondPound", "java:java.util#Random"));
		model.add(typeStatement("http://notARandom", "java:java.util.Set"));
		Set<Random> instances = loader.loadAll(Random.class);
		assertEquals(3, instances.size());
	}

	@Test
	public void loadAlForCustomInnerClass()
			throws ConfigurationBeanLoaderException {
		Set<String> typeUris = toPossibleJavaUris(ExampleClassForLoadAll.class);
		for (String typeUri : typeUris) {
			model.add(typeStatement("http://testUri" + model.size(), typeUri));
		}
		Set<ExampleClassForLoadAll> instances = loader
				.loadAll(ExampleClassForLoadAll.class);
		assertEquals(typeUris.size(), instances.size());
	}

	public static class ExampleClassForLoadAll {
		// Nothing of interest
	}

	// ----------------------------------------------------------------------
	// loadInstance()
	// ----------------------------------------------------------------------

	@Test
	public void loadInstanceVariationsForJavaUtilRandom()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement("http://noPound", "java:java.util.Random"));
		model.add(typeStatement("http://firstPound", "java:java#util.Random"));
		model.add(typeStatement("http://secondPound", "java:java.util#Random"));
		model.add(typeStatement("http://notARandom", "java:java.util.Set"));

		assertNotNull(loader.loadInstance("http://noPound", Random.class));
		assertNotNull(loader.loadInstance("http://firstPound", Random.class));
		assertNotNull(loader.loadInstance("http://secondPound", Random.class));

		try {
			loader.loadInstance("http://notARandom", Random.class);
			fail("Should not be a Random");
		} catch (Exception e) {
			// Expected it
		}
	}

	@Test
	public void loadInstanceVariationsForCustomInnerClass()
			throws ConfigurationBeanLoaderException {
		Set<String> typeUris = toPossibleJavaUris(
				ExampleClassForLoadInstance.class);
		for (String typeUri : typeUris) {
			model.add(typeStatement("http://testUri" + model.size(), typeUri));
		}
		for (int i = 0; i < model.size(); i++) {
			String instanceUri = "http://testUri" + i;
			assertNotNull("No instance for " + instanceUri, loader.loadInstance(
					instanceUri, ExampleClassForLoadInstance.class));
		}
	}

	public static class ExampleClassForLoadInstance {
		// Nothing of interest
	}

}
