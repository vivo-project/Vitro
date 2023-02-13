package edu.cornell.mannlib.vitro.webapp.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class I18nSemanticBundle implements I18nBundle {
	
	private List<String> preferredLocales = Collections.emptyList();
	
	public I18nSemanticBundle(List<Locale> preferredLocales){
		this.preferredLocales  = convertToStrings(preferredLocales);
	}

	private static List<String> convertToStrings(List<Locale> preferredLocales) {
		return preferredLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toList());
	}
	
	@Override
	public String text(String key, Object... parameters) {
		final TranslationProvider provider = TranslationProvider.getInstance();
		return provider.getTranslation(preferredLocales, key, parameters);
	}

	

}
