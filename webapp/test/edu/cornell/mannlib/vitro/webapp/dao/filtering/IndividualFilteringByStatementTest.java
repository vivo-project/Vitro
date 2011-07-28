/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jga.fn.UnaryFunctor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.beans.IndividualStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

/**
 * Test that the IndividualFiltering class filters by statements as much as
 * possible. That way the filter can consider the subject of the statement as
 * well as the predicate, when deciding whether to authorize the request.
 * 
 * <pre>
 * Start with six properties and a filter that recognizes them.
 *    DATA_HIDDEN -- never approved
 *    DATA_VISIBLE -- always approved
 *    DATA_MAYBE -- only approved in statements with subject of SPECIAL URI.
 *    OBJECT_HIDDEN -- never approved
 *    OBJECT_VISIBLE -- always approved
 *    OBJECT_MAYBE -- only approved in statements with subject or object of SPECIAL URI. 
 * 
 * Test all of the filtering methods on two filtered individuals. 
 *    One is SPECIAL_URI, and should see the MAYBE properties.
 *    One is ordinary, and should not see the MAYBE properties.
 * </pre>
 * 
 * This is a simplification of a "self-editing" filter, which shows some
 * properties, hides others, and allows some of the hidden ones in statements,
 * depending on the subject and/or object of the statement.
 */
public class IndividualFilteringByStatementTest extends AbstractTestClass {
	private static final String URI_INDIVIDUAL_SPECIAL = "specialUri";
	private static final String URI_SUBJECT = "subject";

	private static final String PROPERTY_DATA_HIDDEN = "hiddenDataProperty";
	private static final String PROPERTY_DATA_VISIBLE = "visibleDataProperty";
	private static final String PROPERTY_DATA_MAYBE = "maybeDataProperty";

	private static final String PROPERTY_OBJECT_HIDDEN = "hiddenObjectProperty";
	private static final String PROPERTY_OBJECT_VISIBLE = "visibleObjectProperty";
	private static final String PROPERTY_OBJECT_MAYBE = "maybeObjectProperty";

	private static final String VALUE_HIDDEN_DATA_SPECIAL = "hidden data on special";
	private static final String VALUE_VISIBLE_DATA_SPECIAL = "visible data on special";
	private static final String VALUE_MAYBE_DATA_SPECIAL = "maybe data on special";

	private static final String URI_HIDDEN_OBJECT_SPECIAL = "object://hidden_on_special";
	private static final String URI_VISIBLE_OBJECT_SPECIAL = "object://visible_on_special";
	private static final String URI_MAYBE_OBJECT_SPECIAL = "object://maybe_on_special";

	private static final String VALUE_HIDDEN_DATA_ORDINARY = "hidden data on ordinary";
	private static final String VALUE_VISIBLE_DATA_ORDINARY = "visible data on ordinary";
	private static final String VALUE_MAYBE_DATA_ORDINARY = "maybe data on ordinary";

	private static final String URI_HIDDEN_OBJECT_ORDINARY = "object://hidden_on_ordinary";
	private static final String URI_VISIBLE_OBJECT_ORDINARY = "object://visible_on_ordinary";
	private static final String URI_MAYBE_OBJECT_ORDINARY = "object://maybe_on_ordinary";

	private Individual filteredSpecial;
	private Individual filteredOrdinary;

