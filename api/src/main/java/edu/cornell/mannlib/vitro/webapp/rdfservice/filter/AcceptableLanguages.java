package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A normalized list of languages/locales acceptable in results
 * returned by language-filtering RDFServices, graphs, models, etc.
 */
public class AcceptableLanguages extends ArrayList<String>{
    
    private static final String SEPARATOR = "-";
    private static final String PRIVATE_USE_SUBTAG = "x";
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AcceptableLanguages.class);
    
    /**
     * Construct a normalized list of acceptable language strings
     * from a set of raw language strings.  For any values of form 'aa-BB',
     * the base language ('aa') will be also added to the list. 
     * @param rawLanguageStrs may not be null
     */
    public AcceptableLanguages(List<String> rawLanguageStrs) {
        log.debug("Raw language strings:" + rawLanguageStrs);
        for (String lang : rawLanguageStrs) {
            this.add(lang);
            String[] subtags = lang.split(SEPARATOR);
            int length = subtags.length;
            for (int i = 1; i < length; i++) {
                int lastIndex = length - i;
                if (PRIVATE_USE_SUBTAG.equals(subtags[lastIndex - 1])) {
                    continue;
                }
                String baseLang = String.join(SEPARATOR, Arrays.copyOfRange(subtags, 0, lastIndex));
                if (!lang.equals(baseLang) && !this.contains(baseLang)) {
                    this.add(baseLang);
                }
            }
        }
        log.debug("Normalized language strings:" + this);
    }

}
