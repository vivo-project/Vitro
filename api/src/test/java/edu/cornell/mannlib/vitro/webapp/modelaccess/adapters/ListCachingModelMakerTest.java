/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import stubs.org.apache.jena.rdf.model.ModelMaker.ModelMakerStub;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.CannotCreateException;
import org.apache.jena.shared.DoesNotExistException;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Test the functions of a ListCachingModelMaker. Does it properly register the
 * presence of a model with no triples?
 */
public class ListCachingModelMakerTest extends AbstractTestClass {
	private static final String URI_ONE = "http://model.one";
	private static final String URI_TWO = "http://model.two";
	private static final String URI_NONE = "http://model.does.not.exist";

	private static final Model MODEL_ONE = createModel();
	private static final Model MODEL_TWO = createModel();
	private static final Model MODEL_DEFAULT = createModel();
	private static final Model MODEL_FRESH = createModel();

	private ModelMaker rigorous;
	private ModelMaker relaxed;
	private ModelMaker mm;
	private ModelReader modelReader;

	private static Model createModel() {
		return ModelFactory.createDefaultModel();
	}

	@Before
	public void setup() {
		rigorous = ModelMakerStub.rigorous(MODEL_DEFAULT, MODEL_FRESH)
				.put(URI_ONE, MODEL_ONE).put(URI_TWO, MODEL_TWO);
		relaxed = ModelMakerStub.relaxed(MODEL_DEFAULT, MODEL_FRESH)
				.put(URI_ONE, MODEL_ONE).put(URI_TWO, MODEL_TWO);
		relaxed(); // call rigorous() to override, if desired.
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@SuppressWarnings("unused")
	@Test(expected = NullPointerException.class)
	public void nullInnerModel() {
		new ListCachingModelMaker(null);
	}

	@Test
	public void listModels() {
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void hasModelExist() {
		assertTrue(mm.hasModel(URI_ONE));
	}

	@Test
	public void hasModelNonExist() {
		assertFalse(mm.hasModel(URI_NONE));
	}

	@Test
	public void createModelExist() {
		assertEquals(MODEL_ONE, mm.createModel(URI_ONE));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void createModelNonExist() {
		assertEquals(MODEL_FRESH, mm.createModel(URI_NONE));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	@Test(expected = AlreadyExistsException.class)
	public void createModelStrictExist() {
		mm.createModel(URI_ONE, true);
	}

	@Test
	public void createModelStrictNonExist() {
		assertEquals(MODEL_FRESH, mm.createModel(URI_NONE, true));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	@Test
	public void openModelExist() {
		assertEquals(MODEL_TWO, mm.openModel(URI_TWO));
		assertList(URI_ONE, URI_TWO);
	}

	@Test(expected = DoesNotExistException.class)
	public void openModelRigorousNonExist() {
		rigorous();
		mm.openModel(URI_NONE);
	}

	@Test
	public void openModelRelaxedNonExist() {
		assertEquals(MODEL_FRESH, mm.openModel(URI_NONE));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	@Test
	public void openModelIfPresentExist() {
		assertEquals(MODEL_TWO, mm.openModelIfPresent(URI_TWO));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void openModelIfPresentNonExist() {
		assertNull(mm.openModelIfPresent(URI_NONE));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void openModelStrictExist() {
		assertEquals(MODEL_ONE, mm.openModel(URI_ONE, true));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void openModelNonStrictExist() {
		assertEquals(MODEL_ONE, mm.openModel(URI_ONE, false));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void openModelNonStrictNonExist() {
		assertEquals(MODEL_FRESH, mm.openModel(URI_NONE, false));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	@Test
	public void removeModelExist() {
		mm.removeModel(URI_ONE);
		assertList(URI_TWO);
	}

	@Test(expected = DoesNotExistException.class)
	public void removeModelNonExist() {
		mm.removeModel(URI_NONE);
	}

	@Test
	public void getModelExist() {
		assertEquals(MODEL_TWO, mm.getModel(URI_TWO));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void getModelRigorousNonExist() {
		rigorous();
		assertNull(mm.getModel(URI_NONE));
		assertList(URI_ONE, URI_TWO);
	}

	@Test
	public void getModelRelaxedNonExist() {
		assertEquals(MODEL_FRESH, mm.getModel(URI_NONE));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	@Test
	public void getModelLoadIfAbsentExist() {
		assertEquals(MODEL_TWO, mm.getModel(URI_TWO, modelReader));
		assertList(URI_ONE, URI_TWO);
	}

	@Test(expected = CannotCreateException.class)
	public void getModelLoadIfAbsentRigorousNonExist() {
		rigorous();
		mm.getModel(URI_NONE, modelReader);
	}

	@Test
	public void getModelLoadIfAbsentRelaxedNonExist() {
		assertEquals(MODEL_FRESH, mm.getModel(URI_NONE, modelReader));
		assertList(URI_ONE, URI_TWO, URI_NONE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void relaxed() {
		mm = new ListCachingModelMaker(relaxed);
	}

	private void rigorous() {
		mm = new ListCachingModelMaker(rigorous);
	}

	private void assertList(String... expectedArray) {
		Set<String> expected = new HashSet<>(Arrays.asList(expectedArray));
		Set<String> actual = mm.listModels().toSet();
		assertEquals(expected, actual);
	}

}