	@Before
	public void createIndividuals() {
		IndividualStub indSpecial = new IndividualStub(URI_INDIVIDUAL_SPECIAL);

		indSpecial.addDataPropertyStatement(PROPERTY_DATA_HIDDEN,
				VALUE_HIDDEN_DATA_SPECIAL);
		indSpecial.addDataPropertyStatement(PROPERTY_DATA_VISIBLE,
				VALUE_VISIBLE_DATA_SPECIAL);
		indSpecial.addDataPropertyStatement(PROPERTY_DATA_MAYBE,
				VALUE_MAYBE_DATA_SPECIAL);

		indSpecial.addObjectPropertyStatement(PROPERTY_OBJECT_HIDDEN,
				URI_HIDDEN_OBJECT_SPECIAL);
		indSpecial.addObjectPropertyStatement(PROPERTY_OBJECT_VISIBLE,
				URI_VISIBLE_OBJECT_SPECIAL);
		indSpecial.addObjectPropertyStatement(PROPERTY_OBJECT_MAYBE,
				URI_MAYBE_OBJECT_SPECIAL);

		filteredSpecial = new IndividualFiltering(indSpecial,
				new IndividualBasedFilter());

		IndividualStub indOrdinary = new IndividualStub("someOtherUri");

		indOrdinary.addDataPropertyStatement(PROPERTY_DATA_HIDDEN,
				VALUE_HIDDEN_DATA_ORDINARY);
		indOrdinary.addDataPropertyStatement(PROPERTY_DATA_VISIBLE,
				VALUE_VISIBLE_DATA_ORDINARY);
		indOrdinary.addDataPropertyStatement(PROPERTY_DATA_MAYBE,
				VALUE_MAYBE_DATA_ORDINARY);

		indOrdinary.addObjectPropertyStatement(PROPERTY_OBJECT_HIDDEN,
				URI_HIDDEN_OBJECT_ORDINARY);
		indOrdinary.addObjectPropertyStatement(PROPERTY_OBJECT_VISIBLE,
				URI_VISIBLE_OBJECT_ORDINARY);
		indOrdinary.addObjectPropertyStatement(PROPERTY_OBJECT_MAYBE,
				URI_MAYBE_OBJECT_ORDINARY);

		filteredOrdinary = new IndividualFiltering(indOrdinary,
				new IndividualBasedFilter());
	}

	// ----------------------------------------------------------------------
	// Tests on data properties
	// ----------------------------------------------------------------------

	@Test
	public void onSpecial_getDataPropertyStatements() {
		List<DataPropertyStatement> expected = dpsList(filteredSpecial,
				dps(PROPERTY_DATA_MAYBE, VALUE_MAYBE_DATA_SPECIAL),
				dps(PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_SPECIAL));
		List<DataPropertyStatement> actual = filteredSpecial
				.getDataPropertyStatements();
		assertEquivalentDpsList("data property statements", expected, actual);
	}

	@Test
	public void onOrdinary_getDataPropertyStatements() {
		List<DataPropertyStatement> expected = dpsList(filteredOrdinary,
				dps(PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_ORDINARY));
		List<DataPropertyStatement> actual = filteredOrdinary
				.getDataPropertyStatements();
		assertEquivalentDpsList("data property statements", expected, actual);
	}

	@Test
	public void onSpecial_getDataPropertyStatementsByProperty() {
		List<DataPropertyStatement> visibleExpected = dpsList(filteredSpecial,
				dps(PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_SPECIAL));
		List<DataPropertyStatement> visibleActual = filteredSpecial
				.getDataPropertyStatements(PROPERTY_DATA_VISIBLE);
		assertEquivalentDpsList("visible", visibleExpected, visibleActual);

		List<DataPropertyStatement> hiddenExpected = Collections.emptyList();
		List<DataPropertyStatement> hiddenActual = filteredSpecial
				.getDataPropertyStatements(PROPERTY_DATA_HIDDEN);
		assertEquivalentDpsList("hidden", hiddenExpected, hiddenActual);

		List<DataPropertyStatement> maybeExpected = dpsList(filteredSpecial,
				dps(PROPERTY_DATA_MAYBE, VALUE_MAYBE_DATA_SPECIAL));
		List<DataPropertyStatement> maybeActual = filteredSpecial
				.getDataPropertyStatements(PROPERTY_DATA_MAYBE);
		assertEquivalentDpsList("maybe", maybeExpected, maybeActual);
	}

