package edu.cornell.mannlib.vitro.webapp.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stubs.javax.servlet.ServletContextStub;


public class TranslationConverterTest {
	private static final String WILMA = "wilma";
	private static final String HAS_THEME = "http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasTheme";
	private static final String VITRO = "Vitro";
	private static final String VIVO = "VIVO";
	private static final String HAS_APP = "http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasApp";
	private static final String HAS_KEY = "http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasKey";
	private static final String ROOT_PATH = "src/test/resources/edu/cornell/mannlib/vitro/webapp/i18n/TranslationConverterTest/root";
	private static final String INIT_N3_FILE = "src/test/resources/edu/cornell/mannlib/vitro/webapp/i18n/TranslationConverterTest/modelInitContent.n3";
	ServletContextStub ctx = new ServletContextStub();
	private OntModel model;
	private boolean debug = false;
	
    @Before
    public void init() {
        Logger logger = LogManager.getLogger(TranslationConverter.class);
        logger.setLevel(Level.ERROR);
    }

    @After
    public void finish() {
        Logger logger = LogManager.getLogger(TranslationConverter.class);
        logger.setLevel(Level.INFO);
    }

    @Test
    public void testConversion() throws FileNotFoundException {
        VitroResourceBundle.addAppPrefix("customprefix");
		VitroResourceBundle.addAppPrefix("vivo");
		TranslationConverter converter = TranslationConverter.getInstance();
		model = converter.memModel;
		File n3 = new File(INIT_N3_FILE);
		assertTrue(model.isEmpty());
		model.read(new FileReader(n3), null, "n3");
		assertFalse(model.isEmpty());
		File appI18n = new File(ROOT_PATH + TranslationConverter.APP_I18N_PATH);
		File localI18n = new File(ROOT_PATH + TranslationConverter.LOCAL_I18N_PATH);
		File themes = new File(ROOT_PATH + TranslationConverter.THEMES_PATH);
		ctx.setRealPath(TranslationConverter.APP_I18N_PATH, appI18n.getAbsolutePath());
		ctx.setRealPath(TranslationConverter.LOCAL_I18N_PATH, localI18n.getAbsolutePath());
		ctx.setRealPath(TranslationConverter.THEMES_PATH, themes.getAbsolutePath());
		converter.ctx = ctx;
		converter.convertAll();

		assertEquals(2, getCount(HAS_KEY, "test_key_all_en_US"));
		assertEquals(2, getCount(HAS_KEY, "test_key_all_en_CA"));

		assertEquals(2, getCount(HAS_KEY, "test_key_all"));

		assertEquals(1, getCount(HAS_KEY, "property_to_overwrite"));

		assertTrue(n3TranslationValueIsOverwrittenByProperty(model));

		assertEquals(4, getCount(HAS_THEME, WILMA));
		assertEquals(7, getCount(HAS_APP, VITRO));
		assertEquals(3, getCount(HAS_APP, VIVO));
		
		checkTranslationLabelWithUri();
		if (debug) {
		    printResultModel();
		}
	}

	private void checkTranslationLabelWithUri() {
	    Selector selector = new SimpleSelector(null, new PropertyImpl(HAS_KEY), "translation_with_uri");
        StmtIterator it = model.listStatements(selector);
        assertTrue(it.hasNext());
        Resource subject = it.next().getSubject();
        Selector labelSelector = new SimpleSelector(subject, RDFS.label, (Object) null);
        StmtIterator it2 = model.listStatements(labelSelector);
        assertTrue(it2.hasNext());
        String labelText = it2.next().getObject().asLiteral().getLexicalForm();
        assertTrue(labelText.contains("?uri"));
    }

    private void printResultModel() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N3");
		System.out.println(baos.toString());
	}
	
	private int getCount(String hasTheme, String wilma) {
		Selector selector = new SimpleSelector(null, new PropertyImpl(hasTheme), wilma);
		StmtIterator it = model.listStatements(selector);
		int count = 0;
		while (it.hasNext()) {
			count++;
			it.next();
		}
		return count;
	}
	
	private boolean n3TranslationValueIsOverwrittenByProperty(OntModel model) {
		return model.getGraph().contains(
                NodeFactory.createURI("urn:uuid:8c80dbf5-adda-41d5-a6fe-d5efde663600"),
                NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                NodeFactory.createLiteral("value from properties file","en-US"));
	}
}
