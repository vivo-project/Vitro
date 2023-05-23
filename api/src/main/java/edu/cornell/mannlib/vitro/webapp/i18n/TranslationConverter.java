package edu.cornell.mannlib.vitro.webapp.i18n;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;


public class TranslationConverter {

	protected OntModel memModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	protected ServletContext ctx;
	private static final boolean BEGIN = true;
	private static final boolean END = !BEGIN;
	private static final int SUFFIX_LENGTH = ".properties".length();
	private static final Log log = LogFactory.getLog(TranslationConverter.class);
	private static final TranslationConverter INSTANCE = new TranslationConverter();
	private static final String THEMES = "themes";
	private static final String ALL = "all";
	protected static final String APP_I18N_PATH = "/i18n/";
	protected static final String LOCAL_I18N_PATH = "/local/i18n/";
	protected static final String THEMES_PATH = "/themes/";
	private static final String TEMPLATE_BODY = ""
			+ "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#NamedIndividual> .\n"
			+ "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vivoweb.org/ontology/vitro/ui-label/vocabulary#UILabel> .\n"
			+ "?uri <http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasApp> ?application .\n"
			+ "?uri <http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasKey> ?key .\n";
	private static final String TEMPLATE_LABEL = ""
			+ "?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label .\n";
	private static final String TEMPLATE_THEME = ""
			+ "?uri <http://vivoweb.org/ontology/vitro/ui-label/vocabulary#hasTheme> ?theme .\n";
	
	private static final String queryWithTheme(String langTag) { 
		return
			  "SELECT ?uri ?label WHERE {"
			+ TEMPLATE_BODY
			+ optionalLabel(langTag)
			+ TEMPLATE_THEME
			+ "}";
	}
	
	private static final String queryNoTheme(String langTag) { 
        return
            "SELECT ?uri ?label WHERE {"
            + TEMPLATE_BODY
            + optionalLabel(langTag)
            + "FILTER NOT EXISTS {"
            + TEMPLATE_THEME
            + "}"
            + "}";
	}
	
	private static final String optionalLabel(String langTag) {
		return
			  "OPTIONAL {"
			+ "?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label .\n "
			+ "FILTER (LANG(?label)=\"" + langTag + "\")"
			+ "}";
	}
		
	public static TranslationConverter getInstance() {
		return INSTANCE;
	}

	public void initialize(ServletContext ctx) {
		this.ctx = ctx;
		OntModel tdbModel = ModelAccess.on(ctx).getOntModel(ModelNames.INTERFACE_I18N);
		RDFService rdfService = ModelAccess.on(ctx).getRDFService(CONFIGURATION);
		memModel.add(tdbModel);
		convertAll();
		cleanTdbModel(tdbModel, rdfService);
		updateTDBModel(rdfService);
	}

	private void cleanTdbModel(OntModel storedModel, RDFService rdfService) {
		ChangeSet cs = makeChangeSet(rdfService);
		ByteArrayOutputStream removeOS = new ByteArrayOutputStream();
		storedModel.write(removeOS, "N3");
		InputStream removeIS = new ByteArrayInputStream(removeOS.toByteArray());
		cs.addRemoval(removeIS, RDFServiceUtils.getSerializationFormatFromJenaString("N3"), ModelNames.INTERFACE_I18N);
		try {
			rdfService.changeSetUpdate(cs);
		} catch (RDFServiceException e) {
			log.error(e,e);
		}
	}

	private void updateTDBModel(RDFService rdfService) {
		ChangeSet cs = makeChangeSet(rdfService);
		ByteArrayOutputStream addOS = new ByteArrayOutputStream();
		memModel.write(addOS, "N3");
		InputStream addIS = new ByteArrayInputStream(addOS.toByteArray());
		cs.addAddition(addIS, RDFServiceUtils.getSerializationFormatFromJenaString("N3"), ModelNames.INTERFACE_I18N);
		try {
			rdfService.changeSetUpdate(cs);
		} catch (RDFServiceException e) {
			log.error(e,e);
		}
	}
	
	public void convertAll() {
		List<String> i18nDirs = new LinkedList<>(Arrays.asList(APP_I18N_PATH, LOCAL_I18N_PATH, THEMES_PATH));
		List<String> prefixes = VitroResourceBundle.getAppPrefixes();
		prefixes.add("");
		String prefixesRegex = "(" + StringUtils.join(prefixes, ALL + "|") + ALL + ")";
		log.debug("prefixesRegex " + prefixesRegex);
		for (String dir : i18nDirs) {
			File realDir = new File(ctx.getRealPath(dir));
			if (realDir.isDirectory()) {
				Collection<File> files = FileUtils.listFiles(realDir, new RegexFileFilter(prefixesRegex + ".*\\.properties"), DirectoryFileFilter.DIRECTORY);
				for (File file : files) {
					convert(file);
				}	
			}
		}
	}

