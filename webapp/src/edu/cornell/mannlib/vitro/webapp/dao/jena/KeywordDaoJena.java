package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class KeywordDaoJena extends JenaBaseDao implements KeywordDao {
    private static final Log log = LogFactory.getLog(KeywordDaoJena.class.getName());
    
    private String KEYWORD_URI_PREFIX = DEFAULT_NAMESPACE + "keyword";

    private WebappDaoFactory webappDaoFactory = null;

    public KeywordDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
        this.webappDaoFactory = getWebappDaoFactory();
    }

    public void deleteKeyword(Keyword keyword) {
        webappDaoFactory.getIndividualDao().deleteIndividual(KEYWORD_URI_PREFIX+keyword.getId());
    }

    public List<Keyword> getAllKeywords() {
        VClass keywordClass = webappDaoFactory.getVClassDao().getVClassByURI(KEYWORD.getURI());
        List keywordInds = webappDaoFactory.getIndividualDao().getIndividualsByVClass(keywordClass);
        List keywords = new ArrayList();
        Iterator keywordIndIt = keywordInds.iterator();
        while (keywordIndIt.hasNext()) {
            keywords.add(keywordFromKeywordIndividual((Individual)keywordIndIt.next()));
        }
        return keywords;
    }

    public List<Keyword> getKeywordsByStem(String stem) {
        List<Keyword> matchingKeywords = new ArrayList<Keyword>();
        List<Keyword> all = getAllKeywords();
        Iterator<Keyword> allIt = all.iterator();
        while (allIt.hasNext()) {
            Keyword k = allIt.next();
            if (k.getStem()!= null && k.getStem().equalsIgnoreCase(stem)) {
                matchingKeywords.add(k);
            }
        }
        return matchingKeywords;
    }

    public List getAllOrigins() {
        List origins = new ArrayList();
        HashSet originSet = new HashSet();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator stmtIt = getOntModel().listStatements(null, KEYWORD_ORIGIN, (Literal)null);
            try {
                while (stmtIt.hasNext()) {
                    Statement stmt = (Statement) stmtIt.next();
                    Literal lit = (Literal) stmt.getObject();
                    String originStr = (String)lit.getString();
                    if (!originSet.contains(originStr)) {
                        originSet.add(originStr);
                    }
                }
            } finally {
                stmtIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        origins.addAll(originSet);
        return origins;
    }

    public Keyword getKeywordById(int id) {
        Individual keywordIndividual = webappDaoFactory.getIndividualDao().getIndividualByURI(KEYWORD_URI_PREFIX+id);
        Keyword k = keywordFromKeywordIndividual(keywordIndividual);
        k.setId(id);
        return k;
    }

    private Keyword keywordFromKeywordIndividual(Individual keywordIndividual) {
           Keyword keyword = new Keyword();
           if (keywordIndividual != null) {
                keyword.setTerm(keywordIndividual.getName());
                keyword.setId(Integer.decode(keywordIndividual.getLocalName().substring(7,keywordIndividual.getLocalName().length())));
                try {
                    keyword.setStem( ((DataPropertyStatement)(webappDaoFactory.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(keywordIndividual, KEYWORD_STEM.getURI()).toArray())[0]).getData() );
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                try {
                    keyword.setOrigin( ((DataPropertyStatement)(webappDaoFactory.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(keywordIndividual, KEYWORD_ORIGIN.getURI()).toArray())[0]).getData() );
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                try {
                    keyword.setType( ((DataPropertyStatement)(webappDaoFactory.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(keywordIndividual, KEYWORD_TYPE.getURI()).toArray())[0]).getData() );
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                try {
                    keyword.setSource( ((DataPropertyStatement)(webappDaoFactory.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(keywordIndividual, KEYWORD_SOURCE.getURI()).toArray())[0]).getData() );
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                try {
                    keyword.setComments( ((DataPropertyStatement)(webappDaoFactory.getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(keywordIndividual, KEYWORD_COMMENTS.getURI()).toArray())[0]).getData() );
                } catch (Exception e) {
                    // e.printStackTrace();
                }
           }
            return keyword;
    }

    public synchronized int insertNewKeyword(Keyword k) {
        int kId = -1;
        if (k.getId()>0) {
            kId = k.getId();
        } else {
            String term = k.getTerm();
            int count = 0;
            while ((webappDaoFactory.getIndividualDao().getIndividualByURI(KEYWORD_URI_PREFIX+Math.abs(term.hashCode())) != null) && count < 32) {
                term+="a";
                log.debug(term);
                log.debug(KEYWORD_URI_PREFIX+Math.abs(term.hashCode()));
                count++;
            }

            kId = Math.abs(term.hashCode());
        }
        Individual keywordIndividual = new IndividualImpl();
        keywordIndividual.setURI(KEYWORD_URI_PREFIX+kId);
        keywordIndividual.setVClassURI(KEYWORD.getURI());
        keywordIndividual.setName(k.getTerm());
        String keywordURI = null;
        try {
        	keywordURI = webappDaoFactory.getIndividualDao().insertNewIndividual(keywordIndividual);
        } catch (InsertException e) {
        	log.error(e);
        }
      log.debug("KeywordDaoJena.insertNewKeyword() : "+keywordURI);
        
        DataPropertyStatement stem = new DataPropertyStatementImpl();
      log.debug("KeywordDaoJena.insertNewKeyword() stem : "+k.getStem());
        stem.setData(k.getStem());
        stem.setDatapropURI(KEYWORD_STEM.getURI());
        stem.setIndividualURI(keywordURI);
        webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(stem);
        
      log.debug("KeywordDaoJena.insertNewKeyword() type : "+k.getType());
        DataPropertyStatement type = new DataPropertyStatementImpl();
        type.setData(k.getType());
        type.setDatapropURI(KEYWORD_TYPE.getURI());
        type.setIndividualURI(keywordURI);
        webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(type);
        
      log.debug("KeywordDaoJena.insertNewKeyword() source : "+k.getSource());
        if (k.getSource()==null || k.getSource().equals("")) {
            k.setSource("unspecified");
        }
        DataPropertyStatement source = new DataPropertyStatementImpl();
        source.setData(k.getSource());
        source.setDatapropURI(KEYWORD_SOURCE.getURI());
        source.setIndividualURI(keywordURI);
        webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(source);
        
      log.debug("KeywordDaoJena.insertNewKeyword() origin : "+k.getOrigin());
        if (k.getOrigin()==null || k.getOrigin().equals("")) {
            k.setOrigin("unspecified");
        }
        DataPropertyStatement origin = new DataPropertyStatementImpl();
        origin.setData(k.getOrigin());
        origin.setDatapropURI(KEYWORD_ORIGIN.getURI());
        origin.setIndividualURI(keywordURI);
        webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(origin);
        
        if (k.getComments()!=null && !k.getComments().equals("")) {
          log.debug("KeywordDaoJena.insertNewKeyword() comments : "+k.getComments());
            DataPropertyStatement comments = new DataPropertyStatementImpl();
            comments.setData(k.getComments());
            comments.setDatapropURI(KEYWORD_COMMENTS.getURI());
            comments.setIndividualURI(keywordURI);
            webappDaoFactory.getDataPropertyStatementDao().insertNewDataPropertyStatement(comments);
        }
        return kId;
    }

    public void updateKeyword(Keyword k) {
        deleteKeyword(k);
        insertNewKeyword(k);
    }

}