	@Test
	public void onOrdinary_getDataPropertyStatementsByProperty() {
		List<DataPropertyStatement> visibleExpected = dpsList(filteredOrdinary,
				dps(PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_ORDINARY));
		List<DataPropertyStatement> visibleActual = filteredOrdinary
				.getDataPropertyStatements(PROPERTY_DATA_VISIBLE);
		assertEquivalentDpsList("visible", visibleExpected, visibleActual);

		List<DataPropertyStatement> hiddenExpected = Collections.emptyList();
		List<DataPropertyStatement> hiddenActual = filteredOrdinary
				.getDataPropertyStatements(PROPERTY_DATA_HIDDEN);
		assertEquivalentDpsList("hidden", hiddenExpected, hiddenActual);

		List<DataPropertyStatement> maybeExpected = Collections.emptyList();
		List<DataPropertyStatement> maybeActual = filteredOrdinary
				.getDataPropertyStatements(PROPERTY_DATA_MAYBE);
		assertEquivalentDpsList("maybe", maybeExpected, maybeActual);
	}

	@Test
	public void onSpecial_getDataPropertyStatement() {
		DataPropertyStatement visibleExpected = dps(filteredSpecial.getURI(),
				PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_SPECIAL);
		DataPropertyStatement visibleActual = filteredSpecial
				.getDataPropertyStatement(PROPERTY_DATA_VISIBLE);
		assertEquivalentDps("visible", visibleExpected, visibleActual);

		DataPropertyStatement hiddenExpected = null;
		DataPropertyStatement hiddenActual = filteredSpecial
				.getDataPropertyStatement(PROPERTY_DATA_HIDDEN);
		assertEquivalentDps("hidden", hiddenExpected, hiddenActual);

		DataPropertyStatement maybeExpected = dps(filteredSpecial.getURI(),
				PROPERTY_DATA_MAYBE, VALUE_MAYBE_DATA_SPECIAL);
		DataPropertyStatement maybeActual = filteredSpecial
				.getDataPropertyStatement(PROPERTY_DATA_MAYBE);
		assertEquivalentDps("maybe", maybeExpected, maybeActual);
	}

	@Test
	public void onOrdinary_getDataPropertyStatement() {
		DataPropertyStatement visibleExpected = dps(filteredOrdinary.getURI(),
				PROPERTY_DATA_VISIBLE, VALUE_VISIBLE_DATA_ORDINARY);
		DataPropertyStatement visibleActual = filteredOrdinary
				.getDataPropertyStatement(PROPERTY_DATA_VISIBLE);
		assertEquivalentDps("visible", visibleExpected, visibleActual);

		DataPropertyStatement hiddenExpected = null;
		DataPropertyStatement hiddenActual = filteredOrdinary
				.getDataPropertyStatement(PROPERTY_DATA_HIDDEN);
		assertEquivalentDps("hidden", hiddenExpected, hiddenActual);

		DataPropertyStatement maybeExpected = null;
		DataPropertyStatement maybeActual = filteredOrdinary
				.getDataPropertyStatement(PROPERTY_DATA_MAYBE);
		assertEquivalentDps("maybe", maybeExpected, maybeActual);
	}

