package edu.cornell.mannlib.vitro.webapp.i18n;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

public class TranslationProvider {

	private static final String MESSAGE_KEY_NOT_FOUND = "ERROR: Translation not found ''{0}''";
	private static final TranslationProvider INSTANCE = new TranslationProvider();
	private static final Log log = LogFactory.getLog(TranslationProvider.class);
	private static String application = "Vitro";
	private static final String QUERY = "" 
	+ "PREFIX : <http://vivoweb.org/ontology/core/properties/vocabulary#>\n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + "SELECT ?translation\n" + "WHERE {\n"
	+ "  GRAPH <http://vitro.mannlib.cornell.edu/default/interface-i18n> {\n" + "	  ?uri :hasKey ?key .\n"
	+ "	  ?uri rdfs:label ?translationWithLocale .\n" + "	  OPTIONAL { \n"
	+ "		?uri :hasTheme ?found_theme .\n" + "	  }\n" + "	  OPTIONAL { \n"
	+ "		?uri :hasApp ?found_application .\n" + "   }\n"
	+ "   GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata> {\n"
	+ "     OPTIONAL{\n" + "	   ?portal vitro:themeDir ?themePath .\n"
	+ "       BIND(SUBSTR(?themePath, 8, STRLEN(?themePath) - 1) as ?current_theme) .\n" + "     }\n"
	+ "	  }\n" + "	  BIND(COALESCE(?found_theme, \"none\") as ?theme ) .\n"
	+ "	  BIND(COALESCE(?found_application, \"none\") as ?application ) .\n"
	+ "	  BIND(IF(?current_theme = lcase(str(?theme)), 50, 0) AS ?priority1 ) .\n"
	+ "	  BIND(IF(?current_theme = \"none\", xsd:integer(?priority1)+10, xsd:integer(?priority1)) AS ?priority2 ) .\n"
	+ "	  BIND(IF(?current_application = lcase(str(?application)), xsd:integer(?priority2)+5, xsd:integer(?priority2)) AS ?priority3 ) .\n"
	+ "	  BIND (STR(?translationWithLocale) AS ?translation) .\n"
	+ "	  FILTER ( lang(?translationWithLocale) = ?locale ) .\n" + "   }\n" + "} \n"
	+ "ORDER by ASC(?priority3)";

	private RDFService rdfService;
	private Map<TranslationKey, String> cache = new ConcurrentHashMap<>(); 

	public static TranslationProvider getInstance() {
		return INSTANCE;
	}

	public void initialize(ServletContext ctx) {
		RevisionInfoBean info = (RevisionInfoBean) ctx.getAttribute(RevisionInfoBean.ATTRIBUTE_NAME);
		List<LevelRevisionInfo> levelInfos = info.getLevelInfos();
		setApplication(levelInfos);
		rdfService = ModelAccess.on(ctx).getRDFService();
	}

	private void setApplication(List<LevelRevisionInfo> levelInfos) {
		if (levelInfos.isEmpty()) {
			return;
		}
		application = levelInfos.get(0).getName();
	}

	public String getTranslation(List<String> preferredLocales, String key, Object[] parameters) {
		TranslationKey tk = new TranslationKey(preferredLocales, key, parameters);
		if (cache.containsKey(tk) && !needExportInfo()) {
			log.debug("Returned value from cache for " + key);
			return cache.get(tk);
		}
		String text = getText(preferredLocales, key);
		String message = formatString(text, parameters);
		
		if (needExportInfo()) {
			return prepareExportInfo(key, parameters, text, message);
		} else {
			cache.put(tk, message);
			log.debug("Added to cache " + key);
			log.debug("Returned value from request for " + key);
			return message;
		}
	}

	private String prepareExportInfo(String key, Object[] parameters, String text, String message) {
		String separatedArgs = "";
		for (int i = 0; i < parameters.length; i++) {
			separatedArgs += parameters[i] + I18nBundle.INT_SEP;
		}
		log.debug("Returned value with export info for " + key );
		return I18nBundle.START_SEP + key + I18nBundle.INT_SEP + text + I18nBundle.INT_SEP + separatedArgs
				+ message + I18nBundle.END_SEP;
	}

	private String getText(List<String> preferredLocales, String key) {
		String textString;
		QueryHolder queryHolder = new QueryHolder(QUERY)
				.bindToPlainLiteral("current_application", application)
				.bindToPlainLiteral("key", key)
				.bindToPlainLiteral("locale", preferredLocales.get(0));
		LanguageFilteringRDFService lfrs = new LanguageFilteringRDFService(rdfService, preferredLocales);
		List<String> list = createSelectQueryContext(lfrs, queryHolder).execute().toStringFields().flatten();
		if (list.isEmpty()) {
			textString = notFound(key);
		} else {
			textString = list.get(0);
		}
		return textString;
	}

	private static boolean needExportInfo() {
		return DeveloperSettings.getInstance().getBoolean(Key.I18N_ONLINE_TRANSLATION);
	}

	private static String formatString(String textString, Object... parameters) {
		if (parameters.length == 0) {
			return textString;
		} else {
			return MessageFormat.format(textString, parameters);
		}
	}

	private String notFound(String key) {
		return MessageFormat.format(MESSAGE_KEY_NOT_FOUND, key);
	}

	public void clearCache() {
		cache.clear();
		log.info("Translation cache cleared");
	}
	
	private class TranslationKey {

		private List<String> preferredLocales;
		private String key;
		private Object[] parameters;
		
		public TranslationKey(List<String> preferredLocales, String key, Object[] parameters) {
			this.preferredLocales = preferredLocales;
			this.key = key;
			this.parameters = parameters;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (! (obj instanceof TranslationKey)) {
				return false;
			}
			TranslationKey other = (TranslationKey) obj;
	        return new EqualsBuilder()
                .append(preferredLocales, other.preferredLocales)
                .append(key, other.key)
                .append(parameters, other.parameters)
                .isEquals();
		}
		
		@Override
		public int hashCode(){
		    return new HashCodeBuilder()
		        .append(preferredLocales)
		        .append(key)
		        .append(parameters)
		        .toHashCode();
		}
		
	}

}
