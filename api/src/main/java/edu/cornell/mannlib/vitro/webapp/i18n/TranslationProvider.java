package edu.cornell.mannlib.vitro.webapp.i18n;

import java.text.MessageFormat;
import java.util.List;
import javax.servlet.ServletContext;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

public class TranslationProvider {

	private static final String MESSAGE_KEY_NOT_FOUND = "Translation not found for key ''{0}''";
	private static final TranslationProvider INSTANCE = new TranslationProvider();
	private static String application = "Vitro";
	private static final String QUERY = ""
	+ "PREFIX : <http://vivoweb.org/ontology/core/properties/vocabulary#>\n"
	+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
	+ "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n"
	+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
	+ "SELECT ?translation\n"
	+ "WHERE {\n"
	+ "  GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata> {\n"
	+ "    OPTIONAL{\n"
	+ "	  ?portal vitro:themeDir ?themePath .\n"
	+ "      BIND(SUBSTR(?themePath, 8, STRLEN(?themePath) - 1) as ?current_theme) .\n"
	+ "    }\n"
	+ "  }\n"
	+ "  GRAPH <http://vitro.mannlib.cornell.edu/default/interface-i18n> {\n"
	+ "	  ?uri :hasKey ?key .\n"
	+ "	  ?uri rdfs:label ?translationWithLocale .\n"
	+ "	  OPTIONAL { \n"
	+ "		?uri :hasTheme ?found_theme .\n"
	+ "	  }\n"
	+ "	  OPTIONAL { \n"
	+ "		?uri :hasApp ?found_application .\n"
	+ "	  }\n"
	+ "	  BIND(COALESCE(?found_theme, \"none\") as ?theme ) .\n"
	+ "	  BIND(COALESCE(?found_application, \"none\") as ?application ) .\n"
	+ "	  BIND(IF(?current_theme = lcase(str(?theme)), 50, 0) AS ?priority1 ) .\n"
	+ "	  BIND(IF(?current_theme = \"none\", xsd:integer(?priority1)+10, xsd:integer(?priority1)) AS ?priority2 ) .\n"
	+ "	  BIND(IF(?current_application = lcase(str(?application)), xsd:integer(?priority2)+5, xsd:integer(?priority2)) AS ?priority3 ) .\n"
	+ "	  BIND (STR(?translationWithLocale) AS ?translation) .\n"
	+ "	  FILTER ( lang(?translationWithLocale) = ?locale ) .\n"
	+ "   }\n"
	+ "} \n"
	+ "ORDER by ASC(?priority3)" ;

	private RDFService rdfService;
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
		QueryHolder queryHolder = new QueryHolder(QUERY)
				.bindToPlainLiteral("current_application", application)
				.bindToPlainLiteral(key, key);
		LanguageFilteringRDFService lfrs = new LanguageFilteringRDFService(rdfService, preferredLocales);
		List<String> list = createSelectQueryContext(lfrs, queryHolder).execute().toStringFields().flatten();
		if (list.isEmpty()) {
			return notFound(key);	
		}
		return list.get(0);
	}

	private String notFound(String key) {
		return MessageFormat.format(MESSAGE_KEY_NOT_FOUND, key);
	}

}
