package edu.cornell.mannlib.vitro.webapp.i18n;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class TranslationProviderTest {

    private static final String VITRO = "Vitro";
    private static final String VIVO = "VIVO";
    private static final String ROOT =
            "src/test/resources/edu/cornell/mannlib/vitro/webapp/i18n/TranslationProviderTest/";
    private static final String TRANSLATIONS_N3_FILE = ROOT + "modelInitContent.n3";
    private static final String WILMA = "wilma";
    private static final String NEMO = "nemo";

    private Model i18nModel;
    private RDFServiceModel rdfService;
    private TranslationProvider tp;

    @Before
    public void init() {
        Logger logger = LogManager.getLogger(TranslationProvider.class);
        logger.setLevel(Level.ERROR);
    }

    @After
    public void finish() {
        Logger logger = LogManager.getLogger(TranslationProvider.class);
        logger.setLevel(Level.INFO);
    }

    public void init(String i18nFile, String themeName, String appName) throws FileNotFoundException {
        i18nModel = ModelFactory.createDefaultModel();
        i18nModel.read(new FileReader(new File(i18nFile)), null, "n3");
        Dataset ds = DatasetFactory.createTxnMem();
        ds.addNamedModel("http://vitro.mannlib.cornell.edu/default/interface-i18n", i18nModel);
        rdfService = new RDFServiceModel(ds);
        tp = TranslationProvider.getInstance();
        tp.rdfService = rdfService;
        tp.setTheme(themeName);
        tp.application = appName;
        tp.clearCache();
    }

    @Test
    public void testNotExistingKey() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "non_existing_key", array);
        assertEquals("ERROR: Translation not found 'non_existing_key'", translation);
    }

    @Test
    public void testVitroWilmaEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey Vitro wilma en-US", translation);
    }

    @Test
    public void testVitroWilmaDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array);
        assertEquals("testkey Vitro wilma de-DE", translation);
    }

    @Test
    public void testVIVOWilmaEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey VIVO wilma en-US", translation);
    }

    @Test
    public void testVIVOWilmaDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array);
        assertEquals("testkey VIVO wilma de-DE", translation);
    }

    @Test
    public void testThemeFallbackVitroNemoEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey Vitro no theme en-US", translation);
    }

    @Test
    public void testThemeFallbackVitroNemoDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array);
        assertEquals("testkey Vitro no theme de-DE", translation);
    }

    @Test
    public void testThemeFallbackVIVONemoEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey VIVO no theme en-US", translation);
    }

    @Test
    public void testThemeFallbackVIVONemoDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array);
        assertEquals("testkey VIVO no theme de-DE", translation);
    }

    @Test
    public void testAppFallbackVIVONemoEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey_app_fallback", array);
        assertEquals("testkey_app_fallback Vitro wilma en-US", translation);
    }

    @Test
    public void testAppFallbackVIVONemoDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey_app_fallback", array);
        assertEquals("testkey_app_fallback Vitro wilma de-DE", translation);
    }

    @Test
    public void testAppAndThemeFallbackVIVONemoEnUS() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey_app_fallback", array);
        assertEquals("testkey_app_fallback Vitro no theme en-US", translation);
    }

    @Test
    public void testAppAndThemeFallbackVIVONemoDeDE() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey_app_fallback", array);
        assertEquals("testkey_app_fallback Vitro no theme de-DE", translation);
    }

    @Test
    public void testCache() throws FileNotFoundException {
        init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
        Object array[] = {};
        String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey Vitro wilma en-US", translation);
        tp.application = VIVO;
        translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey Vitro wilma en-US", translation);
        tp.clearCache();
        translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array);
        assertEquals("testkey VIVO wilma en-US", translation);
    }
}