	@Ignore
	@Test
	public void onSpecial_getDataPropertyList() {
		fail("onSpecial_getDataPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getDataPropertyList() {
		fail("onOrdinary_getDataPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onSpecial_getPopulatedDataPropertyList() {
		fail("onSpecial_getPopulatedDataPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getPopulatedDataPropertyList() {
		fail("onOrdinary_getPopulatedDataPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onSpecial_getDataPropertyMap() {
		fail("onSpecial_getDataPropertyMap not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getDataPropertyMap() {
		fail("onOrdinary_getDataPropertyMap not implemented");
	}

	// ----------------------------------------------------------------------
	// Tests on object properties
	// ----------------------------------------------------------------------

	@Test
	public void onSpecial_getObjectPropertyStatementsByProperty() {
		List<ObjectPropertyStatement> visibleExpected = opsList(
				filteredSpecial,
				ops(PROPERTY_OBJECT_VISIBLE, URI_VISIBLE_OBJECT_SPECIAL));
		List<ObjectPropertyStatement> visibleActual = filteredSpecial
				.getObjectPropertyStatements(PROPERTY_OBJECT_VISIBLE);
		assertEquivalentOpsList("visible", visibleExpected, visibleActual);

		List<ObjectPropertyStatement> hiddenExpected = Collections.emptyList();
		List<ObjectPropertyStatement> hiddenActual = filteredSpecial
				.getObjectPropertyStatements(PROPERTY_OBJECT_HIDDEN);
		assertEquivalentOpsList("hidden", hiddenExpected, hiddenActual);

		List<ObjectPropertyStatement> maybeExpected = opsList(filteredSpecial,
				ops(PROPERTY_OBJECT_MAYBE, URI_MAYBE_OBJECT_SPECIAL));
		List<ObjectPropertyStatement> maybeActual = filteredSpecial
				.getObjectPropertyStatements(PROPERTY_OBJECT_MAYBE);
		assertEquivalentOpsList("maybe", maybeExpected, maybeActual);
	}

	@Test
	public void onOrdinary_getObjectPropertyStatementsByProperty() {
		List<ObjectPropertyStatement> visibleExpected = opsList(
				filteredOrdinary,
				ops(PROPERTY_OBJECT_VISIBLE, URI_VISIBLE_OBJECT_ORDINARY));
		List<ObjectPropertyStatement> visibleActual = filteredOrdinary
				.getObjectPropertyStatements(PROPERTY_OBJECT_VISIBLE);
		assertEquivalentOpsList("visible", visibleExpected, visibleActual);

		List<ObjectPropertyStatement> hiddenExpected = Collections.emptyList();
		List<ObjectPropertyStatement> hiddenActual = filteredOrdinary
				.getObjectPropertyStatements(PROPERTY_OBJECT_HIDDEN);
		assertEquivalentOpsList("hidden", hiddenExpected, hiddenActual);

		List<ObjectPropertyStatement> maybeExpected = Collections.emptyList();
		List<ObjectPropertyStatement> maybeActual = filteredOrdinary
				.getObjectPropertyStatements(PROPERTY_OBJECT_MAYBE);
		assertEquivalentOpsList("maybe", maybeExpected, maybeActual);
	}

	@Test
	public void onSpecial_getObjectPropertyStatements() {
		List<ObjectPropertyStatement> expected = opsList(filteredSpecial,
				ops(PROPERTY_OBJECT_MAYBE, URI_MAYBE_OBJECT_SPECIAL),
				ops(PROPERTY_OBJECT_VISIBLE, URI_VISIBLE_OBJECT_SPECIAL));
		List<ObjectPropertyStatement> actual = filteredSpecial
				.getObjectPropertyStatements();
		assertEquivalentOpsList("object property statements", expected, actual);
	}

	@Test
	public void onOrdinary_getObjectPropertyStatements() {
		List<ObjectPropertyStatement> expected = opsList(filteredOrdinary,
				ops(PROPERTY_OBJECT_VISIBLE, URI_VISIBLE_OBJECT_ORDINARY));
		List<ObjectPropertyStatement> actual = filteredOrdinary
				.getObjectPropertyStatements();
		assertEquivalentOpsList("object property statements", expected, actual);
	}

	@Ignore
	@Test
	public void onSpecial_getObjectPropertyMap() {
		fail("onSpecial_getObjectPropertyMap not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getObjectPropertyMap() {
		fail("onOrdinary_getObjectPropertyMap not implemented");
	}

	@Ignore
	@Test
	public void onSpecial_getObjectPropertyList() {
		fail("onSpecial_getObjectPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getObjectPropertyList() {
		fail("onOrdinary_getObjectPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onSpecial_getPopulatedObjectPropertyList() {
		fail("onSpecial_getPopulatedObjectPropertyList not implemented");
	}

	@Ignore
	@Test
	public void onOrdinary_getPopulatedObjectPropertyList() {
		fail("onOrdinary_getPopulatedObjectPropertyList not implemented");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private DataPropertyStatement dps(String propertyUri, String value) {
		return dps("", propertyUri, value);
	}

	private DataPropertyStatement dps(String subjectUri, String propertyUri,
			String value) {
		return new DPS(subjectUri, propertyUri, value);
	}

	private List<DataPropertyStatement> dpsList(Individual ind,
			DataPropertyStatement... dpsArray) {
		List<DataPropertyStatement> list = new ArrayList<DataPropertyStatement>();
		for (DataPropertyStatement dps : dpsArray) {
			list.add(new DPS(ind.getURI(), dps.getDatapropURI(), dps.getData()));
		}
		return list;
	}

	private void assertEquivalentDpsList(String label,
			Collection<DataPropertyStatement> expected,
			Collection<DataPropertyStatement> actual) {
		Set<DPS> expectedSet = new HashSet<DPS>();
		for (DataPropertyStatement dps : expected) {
			expectedSet.add(new DPS(dps));
		}

		Set<DPS> actualSet = new HashSet<DPS>();
		for (DataPropertyStatement dps : actual) {
			actualSet.add(new DPS(dps));
		}

		assertEquals(label, expectedSet, actualSet);
	}

	private void assertEquivalentDps(String label,
			DataPropertyStatement expected, DataPropertyStatement actual) {
		DPS expectedDps = (expected == null) ? null : new DPS(expected);
		DPS actualDps = (actual == null) ? null : new DPS(actual);
		assertEquals(label, expectedDps, actualDps);
	}

	private ObjectPropertyStatement ops(String propertyUri, String objectUri) {
		return ops("", propertyUri, objectUri);
	}

	private ObjectPropertyStatement ops(String subjectUri, String propertyUri,
			String objectUri) {
		return new OPS(subjectUri, propertyUri, objectUri);
	}

	private List<ObjectPropertyStatement> opsList(Individual ind,
			ObjectPropertyStatement... opsArray) {
		List<ObjectPropertyStatement> list = new ArrayList<ObjectPropertyStatement>();
		for (ObjectPropertyStatement ops : opsArray) {
			list.add(new OPS(ind.getURI(), ops.getPropertyURI(), ops
					.getObjectURI()));
		}
		return list;
	}

	private void assertEquivalentOpsList(String label,
			Collection<ObjectPropertyStatement> expected,
			Collection<ObjectPropertyStatement> actual) {
		Set<OPS> expectedSet = new HashSet<OPS>();
		for (ObjectPropertyStatement ops : expected) {
			expectedSet.add(new OPS(ops));
		}
		Set<OPS> actualSet = new HashSet<OPS>();
		for (ObjectPropertyStatement ops : actual) {
			actualSet.add(new OPS(ops));
		}

		assertEquals(label, expectedSet, actualSet);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class DPS implements DataPropertyStatement {
		// ----------------------------------------------------------------------
		// Stub infrastructure
		// ----------------------------------------------------------------------

		private final String subjectUri;
		private final String predicateUri;
		private final String value;

		public DPS(String subjectUri, String predicateUri, String value) {
			this.subjectUri = subjectUri;
			this.predicateUri = predicateUri;
			this.value = value;
		}

		public DPS(DataPropertyStatement dps) {
			this(dps.getIndividualURI(), dps.getDatapropURI(), dps.getData());
		}

		// ----------------------------------------------------------------------
		// Stub methods
		// ----------------------------------------------------------------------

		@Override
		public String getIndividualURI() {
			return subjectUri;
		}

		@Override
		public String getDatapropURI() {
			return predicateUri;
		}

		@Override
		public String getData() {
			return value;
		}

		@Override
		public int hashCode() {
			return subjectUri.hashCode() ^ predicateUri.hashCode()
					^ value.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DPS)) {
				return false;
			}
			DPS that = (DPS) obj;
			return this.subjectUri.equals(that.subjectUri)
					&& this.predicateUri.equals(that.predicateUri)
					&& this.value.equals(that.value);
		}

		@Override
		public String toString() {
			return "DPS[" + subjectUri + ", " + predicateUri + ", " + value
					+ "]";
		}

		// ----------------------------------------------------------------------
		// Un-implemented methods
		// ----------------------------------------------------------------------

		@Override
		public Individual getIndividual() {
			throw new RuntimeException(
					"DataPropertyStatement.getIndividual() not implemented.");
		}

		@Override
		public void setIndividual(Individual individual) {
			throw new RuntimeException(
					"DataPropertyStatement.setIndividual() not implemented.");
		}

		@Override
		public void setIndividualURI(String individualURI) {
			throw new RuntimeException(
					"DataPropertyStatement.setIndividualURI() not implemented.");
		}

		@Override
		public void setData(String data) {
			throw new RuntimeException(
					"DataPropertyStatement.setData() not implemented.");
		}

		@Override
		public void setDatapropURI(String propertyURI) {
			throw new RuntimeException(
					"DataPropertyStatement.setDatapropURI() not implemented.");
		}

		@Override
		public String getDatatypeURI() {
			throw new RuntimeException(
					"DataPropertyStatement.getDatatypeURI() not implemented.");
		}

		@Override
		public void setDatatypeURI(String datatypeURI) {
			throw new RuntimeException(
					"DataPropertyStatement.setDatatypeURI() not implemented.");
		}

		@Override
		public String getLanguage() {
			throw new RuntimeException(
					"DataPropertyStatement.getLanguage() not implemented.");
		}

		@Override
		public void setLanguage(String language) {
			throw new RuntimeException(
					"DataPropertyStatement.setLanguage() not implemented.");
		}

		@Override
		public String getString() {
			throw new RuntimeException(
					"DataPropertyStatement.getString() not implemented.");
		}

	}

	private static class OPS implements ObjectPropertyStatement {
		// ----------------------------------------------------------------------
		// Stub infrastructure
		// ----------------------------------------------------------------------

		private final String subjectUri;
		private final String predicateUri;
		private final String objectUri;

		public OPS(String subjectUri, String predicateUri, String objectUri) {
			this.subjectUri = subjectUri;
			this.predicateUri = predicateUri;
			this.objectUri = objectUri;
		}

		public OPS(ObjectPropertyStatement ops) {
			this(ops.getSubjectURI(), ops.getPropertyURI(), ops.getObjectURI());
		}

		// ----------------------------------------------------------------------
		// Stub methods
		// ----------------------------------------------------------------------

		@Override
		public String getSubjectURI() {
			return subjectUri;
		}

		@Override
		public String getPropertyURI() {
			return predicateUri;
		}

		@Override
		public String getObjectURI() {
			return objectUri;
		}

		@Override
		public int hashCode() {
			return subjectUri.hashCode() ^ predicateUri.hashCode()
					^ objectUri.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof OPS)) {
				return false;
			}
			OPS that = (OPS) obj;
			return this.subjectUri.equals(that.subjectUri)
					&& this.predicateUri.equals(that.predicateUri)
					&& this.objectUri.equals(that.objectUri);
		}

		@Override
		public String toString() {
			return "OPS[" + subjectUri + ", " + predicateUri + ", " + objectUri
					+ "]";
		}

		// ----------------------------------------------------------------------
		// Un-implemented methods
		// ----------------------------------------------------------------------

		@Override
		public boolean isSubjectOriented() {
			throw new RuntimeException(
					"ObjectPropertyStatement.isSubjectOriented() not implemented.");
		}

		@Override
		public void setSubjectOriented(boolean subjectOriented) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setSubjectOriented() not implemented.");
		}

		@Override
		public void setSubjectURI(String subjectURI) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setSubjectURI() not implemented.");
		}

		@Override
		public void setObjectURI(String objectURI) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setObjectURI() not implemented.");
		}

		@Override
		public Individual getSubject() {
			throw new RuntimeException(
					"ObjectPropertyStatement.getSubject() not implemented.");
		}

		@Override
		public void setSubject(Individual subject) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setSubject() not implemented.");
		}

		@Override
		public ObjectProperty getProperty() {
			throw new RuntimeException(
					"ObjectPropertyStatement.getProperty() not implemented.");
		}

		@Override
		public void setProperty(ObjectProperty property) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setProperty() not implemented.");
		}

		@Override
		public Individual getObject() {
			throw new RuntimeException(
					"ObjectPropertyStatement.getObject() not implemented.");
		}

		@Override
		public void setObject(Individual object) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setObject() not implemented.");
		}

		@Override
		public void setPropertyURI(String URI) {
			throw new RuntimeException(
					"ObjectPropertyStatement.setPropertyURI() not implemented.");
		}

		@Override
		public PropertyInstance toPropertyInstance() {
			throw new RuntimeException(
					"ObjectPropertyStatement.toPropertyInstance() not implemented.");
		}

	}

