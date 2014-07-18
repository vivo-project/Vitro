/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import stubs.com.hp.hpl.jena.rdf.model.ModelMaker.ModelMakerStub;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.CannotCreateException;
import com.hp.hpl.jena.shared.DoesNotExistException;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.UnionModelsModelMaker.UnionSpec;

/**
 * Test the functionality of the UnionModelsModelMaker.
 */
public class UnionModelsModelMakerTest extends AbstractTestClass {
	private static final String URI_ONE = "http://model.one";
	private static final String URI_TWO = "http://model.two";
	private static final String URI_THREE = "http://model.three";
	private static final String URI_UNION = "http://model.union";
	private static final String URI_NONE = "http://model.does.not.exist";

	private static final Model MODEL_ONE = createModel();
	private static final Model MODEL_TWO = createModel();
	private static final Model MODEL_THREE = createModel();
	private static final Model MODEL_DEFAULT = createModel();
	private static final Model MODEL_FRESH = createModel();

	private static Model createModel() {
		return ModelFactory.createDefaultModel();
	}

	private ModelMaker inner;
	private ModelMaker mm;

	@Before
	public void setup() {
		/*
		 * Use a rigorous inner model maker, but it doesn't make much difference.
		 */
		inner = ModelMakerStub.rigorous(MODEL_DEFAULT, MODEL_FRESH)
				.put(URI_ONE, MODEL_ONE).put(URI_TWO, MODEL_TWO)
				.put(URI_THREE, MODEL_THREE);

		mm = new UnionModelsModelMaker(inner, UnionSpec.base(URI_ONE)
				.plus(URI_TWO).yields(URI_UNION));
	}

	@SuppressWarnings("unused")
	@Test(expected = NullPointerException.class)
	public void nullInnerModel() {
		new UnionModelsModelMaker(null, UnionSpec.base(URI_ONE).plus(URI_TWO)
				.yields(URI_UNION));
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void duplicateUnionUri() {
		new UnionModelsModelMaker(inner, UnionSpec.base(URI_ONE).plus(URI_TWO)
				.yields(URI_UNION), UnionSpec.base(URI_ONE).plus(URI_THREE)
				.yields(URI_UNION));
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void nestedUnions() {
		new UnionModelsModelMaker(inner, UnionSpec.base(URI_ONE).plus(URI_TWO)
				.yields(URI_UNION), UnionSpec.base(URI_UNION).plus(URI_THREE)
				.yields("http://nestedUnion"));
	}

	@Test
	public void hasModelActual() {
		assertTrue(mm.hasModel(URI_ONE));
	}

	@Test
	public void hasModelNone() {
		assertFalse(mm.hasModel(URI_NONE));
	}

	@Test
	public void hasModelUnion() {
		assertTrue(mm.hasModel(URI_UNION));
	}

	@Test
	public void listModels() {
		assertExpectedModelsList(URI_ONE, URI_TWO, URI_THREE, URI_UNION);
	}

	@Test
	public void createModelActual() {
		assertEquals(MODEL_ONE, mm.createModel(URI_ONE));
	}

	@Test
	public void createModelNone() {
		assertEquals(MODEL_FRESH, mm.createModel(URI_NONE));
	}

	@Test
	public void createModelUnion() {
		assertTrue(isUnionModel(mm.createModel(URI_UNION)));
	}

	@Test(expected = AlreadyExistsException.class)
	public void createModelActualStrict() {
		mm.createModel(URI_ONE, true);
	}

	@Test
	public void createModelNoneStrict() {
		assertEquals(MODEL_FRESH, mm.createModel(URI_NONE, true));
	}

	@Test(expected = AlreadyExistsException.class)
	public void createModelUnionStrict() {
		mm.createModel(URI_UNION, true);
	}

	@Test
	public void openModelActual() {
		assertEquals(MODEL_ONE, mm.openModel(URI_ONE));
	}

	@Test(expected = DoesNotExistException.class)
	public void openModelNone() {
		mm.openModel(URI_NONE);
	}

	@Test
	public void openModelUnion() {
		assertTrue(isUnionModel(mm.openModel(URI_UNION)));
	}

	@Test
	public void openModelActualStrict() {
		assertEquals(MODEL_ONE, mm.openModel(URI_ONE, true));
	}

	@Test(expected = DoesNotExistException.class)
	public void openModelNoneStrict() {
		mm.openModel(URI_NONE, true);
	}

	@Test
	public void openModelUnionStrict() {
		assertTrue(isUnionModel(mm.openModel(URI_UNION, true)));
	}

	@Test
	public void openModelIfPresentActual() {
		assertEquals(MODEL_ONE, mm.openModelIfPresent(URI_ONE));
	}

	@Test
	public void openModelIfPresentNone() {
		assertNull(mm.openModelIfPresent(URI_NONE));
	}

	@Test
	public void openModelIfPresentUnion() {
		assertTrue(isUnionModel(mm.openModelIfPresent(URI_UNION)));
	}

	@Test
	public void removeModelActual() {
		mm.removeModel(URI_ONE);
		assertExpectedModelsList(URI_TWO, URI_THREE, URI_UNION);
	}

	@Test(expected = DoesNotExistException.class)
	public void removeModelNone() {
		mm.removeModel(URI_NONE);
	}

	@Test
	public void removeModelUnion() {
		mm.removeModel(URI_UNION);
		assertExpectedModelsList(URI_ONE, URI_TWO, URI_THREE);
	}

	@Test
	public void getModelActual() {
		assertEquals(MODEL_ONE, mm.getModel(URI_ONE));
	}

	@Test
	public void getModelNone() {
		assertEquals(null, mm.getModel(URI_NONE));
	}

	@Test
	public void getModelUnion() {
		assertTrue(isUnionModel(mm.getModel(URI_UNION)));
	}

	@Test
	public void getModelLoadIfAbsentActual() {
		assertEquals(MODEL_ONE, mm.getModel(URI_ONE, null));
	}

	@Test(expected = CannotCreateException.class)
	public void getModelLoadIfAbsentNone() {
		mm.getModel(URI_NONE, null);
	}

	@Test
	public void getModelLoadIfAbsentUnion() {
		assertTrue(isUnionModel(mm.getModel(URI_UNION, null)));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * No easy way to assert that this is actually the union model, but we can
	 * assert that it is not null, and not any model we know of.
	 */
	private boolean isUnionModel(Model m) {
		Model[] knownModels = { MODEL_ONE, MODEL_TWO, MODEL_THREE,
				MODEL_DEFAULT, MODEL_FRESH };
		if (m == null) {
			return false;
		}

		for (Model knownModel : knownModels) {
			if (m == knownModel) {
				return false;
			}
		}

		return true;
	}

	private void assertExpectedModelsList(String... uris) {
		Set<String> expected = new HashSet<>(Arrays.asList(uris));
		assertEquals(expected, mm.listModels().toSet());
	}

}