	private void convert(File file) {
		Properties props = new Properties();
		try (Reader reader = new InputStreamReader( new FileInputStream(file), "UTF-8")) {
			props.load(reader);
		} catch (Exception e) {
			log.error(e,e);
		} 
		if (props == null || props.isEmpty()) {
			return;
		}
		log.info("Converting properties " + file.getAbsolutePath());
		String theme = getTheme(file);
		String application = getApplication(file);
		String language = getLanguage(file);
		String langTag = getLanguageTag(language);
		StringWriter additions = new StringWriter();
		StringWriter retractionsN3 = new StringWriter();
		for (Object key : props.keySet()) {
			Object value = props.get(key);
			QueryExecution queryExecution = getQueryExecution(key.toString(), theme, application, langTag);
			ResultSet results = queryExecution.execSelect();
			String uri = null;
			if (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				uri = solution.get("uri").toString();
				String label = getLabel(solution);	
				if (labelAreadyExists(value, label)) {
					continue;
				}
				if (!StringUtils.isBlank(label)) {
					String retraction = fillOutLabelTemplate(uri, label, langTag);
					retractionsN3.append(retraction);
				}
			} 
			String addition = fillOutTemplate(uri, key.toString(), value.toString(), theme, application, langTag);
			additions.append(addition);
		}
		log.debug("Remove from model" + retractionsN3.toString());
		log.debug("Add to model" + additions.toString());
		OntModel addModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel removeModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		addModel.read(new StringReader(additions.toString()), null, "n3");
		removeModel.read(new StringReader(retractionsN3.toString()), null, "n3");
		memModel.enterCriticalSection(Lock.WRITE);
		try {
			memModel.remove(removeModel);
			memModel.add(addModel);
		} finally {
			memModel.leaveCriticalSection();	
		}
		log.info("Conversion finished for properties " + file.getAbsolutePath());

	}

	private String getLanguageTag(String language) {
		return language.replaceAll("_","-");
	}

	private String getLabel(QuerySolution solution) {
		final RDFNode label = solution.get("label");
		if (label == null) {
			return "";
		}
		return ((Literal)label).getLexicalForm();
	}
	
	private boolean labelAreadyExists(Object value, String label) {
		return label.equals(value.toString());
	}

	private String fillOutTemplate(String uri, String key, String newLabel, String theme, String application, String langTag) {
		if (StringUtils.isBlank(uri)) {
			return fillOutFullTemplate(key, newLabel, theme, application, langTag);	
		} else {
			return fillOutLabelTemplate(uri, newLabel, langTag);
		}
	}

	private String fillOutLabelTemplate(String uri, String label, String langTag) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.setCommandText(TEMPLATE_LABEL);
		pss.setIri("uri", uri);
		pss.setLiteral("label", label, langTag);
		return pss.toString();
	}

	private String fillOutFullTemplate(String key, String label, String theme, String application, String langTag) {
		String template = getBodyTemplate(theme);
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.setCommandText(template);
		pss.setIri("uri", createUUID());
		pss.setLiteral("label", label, langTag);
		pss.setLiteral("key", key);
		pss.setLiteral("application", application);
		if (!StringUtils.isBlank(theme)) {
			pss.setLiteral("theme", theme);
		}
		return pss.toString();
	}

	private QueryExecution getQueryExecution(String key, String theme, String application, String langTag) {
		Query query;
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add("application", ResourceFactory.createStringLiteral(application));
		bindings.add("key", ResourceFactory.createStringLiteral(key));
		if (StringUtils.isBlank(theme)) {
			query = QueryFactory.create(queryNoTheme(langTag));
		} else {
			query = QueryFactory.create(queryWithTheme(langTag));
			bindings.add("theme", ResourceFactory.createStringLiteral(theme));
		}
		QueryExecution qexec = QueryExecutionFactory.create(query, memModel, bindings);
		return qexec;
	}

	private String createUUID() {
		return "urn:uuid:" + UUID.randomUUID();
	}

	private String getBodyTemplate(String theme) {
        if (StringUtils.isBlank(theme)) {
        	return TEMPLATE_BODY + TEMPLATE_LABEL;
        }		
        return TEMPLATE_BODY + TEMPLATE_LABEL + TEMPLATE_THEME;
	}

	private String getLanguage(File file) {
		String name = file.getName();
		if (!name.contains("_")) {
			return "en_US";	
		}
		int startIndex;
		if (name.contains("_all")) {
			startIndex = name.indexOf("_all_") + 5;
		} else {
			startIndex = name.indexOf("_") + 1;
		}
		int endIndex = name.length() - SUFFIX_LENGTH;
		
		return name.substring(startIndex,endIndex);
	}

	private String getApplication(File file) {
		String name = file.getName();
		if (name.toLowerCase().contains("vivo")) {
			return "VIVO";
		}
		return "Vitro";
	}

	private String getTheme(File file) {
		File parent = file.getParentFile();
		if (parent == null) {
			return "";
		}
		if (THEMES.equals(parent.getName())) {
			return file.getName();
		}
		return getTheme(parent);
	}

	private ChangeSet makeChangeSet(RDFService rdfService) {
		ChangeSet cs = rdfService.manufactureChangeSet();
		cs.addPreChangeEvent(new BulkUpdateEvent(null, BEGIN));
		cs.addPostChangeEvent(new BulkUpdateEvent(null, END));
		return cs;
	}
	
}