	private static class IndividualBasedFilter implements VitroFilters {
		// ----------------------------------------------------------------------
		// Stub infrastructure
		// ----------------------------------------------------------------------

		private static class DataPropertyStatementFilter extends
				UnaryFunctor<DataPropertyStatement, Boolean> {
			@Override
			public Boolean fn(DataPropertyStatement dps) {
				if (PROPERTY_DATA_VISIBLE.equals(dps.getDatapropURI())) {
					return true;
				}
				if (PROPERTY_DATA_MAYBE.equals(dps.getDatapropURI())
						&& URI_INDIVIDUAL_SPECIAL
								.equals(dps.getIndividualURI())) {
					return true;
				}
				return false;
			}
		}

		private static class ObjectPropertyStatementFilter extends
				UnaryFunctor<ObjectPropertyStatement, Boolean> {
			@Override
			public Boolean fn(ObjectPropertyStatement ops) {
				if (PROPERTY_OBJECT_VISIBLE.equals(ops.getPropertyURI())) {
					return true;
				}
				if (PROPERTY_OBJECT_MAYBE.equals(ops.getPropertyURI())
						&& URI_INDIVIDUAL_SPECIAL.equals(ops.getSubjectURI())) {
					return true;
				}
				return false;
			}
		}

		// ----------------------------------------------------------------------
		// Stub methods
		// ----------------------------------------------------------------------

