/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class KeywordIndividualRelationDaoJena extends JenaBaseDao implements KeywordIndividualRelationDao {

    public KeywordIndividualRelationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getABoxModel();
    }
    
    public void deleteKeywordIndividualRelation(KeywordIndividualRelation k) {
    	deleteKeywordIndividualRelation(k,getOntModel());
    }

    public void deleteKeywordIndividualRelation(KeywordIndividualRelation k, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Individual kirInd = ontModel.getIndividual(k.getURI());
            if (kirInd != null) {
                kirInd.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        if (k.getEntURI() != null) {
            Individual ind = getWebappDaoFactory().getIndividualDao().getIndividualByURI(k.getEntURI());
            getWebappDaoFactory().getIndividualDao().markModified(ind);
        }
    }

    public KeywordIndividualRelation getKeywordIndividualRelationByURI(String URI) {
        com.hp.hpl.jena.ontology.Individual kirInd = getOntModel().getIndividual(URI);
        if (kirInd != null) {
            return keywordIndividualRelationFromIndividual(kirInd);
        } else {
            return null;
        }
    }

       public List<KeywordIndividualRelation> getKeywordIndividualRelationsByIndividualURI(String individualURI) {
            List kirList = new ArrayList();
            if (individualURI == null) {
            	return kirList;
            }
            getOntModel().enterCriticalSection(Lock.READ);
            try {
                com.hp.hpl.jena.ontology.Individual individual = getOntModel().getIndividual(individualURI);
                if (individual != null) {
                    ClosableIterator stmtIt = getOntModel().listStatements(null, KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
                    try {
                        while (stmtIt.hasNext()) {
                            Statement stmt = (Statement) stmtIt.next();
                            Resource kirRes = stmt.getSubject();
                            if (kirRes != null) {
                                boolean checked = false;
                                ClosableIterator checkIt = kirRes.listProperties(RDF.type);
                                try {
                                    while (checkIt.hasNext()) {
                                        Statement chk = (Statement) checkIt.next();
                                        if (((Resource)chk.getObject()).getURI().equals(KEYWORD_INDIVIDUALRELATION.getURI())) {
                                            checked = true;
                                            break;
                                        }
                                    }
                                } finally {
                                    checkIt.close();
                                }
                                if (checked) {
                                    com.hp.hpl.jena.ontology.Individual kirInd = getOntModel().getIndividual(kirRes.getURI());
                                    kirList.add(keywordIndividualRelationFromIndividual(kirInd));
                                }
                            }
                        }
                    } finally {
                        stmtIt.close();
                    }
                }
            } finally {
                getOntModel().leaveCriticalSection();
            }
            return kirList;
        }

    public String insertNewKeywordIndividualRelation(KeywordIndividualRelation k) {
    	return insertNewKeywordIndividualRelation(k,getOntModel());
    }

    public String insertNewKeywordIndividualRelation(KeywordIndividualRelation k, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        String uriToUse = null;
        try {
            Resource individual = ontModel.getResource(k.getEntURI());
            Resource keyword = ontModel.getResource(DEFAULT_NAMESPACE+"keyword"+k.getKeyId());
            if (k.getURI() != null) {
                uriToUse = k.getURI();
            } else {
                String dedup = DEFAULT_NAMESPACE+"keywordIndividualRelation_"+k.getKeyId()+"_"+individual.getLocalName();
                //while (ontModel.getResource(dedup) != null) {
                //  dedup += "a";
                //}
                uriToUse = dedup;
            }
            OntClass KeywordIndividualRelationOntClass = ontModel.getOntClass(VitroVocabulary.KEYWORD_INDIVIDUALRELATION);
            if (KeywordIndividualRelationOntClass != null) {
                com.hp.hpl.jena.ontology.Individual tirInd = KeywordIndividualRelationOntClass.createIndividual(uriToUse);
                addPropertyResourceValue(tirInd, KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD, keyword);
                addPropertyResourceValue(tirInd, KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
                addPropertyStringValue(tirInd, KEYWORD_INDIVIDUALRELATION_MODE, k.getMode(), ontModel);
            } else {
                log.error(VitroVocabulary.KEYWORD_INDIVIDUALRELATION+" class not found");
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        if (k.getEntURI() != null) {
            Individual ind = getWebappDaoFactory().getIndividualDao().getIndividualByURI(k.getEntURI());
            getWebappDaoFactory().getIndividualDao().markModified(ind);
        }
        return uriToUse;
    }

    public void updateKeywordIndividualRelation(KeywordIndividualRelation k) {
    	updateKeywordIndividualRelation(k,getOntModel());
    }

    public void updateKeywordIndividualRelation(KeywordIndividualRelation k, OntModel ontModel) {
           ontModel.enterCriticalSection(Lock.WRITE);
            try {
                com.hp.hpl.jena.ontology.Individual kirInd = ontModel.getIndividual(k.getURI());
                Resource individual = ontModel.getResource(k.getEntURI());
                Resource keyword = ontModel.getResource(DEFAULT_NAMESPACE+"keyword"+k.getKeyId());
                updatePropertyResourceValue(kirInd, KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD, keyword);
                updatePropertyResourceValue(kirInd, KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
                updatePropertyStringValue(kirInd, KEYWORD_INDIVIDUALRELATION_MODE, k.getMode(), ontModel);
            } finally {
                ontModel.leaveCriticalSection();
            }
            if (k.getEntURI() != null) {
                Individual ind = getWebappDaoFactory().getIndividualDao().getIndividualByURI(k.getEntURI());
                getWebappDaoFactory().getIndividualDao().markModified(ind);
            }
    }

       private KeywordIndividualRelation keywordIndividualRelationFromIndividual(com.hp.hpl.jena.ontology.Individual ind) {
            KeywordIndividualRelation kir = new KeywordIndividualRelation();
            kir.setURI(ind.getURI());
            try {
                kir.setEntURI(((Resource)ind.getPropertyValue(KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL)).getURI());
            } catch (Exception e) {/* TODO log */}
            try {
                String keywordLocalName = ((Resource)ind.getPropertyValue(KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD)).getLocalName();
                kir.setKeyId(Integer.decode(keywordLocalName.substring(7,keywordLocalName.length())));

            } catch (Exception e) {/* TODO log */}
            kir.setMode(getPropertyStringValue(ind,KEYWORD_INDIVIDUALRELATION_MODE));
            return kir;
        }

}
