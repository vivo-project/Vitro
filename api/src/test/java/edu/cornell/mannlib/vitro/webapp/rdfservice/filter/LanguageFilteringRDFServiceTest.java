/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import stubs.org.apache.jena.rdf.model.LiteralStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * This is the matching order we expect to see:
 *
 * <pre>
 * exact match to preferred, by order.
 * partial match to preferred, by order.
 * vanilla or null (no language)
 * no match
 * </pre>
 */
public class LanguageFilteringRDFServiceTest extends AbstractTestClass {

	private static final Log log = LogFactory
			.getLog(LanguageFilteringRDFServiceTest.class);

	private static final String COLLATOR_CLASSNAME = "edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService$RowIndexedLiteralSortByLang";
	private static final String RIL_CLASSNAME = "edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService$RowIndexedLiteral";

	private static final String TEST_CONSTRUCT_QUERY = "CONSTRUCT { ?s ?o ?p . } WHERE { ?s ?o ?p . }";

	private LanguageFilteringRDFService filteringRDFService;
	private List<Object> listOfRowIndexedLiterals;
	private int literalIndex;

	private List<String> preferredLanguages;
	private List<String> availableLanguages;
	private List<String> expectedSortOrders;

	@Before
	public void setup() {
//		setLoggerLevel(this.getClass(), Level.DEBUG);
//		setLoggerLevel(LanguageFilteringRDFService.class, Level.DEBUG);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void singleMatch() {
		preferredLanguages = list("en-US");
		availableLanguages = list("en-US");
		expectedSortOrders = list("en-US");
		testArbitraryOrder();
	}

	@Test
	public void singleNoMatch() {
		preferredLanguages = list("en-US");
		availableLanguages = list("es-MX");
		expectedSortOrders = list("es-MX");
		testArbitraryOrder();
	}

	@Test
	public void doubleMatch() {
		preferredLanguages = list("en-US", "es-MX");
		availableLanguages = list("en-US", "es-MX");
		expectedSortOrders = list("en-US", "es-MX");
		testBothWays();
	}

	@Test
	public void noMatches() {
		preferredLanguages = list("es-MX");
		availableLanguages = list("en-US", "fr-FR");
		expectedSortOrders = list("en-US", "fr-FR");
		testArbitraryOrder();
	}

	@Test
	public void partialMatches() {
		preferredLanguages = list("en", "es");
		availableLanguages = list("en-US", "es-MX");
		expectedSortOrders = list("en-US", "es-MX");
		testBothWays();
	}

	@Test
	public void matchIsBetterThanNoMatch() {
		preferredLanguages = list("en-US", "es-MX");
		availableLanguages = list("en-US", "fr-FR");
		expectedSortOrders = list("en-US", "fr-FR");
		testBothWays();
	}

	@Test
	public void matchIsBetterThanPartialMatch() {
		preferredLanguages = list("es-ES", "en-US");
		availableLanguages = list("en-US", "es-MX");
		expectedSortOrders = list("en-US", "es-MX");
		testBothWays();
	}

	@Test
	public void exactMatchIsBetterThanPartialMatch() {
		preferredLanguages = list("es");
		availableLanguages = list("es", "es-MX");
		expectedSortOrders = list("es", "es-MX");
		testBothWays();
	}

	@Test
	public void matchIsBetterThanVanilla() {
		preferredLanguages = list("en-US");
		availableLanguages = list("en-US", "");
		expectedSortOrders = list("en-US", "");
		testBothWays();
	}

	@Test
	public void partialMatchIsBetterThanVanilla() {
		preferredLanguages = list("es-MX");
		availableLanguages = list("es-ES", "");
		expectedSortOrders = list("es-ES", "");
		testBothWays();
	}

	@Test
	public void vanillaIsBetterThanNoMatch() {
		preferredLanguages = list("es-MX");
		availableLanguages = list("en-US", "");
		expectedSortOrders = list("", "en-US");
		testBothWays();
	}