		@Override
		public UnaryFunctor<DataPropertyStatement, Boolean> getDataPropertyStatementFilter() {
			return new DataPropertyStatementFilter();
		}

		@Override
		public UnaryFunctor<ObjectPropertyStatement, Boolean> getObjectPropertyStatementFilter() {
			return new ObjectPropertyStatementFilter();
		}

		// ----------------------------------------------------------------------
		// Un-implemented methods
		// ----------------------------------------------------------------------

		@Override
		public VitroFilters and(VitroFilters other) {
			throw new RuntimeException("VitroFilters.and() not implemented.");
		}

		@Override
		public UnaryFunctor<Individual, Boolean> getIndividualFilter() {
			throw new RuntimeException(
					"VitroFilters.getIndividualFilter() not implemented.");
		}

		@Override
		public UnaryFunctor<DataProperty, Boolean> getDataPropertyFilter() {
			throw new RuntimeException(
					"VitroFilters.getDataPropertyFilter() not implemented.");
		}

		@Override
		public UnaryFunctor<ObjectProperty, Boolean> getObjectPropertyFilter() {
			throw new RuntimeException(
					"VitroFilters.getObjectPropertyFilter() not implemented.");
		}

		@Override
		public UnaryFunctor<VClass, Boolean> getClassFilter() {
			throw new RuntimeException(
					"VitroFilters.getClassFilter() not implemented.");
		}

		@Override
		public UnaryFunctor<VClassGroup, Boolean> getVClassGroupFilter() {
			throw new RuntimeException(
					"VitroFilters.getVClassGroupFilter() not implemented.");
		}

		@Override
		public UnaryFunctor<PropertyGroup, Boolean> getPropertyGroupFilter() {
			throw new RuntimeException(
					"VitroFilters.getPropertyGroupFilter() not implemented.");
		}

	}

}
