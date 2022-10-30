package edu.cornell.mannlib.vitro.webapp.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class TranslationProviderTest {

	private static final String VITRO = "Vitro";
	private static final String VIVO = "VIVO";


	private static final String ROOT = "src/test/resources/edu/cornell/mannlib/vitro/webapp/i18n/TranslationProviderTest/";

	private static final String TRANSLATIONS_N3_FILE = ROOT + "modelInitContent.n3";
	private static final String WILMA = ROOT + "appMetadataWilma.n3";
	private static final String NEMO = ROOT + "appMetadataNemo.n3";

	private Model i18nModel;
	private Model appMetaModel;
	private RDFServiceModel rdfService;
	private TranslationProvider tp;

	public void init(String i18nFile, String themeMetaFilePath, String appName) {
		i18nModel = ModelFactory.createDefaultModel();
		appMetaModel = ModelFactory.createDefaultModel();
		try {
			i18nModel.read(new FileReader(new File(i18nFile)), null, "n3");
			appMetaModel.read(new FileReader(new File(themeMetaFilePath)), null, "n3");
		} catch(Exception e) {
			
		}
		Dataset ds = DatasetFactory.createTxnMem();
		ds.addNamedModel("http://vitro.mannlib.cornell.edu/default/interface-i18n", i18nModel);
		ds.addNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata", appMetaModel);
		rdfService = new RDFServiceModel(ds);
		tp = TranslationProvider.getInstance();
		tp.rdfService = rdfService;
		tp.application = appName;
		tp.clearCache();
	}

	@Test
	public void testNotExistingKey() throws FileNotFoundException {
		init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "non_existing_key", array );
		assertEquals("ERROR: Translation not found 'non_existing_key'", translation);
	}
	
	@Test
	public void testVitroWilmaEnUS() {
		init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array );
		assertEquals("testkey Vitro wilma en-US", translation);
	}
	
	@Test
	public void testVitroWilmaDeDE() {
		init(TRANSLATIONS_N3_FILE, WILMA, VITRO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array );
		assertEquals("testkey Vitro wilma de-DE", translation);
	}
	
	@Test
	public void testVIVOWilmaEnUS() {
		init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array );
		assertEquals("testkey VIVO wilma en-US", translation);
	}
	
	@Test
	public void testVIVOWilmaDeDE() {
		init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array );
		assertEquals("testkey VIVO wilma de-DE", translation);
	}
	
	@Test
	public void testThemeFallbackVitroNemoEnUS() {
		init(TRANSLATIONS_N3_FILE, NEMO, VITRO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array );
		assertEquals("testkey Vitro no theme en-US", translation);
	}
	
	@Test
	public void testThemeFallbackVitroNemoDeDE() {
		init(TRANSLATIONS_N3_FILE, NEMO, VITRO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array );
		assertEquals("testkey Vitro no theme de-DE", translation);
	}
	
	@Test
	public void testThemeFallbackVIVONemoEnUS() {
		init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey", array );
		assertEquals("testkey VIVO no theme en-US", translation);
	}
	
	@Test
	public void testThemeFallbackVIVONemoDeDE() {
		init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey", array );
		assertEquals("testkey VIVO no theme de-DE", translation);
	}

	@Test
	public void testAppFallbackVIVONemoEnUS() {
		init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey_app_fallback", array );
		assertEquals("testkey_app_fallback Vitro wilma en-US", translation);
	}
	
	@Test
	public void testAppFallbackVIVONemoDeDE() {
		init(TRANSLATIONS_N3_FILE, WILMA, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey_app_fallback", array );
		assertEquals("testkey_app_fallback Vitro wilma de-DE", translation);
	}
	
	@Test
	public void testAppAndThemeFallbackVIVONemoEnUS() {
		init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("en-US"), "testkey_app_fallback", array );
		assertEquals("testkey_app_fallback Vitro no theme en-US", translation);
	}
	
	@Test
	public void testAppAndThemeFallbackVIVONemoDeDE() {
		init(TRANSLATIONS_N3_FILE, NEMO, VIVO);
		Object array[]={};
		String translation = tp.getTranslation(Collections.singletonList("de-DE"), "testkey_app_fallback", array );
		assertEquals("testkey_app_fallback Vitro no theme de-DE", translation);
	}


	
	
}
