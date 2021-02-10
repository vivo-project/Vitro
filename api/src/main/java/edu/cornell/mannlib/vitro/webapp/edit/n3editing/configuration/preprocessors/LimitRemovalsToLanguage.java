package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;

/**
 * A model change preprocessor that discards triples with language tags
 * in the retractionsModel that do not match the specified language, unless
 * the additionsModel also contains a new value for the same subject and 
 * predicate in that language, or no values in any language are added for the 
 * same subject and predicate (such as when an entire resource is deleted) .
 */
public class LimitRemovalsToLanguage implements ModelChangePreprocessor {

    private static final Log log = LogFactory.getLog(LimitRemovalsToLanguage.class);
    private String language;
    
    /**
     * @param locale the Java locale object representing the language
     * to which edits should be limited.  May not be null.
     */
    public LimitRemovalsToLanguage(Locale locale) {
        if(locale == null) {
            throw new IllegalArgumentException("Locale may not be null.");    
        }            
        this.language = LanguageFilteringUtils.localeToLanguage(locale);
    }
    
    /**
     * @param language string representing the RDF language tag to which
     * edits should be limited. May not be null.
     */
    public LimitRemovalsToLanguage(String language) {
        if(language == null) {
            throw new IllegalArgumentException("Language may not be null.");    
        }
        this.language = language;
    }
    
    @Override
    public void preprocess(Model retractionsModel, Model additionsModel,
            HttpServletRequest request) {                 
        log.debug("limiting changes to " + language);
        List<Statement> eliminatedRetractions = new ArrayList<Statement>();
        StmtIterator sit = retractionsModel.listStatements();
        while(sit.hasNext()) {
            Statement stmt = sit.next();
            if(stmt.getObject().isLiteral()) {
                Literal lit = stmt.getObject().asLiteral();
                if(!StringUtils.isEmpty(lit.getLanguage()) 
                        && !lit.getLanguage().equals(language)) {
                    boolean eliminateRetraction = true;
                    StmtIterator replacements = additionsModel
                            .listStatements(stmt.getSubject(), 
                                    stmt.getPredicate(), (RDFNode) null);
                    if(!replacements.hasNext()) {
                        eliminateRetraction = false;
                    } else {
                        while(replacements.hasNext()) {
                            Statement replacement = replacements.next();
                            if(replacement.getObject().isLiteral() 
                                    && lit.getLanguage().equals(replacement
                                            .getObject().asLiteral()
                                            .getLanguage())) {
                                eliminateRetraction = false;
                            }
                        }
                    }
                    if(eliminateRetraction) {
                        eliminatedRetractions.add(stmt);
                    }
                }
            }
        }
        retractionsModel.remove(eliminatedRetractions);
    }

}
