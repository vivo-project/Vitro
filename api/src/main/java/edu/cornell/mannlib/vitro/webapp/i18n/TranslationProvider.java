package edu.cornell.mannlib.vitro.webapp.i18n;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

public class TranslationProvider {

	private static final String MESSAGE_KEY_NOT_FOUND = "ERROR: Translation not found ''{0}''";
	private static final TranslationProvider INSTANCE = new TranslationProvider();
	private static final Log log = LogFactory.getLog(TranslationProvider.class);
	private static final I18nLogger i18nLogger = new I18nLogger();
	private static final String QUERY = "" 
	+ "PREFIX : <http://vivoweb.org/ontology/core/properties/vocabulary#>\n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" 
	+ "SELECT ?translation \n" + "WHERE {\n"
	+ "  GRAPH <http://vitro.mannlib.cornell.edu/default/interface-i18n> {\n" 
	+ "	  ?uri :hasKey ?key .\n"
	+ "	  ?uri rdfs:label ?translation .\n" 
	+ "	  OPTIONAL { \n"
	+ "		?uri :hasTheme ?found_theme .\n"
	+ "	  }\n" 
	+ "	  OPTIONAL { \n"
	+ "		?uri :hasApp ?found_application .\n" 
	+ "   }\n"
	+ "	  BIND(COALESCE(?found_theme, \"none\") as ?theme ) .\n"
	+ "	  FILTER(?theme = \"none\" || ?theme = ?current_theme) . "
	+ "	  BIND(COALESCE(?found_application, \"none\") as ?application ) .\n"
	+ "	  BIND(IF(?current_application = ?application && ?current_theme = ?theme, 3, "
	+ "		   	IF(?current_theme = ?theme, 2, "
	+ "				IF(?current_application = ?application, 1, 0)) ) AS ?order ) .\n"
	+ "   }\n" + "} \n"
	+ "ORDER by DESC(?order)";

	protected RDFService rdfService;
	protected String application = "Vitro";
	private Map<TranslationKey, String> cache = new ConcurrentHashMap<>();
	private String theme = "vitro";
	private int prefixLen = "themes/".length();
	private int suffixLen = "/".length();
	private WebappDaoFactory wdf; 

	public static TranslationProvider getInstance() {
		return INSTANCE;
	}

	public void initialize(ServletContext ctx) {
		RevisionInfoBean info = (RevisionInfoBean) ctx.getAttribute(RevisionInfoBean.ATTRIBUTE_NAME);
		List<LevelRevisionInfo> levelInfos = info.getLevelInfos();
		setApplication(levelInfos);
		rdfService = ModelAccess.on(ctx).getRDFService(CONFIGURATION);
		wdf = ModelAccess.on(ctx).getWebappDaoFactory();
		updateTheme();
	}

	private void updateTheme() {
		final String themeDir = wdf.getApplicationDao().getApplicationBean().getThemeDir();
		final int length = themeDir.length();
		theme = themeDir.substring(prefixLen, length - suffixLen);
	}
	
	public void setTheme(String theme) {
		this.theme = theme;
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
		String formattedText = formatString(text, parameters);
		i18nLogger.log(key, parameters, text, formattedText);
		if (needExportInfo()) {
			return prepareExportInfo(key, parameters, text, formattedText);
		} else {
			cache.put(tk, formattedText);
			log.debug("Added to cache " + key);
			log.debug("Returned value from request for " + key);
			return formattedText;
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
				.bindToPlainLiteral("current_theme", theme)
				.bindToPlainLiteral("locale", preferredLocales.get(0));
		
		LanguageFilteringRDFService lfrs = new LanguageFilteringRDFService(rdfService, preferredLocales);
		List<String> list = new LinkedList<>();
		try {
			lfrs.sparqlSelectQuery(queryHolder.getQueryString(), new ResultSetConsumer() {
			    @Override
			    protected void processQuerySolution(QuerySolution qs) {
			        Literal translation = qs.getLiteral("translation");
			        if (translation != null) {
			        	list.add(translation.getLexicalForm());
			        }
			    }
			});
		} catch (RDFServiceException e) {
			log.error(e,e);
		}
		
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
			return MessageFormat.format(TranslationProvider.preprocessForFormating(textString), parameters);
		}
	}

	/**
	 * This method should prepare the inputText for MessageFormat.format method. At the moment it is replacing single
	 * apostrophe with double, it might be extented in the future with some additional preprocessing.
	 * @param inputText - string which should be preprocessed
	 * @return preprocessed input string, i.e. string with replaced single apostrophe with double
	 */
	public static String preprocessForFormating(String inputText){
		if (inputText != null) {
			return inputText.replace("''", "'").replace("'", "''");
		} else {
			return "";
		}
	}

	private String notFound(String key) {
		return MessageFormat.format(MESSAGE_KEY_NOT_FOUND, key);
	}

	public void clearCache() {
		if (wdf != null) {
			updateTheme();
		}
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
