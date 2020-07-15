package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * A filter of literal statements from Models according to language preferences. 
 */
public class LanguageFilterModel {

    private static final Log log = LogFactory.getLog(LanguageFilterModel.class);
    
    /**
     * 
     * @param m the model to filter. May not be null.
     * @param langs list of strings of type 'en-US'. May not be null.
     * @return model with language-inappropriate literal statements filtered out.
     */
    public Model filterModel(Model m, List<String> langs) {
        log.debug("filterModel");
        List<Statement> retractions = new ArrayList<Statement>();
        StmtIterator stmtIt = m.listStatements();
        while (stmtIt.hasNext()) {
            Statement stmt = stmtIt.nextStatement();
            if (stmt.getObject().isLiteral()) {
                List<Statement> candidatesForRemoval = m.listStatements(
                        stmt.getSubject(), stmt.getPredicate(), (RDFNode) null).toList();
                if (candidatesForRemoval.size() == 1) {
                    continue;
                }
                candidatesForRemoval.sort(new StatementSortByLang(langs));
                log.debug("sorted statements: " + showSortedStatements(candidatesForRemoval));
                Iterator<Statement> candIt = candidatesForRemoval.iterator();
                String langRegister = null;
                boolean chuckRemaining = false;
                while(candIt.hasNext()) {
                    Statement s = candIt.next();
                    if (!s.getObject().isLiteral()) {
                        continue;
                    } else if (chuckRemaining) {
                        retractions.add(s);
                    }
                    String lang = s.getObject().asLiteral().getLanguage();
                    if (langRegister == null) {
                        langRegister = lang;
                    } else if (!langRegister.equals(lang)) {
                        chuckRemaining = true;
                        retractions.add(s);
                    }
                }
            }

        }
        m.remove(retractions);
        return m;
    }
    
    private String showSortedStatements(List<Statement> candidatesForRemoval) {
        List<String> langStrings = new ArrayList<String>();
        for (Statement stmt: candidatesForRemoval) {
            if (stmt == null) {
                langStrings.add("null stmt");
            } else {
                RDFNode node = stmt.getObject();
                if (!node.isLiteral()) {
                    langStrings.add("not literal");
                } else {
                    langStrings.add(node.asLiteral().getLanguage());
                }
            }
        }
        return langStrings.toString();
    }
    
    private class StatementSortByLang extends LangSort implements Comparator<Statement> {

        public StatementSortByLang(List<String> langs) {
            super(langs);
        }
        
        public int compare(Statement s1, Statement s2) {
            if (s1 == null || s2 == null) {
                return 0;
            } else if (!s1.getObject().isLiteral() || !s2.getObject().isLiteral()) {
                return 0;
            }

            String s1lang = s1.getObject().asLiteral().getLanguage();
            String s2lang = s2.getObject().asLiteral().getLanguage();

            return compareLangs(s1lang, s2lang);
        }
    }
    
}