	@Test
	public void omnibus() {
		preferredLanguages = list("es-MX", "es", "en-UK", "es-PE", "fr");
		availableLanguages = list("es-MX", "es", "fr", "es-ES", "fr-FR", "",
				"de-DE");
		expectedSortOrders = list("es-MX", "es", "fr", "es-ES", "fr-FR", "",
				"de-DE");
		testBothWays();
	}

	@Test
	public void sparqlConstructQueryTestGetPreferredExistingLang() throws RDFServiceException {
		preferredLanguages = list("en-US", "de-DE");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelEn_De_NoTag.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		assertEquals(model.listStatements().next().getObject().toString(), "US label@en-US");
	}
	
	@Test
	public void sparqlConstructQueryTestGetSecodAvailPreferredLang() throws RDFServiceException {
		preferredLanguages = list("en-US", "de-DE");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelDe_NoTag.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		assertEquals(model.listStatements().next().getObject().toString(), "DE label@de-DE");
	}

	@Test
	public void sparqlConstructQueryTestGetNoTagAsLangsNotAvailable() throws RDFServiceException {
		preferredLanguages = list("en-US", "de-DE");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelNoTag.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		assertEquals(model.listStatements().next().getObject().toString(), "no tag label");
	}
	
	@Test
	public void sparqlConstructQueryTestPrivateFallback1() throws RDFServiceException {
		preferredLanguages = list("fr-CA");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelPrivateFallback.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "fr@fr";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}

	@Test
	public void sparqlConstructQueryTestPrivateFallback2() throws RDFServiceException {
		preferredLanguages = list("fr");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelPrivateFallback2.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "TIB FR ca@fr-CA-x-tib";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}

	@Test
	public void sparqlConstructQueryTestPrivateFallback3() throws RDFServiceException {
		preferredLanguages = list("fr-CA-x-tib");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelPrivateFallback3.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "fr@fr";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}
	
	@Test
	public void sparqlConstructQueryTestPrivate() throws RDFServiceException {
		preferredLanguages = list("fr-CA-x-tib");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelPrivate.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "TIB FR ca@fr-CA-x-tib";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}
	
	@Test
	public void sparqlConstructQueryTestFallback1() throws RDFServiceException {
		preferredLanguages = list("fr-CA");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelFallback.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "fr CH@fr-CH";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}
	
	@Test
	public void sparqlConstructQueryTestFallback2() throws RDFServiceException {
		preferredLanguages = list("fr");
		OntModel model;
		createLanguageFilter("LangFilteringTestModelFallback.n3");
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		filteringRDFService.sparqlConstructQuery(TEST_CONSTRUCT_QUERY, model);
		assertEquals(model.size(),1);
		final String expected = "fr CH@fr-CH";
		assertEquals(expected, model.listStatements().next().getObject().toString());
	}



	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * Sort the available languages as they are presented. Then reverse them and
	 * sort again.
	 */
	private void testBothWays() {
		createLanguageFilter(null);

		buildListOfLiterals();
		sortListOfLiterals();
		assertLanguageOrder("sort literals");

		buildReversedListOfLiterals();
		sortListOfLiterals();
		assertLanguageOrder("sort reversed literals");
	}

	/**
	 * Sort the available languages, without caring what the eventual sorted
	 * order is. Really, this is just a test to see that no exceptions are
	 * thrown, and no languages are "lost in translation".
	 */
	private void testArbitraryOrder() {
		createLanguageFilter(null);

		buildListOfLiterals();
		sortListOfLiterals();
		assertLanguages("sort literals");

		buildReversedListOfLiterals();
		sortListOfLiterals();
		assertLanguages("sort reversed literals");
	}

	private List<String> list(String... strings) {
		return new ArrayList<String>(Arrays.asList(strings));
	}

