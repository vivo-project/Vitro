/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import static edu.cornell.mannlib.vitro.webapp.i18n.selection.LocaleSelectionSetup.PROPERTY_SELECTABLE_LOCALES;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationReader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * A variation on SelectQueryDocumentModifier where the suffix of target field is defined.
 * Multiple queries are performed for each of locales configured in runtime.properties
 * 
 * Target field names are composed of locale + fieldSuffix.
 * 
 * Each query should contain a ?uri variable, which will be replaced by the URI
 * of the individual.
 * 
 * All of the other result fields in each row of each query will be converted to
 * strings and added to the field.
 *
 */
public class SelectQueryI18nDocumentModifier extends SelectQueryDocumentModifier
		implements DocumentModifier, ContextModelsUser, ConfigurationReader {
	private static final Log log = LogFactory.getLog(SelectQueryI18nDocumentModifier.class);

	private String fieldSuffix = "";

	private ArrayList<String> locales = new ArrayList<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTargetSuffix")
	public void setTargetSuffix(String fieldSuffix) {
		this.fieldSuffix = fieldSuffix;
	}

	@Override
	public void modifyDocument(Individual ind, SearchInputDocument doc) {
		if (passesTypeRestrictions(ind) && StringUtils.isNotBlank(fieldSuffix)) {
			List<Map<String, List<String>>> maps = getTextForQueries(ind);
			for (Map<String, List<String>> map : maps) {
				for (String locale : map.keySet()) {
					List<String> values = map.get(locale);
					String fieldName = locale + fieldSuffix; 
					doc.addField(fieldName, values);
				}
			}
		}
	}

	protected List<Map<String, List<String>>> getTextForQueries(Individual ind) {
		List<Map<String, List<String>>> list = new ArrayList<>();
		for (String query : queries) {
			list.add(getTextForQuery(query, ind));
		}
		return list;
	}

	protected Map<String, List<String>> getTextForQuery(String query, Individual ind) {
		try {
			QueryHolder queryHolder = new QueryHolder(query).bindToUri("uri", ind.getURI());
			Map<String, List<String>> mapLocaleToFields = new HashMap<>();
			for (String locale : locales) {
				LanguageFilteringRDFService lfrs = new LanguageFilteringRDFService(rdfService,
						Collections.singletonList(locale));
				List<String> list = createSelectQueryContext(lfrs, queryHolder)
				        .execute()
				        .toStringFields(varNames.toArray(new String[varNames.size()]))
				        .flatten();
				mapLocaleToFields.put(locale, list);
				log.debug(label + " for locale " + locale + " - query: '" + query + "' returns " + list);
			}
			return mapLocaleToFields;
		} catch (Throwable t) {
			log.error("problem while running query '" + query + "'", t);
			return Collections.emptyMap();
		}
	}

	@Override
	public void setConfigurationProperties(ConfigurationProperties config) {
		String property = config.getProperty(PROPERTY_SELECTABLE_LOCALES);
		if (!StringUtils.isBlank(property)) {
			String[] values = property.trim().split("\\s*,\\s*");
			for (String value : values) {
				String locale = value.replace("_", "-");
				addLocale(locale);
			}
		}
	}

	private void addLocale(String localeString) {
		if (StringUtils.isBlank(localeString)) {
			return;	
		}
		locales.add(localeString);
	}

}
