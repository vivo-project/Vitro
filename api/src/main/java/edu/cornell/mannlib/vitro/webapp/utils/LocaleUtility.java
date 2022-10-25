package edu.cornell.mannlib.vitro.webapp.utils;

import org.apache.commons.lang3.LocaleUtils;

import java.util.Locale;

public final class LocaleUtility {

    private LocaleUtility(){}

    public static Locale languageStringToLocale(String localeString){
        String[] parsedLoc = localeString.trim().split("_", -1);
        //regex pattern for locale tag with script specified
        Locale locale = localeString.matches("^[a-z]{1,3}_[A-Z][a-z]{3}_[A-Z]{2}") ?
            new Locale.Builder().setLanguage(parsedLoc[0]).setRegion(parsedLoc[2]).setScript(parsedLoc[1]).build() :
            LocaleUtils.toLocale(localeString);
        return locale;
    }
}