	private void createLanguageFilter(String fileName) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		if (fileName != null) {
			Reader reader = new InputStreamReader(this.getClass().getResourceAsStream(fileName));
			model.read(reader, "http://test.edu/", "N3");
		}
		RDFService rdfService = new RDFServiceModel(model);
		filteringRDFService = new LanguageFilteringRDFService(rdfService,	preferredLanguages);
	}

	private void buildListOfLiterals() {
		List<Object> list = new ArrayList<Object>();
		for (String language : availableLanguages) {
			list.add(buildRowIndexedLiteral(language));
		}
		listOfRowIndexedLiterals = list;
	}

	private void buildReversedListOfLiterals() {
		List<Object> list = new ArrayList<Object>();
		for (String language : availableLanguages) {
			list.add(0, buildRowIndexedLiteral(language));
		}
		listOfRowIndexedLiterals = list;
	}

	private void sortListOfLiterals() {
		log.debug("before sorting: "
				+ languagesFromLiterals(listOfRowIndexedLiterals));
		Comparator<Object> comparator = buildRowIndexedLiteralSortByLang();
		listOfRowIndexedLiterals.sort(comparator);
	}

	private void assertLanguageOrder(String message) {
		List<String> expectedLanguages = expectedSortOrders;
		log.debug("expected order: " + expectedLanguages);

		List<String> actualLanguages = languagesFromLiterals(listOfRowIndexedLiterals);
		log.debug("actual order:   " + actualLanguages);

		assertEquals(message, expectedLanguages, actualLanguages);
	}

	private void assertLanguages(String message) {
		Set<String> expectedLanguages = new HashSet<String>(expectedSortOrders);
		log.debug("expected languages: " + expectedLanguages);

		Set<String> actualLanguages = new HashSet<String>(
				languagesFromLiterals(listOfRowIndexedLiterals));
		log.debug("actual languages:   " + actualLanguages);

		assertEquals(message, expectedLanguages, actualLanguages);
	}

	private List<String> languagesFromLiterals(List<Object> literals) {
		List<String> actualLanguages = new ArrayList<String>();
		for (Object ril : literals) {
			actualLanguages.add(getLanguageFromRowIndexedLiteral(ril));
		}
		return actualLanguages;
	}

	// ----------------------------------------------------------------------
	// Reflection methods to get around "private" declarations.
	// ----------------------------------------------------------------------

	private Object buildRowIndexedLiteral(String language) {
		try {
			Class<?> clazz = Class.forName(RIL_CLASSNAME);
			Class<?>[] argTypes = { LanguageFilteringRDFService.class,
					Literal.class, Integer.TYPE };
			Constructor<?> constructor = clazz.getDeclaredConstructor(argTypes);
			constructor.setAccessible(true);

			Literal l = new LiteralStub(language);
			int i = literalIndex++;
			return constructor.newInstance(filteringRDFService, l, i);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not create a row-indexed literal", e);
		}
	}

	@SuppressWarnings("unchecked")
	private Comparator<Object> buildRowIndexedLiteralSortByLang() {
		try {
			Class<?> clazz = Class.forName(COLLATOR_CLASSNAME);
			Class<?>[] argTypes = { LanguageFilteringRDFService.class, List.class };
			Constructor<?> constructor = clazz.getDeclaredConstructor(argTypes);						
			constructor.setAccessible(true);
			return (Comparator<Object>) constructor
					.newInstance(filteringRDFService, new AcceptableLanguages(
					        preferredLanguages));
		} catch (Exception e) {
			throw new RuntimeException("Could not create a collator", e);
		}
	}

	private String getLanguageFromRowIndexedLiteral(Object ril) {
		try {
			Method m = ril.getClass().getDeclaredMethod("getLiteral");
			m.setAccessible(true);
			Literal l = (Literal) m.invoke(ril);
			return l.getLanguage();
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not get the Literal from a RowIndexedLiteral", e);
		}
	}

}
