/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.TabIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.controller.EntityController;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.TabIndividualRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TabIndividualRelationDaoJena extends JenaBaseDao implements TabIndividualRelationDao {

    private static final Log log = LogFactory.getLog(EntityController.class.getName());

    public TabIndividualRelationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }

    public void deleteTabIndividualRelation(TabIndividualRelation tir) {
    	deleteTabIndividualRelation(tir,getOntModel());
    }

    public void deleteTabIndividualRelation(TabIndividualRelation t2e, OntModel ontModel) {
        com.hp.hpl.jena.ontology.Individual tirInd = ontModel.getIndividual(t2e.getURI());
        if (tirInd != null) {
            tirInd.remove();
        }
    }

    public TabIndividualRelation getTabIndividualRelationByURI(String uri) {
        com.hp.hpl.jena.ontology.Individual tirInd = getOntModel().getIndividual(uri);
        if (tirInd != null) {
            return tabIndividualRelationFromIndividual(tirInd);
        } else {
            return null;
        }
    }

    public List<TabIndividualRelation> getTabIndividualRelationsByIndividualURI(String individualURI) {
        List tirList = new ArrayList();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            com.hp.hpl.jena.ontology.Individual individual = getOntModel().getIndividual(individualURI);
            if (individual != null) {
                ClosableIterator stmtIt = getOntModel().listStatements(null, TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
                try {
                    while (stmtIt.hasNext()) {
                        Statement stmt = (Statement) stmtIt.next();
                        Resource tirRes = stmt.getSubject();
                        if (tirRes != null) {
                            boolean checked = false;
                            ClosableIterator checkIt = tirRes.listProperties(RDF.type);
                            try {
                                while (checkIt.hasNext()) {
                                    Statement chk = (Statement) checkIt.next();
                                    if (((Resource)chk.getObject()).getURI().equals(TAB_INDIVIDUALRELATION.getURI())) {
                                        checked = true;
                                        break;
                                    }
                                }
                            } finally {
                                checkIt.close();
                            }
                            if (checked) {
                                com.hp.hpl.jena.ontology.Individual tirInd = getOntModel().getIndividual(tirRes.getURI());
                                tirList.add(tabIndividualRelationFromIndividual(tirInd));
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
        return tirList;
    }

    public List<TabIndividualRelation> getTabIndividualRelationsByTabURI(String tabURI) {
        List tirList = new ArrayList();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            com.hp.hpl.jena.ontology.Individual tab = getOntModel().getIndividual(tabURI);
            if (tab != null) {
                ClosableIterator stmtIt = getOntModel().listStatements(null, TAB_INDIVIDUALRELATION_INVOLVESTAB, tab);
                try {
                    while (stmtIt.hasNext()) {
                        Statement stmt = (Statement) stmtIt.next();
                        Resource tirRes = stmt.getSubject();
                        if (tirRes != null) {
                            boolean checked = false;
                            ClosableIterator checkIt = tirRes.listProperties(RDF.type);
                            try {
                                while (checkIt.hasNext()) {
                                    Statement chk = (Statement) checkIt.next();
                                    if (((Resource)chk.getObject()).getURI().equals(TAB_INDIVIDUALRELATION.getURI())) {
                                        checked = true;
                                        break;
                                    }
                                }
                            } finally {
                                checkIt.close();
                            }
                            if (checked) {
                                com.hp.hpl.jena.ontology.Individual tirInd = getOntModel().getIndividual(tirRes.getURI());
                                tirList.add(tabIndividualRelationFromIndividual(tirInd));
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
        return tirList;
    }

    public int insertNewTabIndividualRelation(TabIndividualRelation t2e) {
    	insertNewTabIndividualRelation(t2e,getOntModel());
        return 0;
    }

    public void insertNewTabIndividualRelation( TabIndividualRelation tir, OntModel ontModel ) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource individual = ontModel.getResource(tir.getEntURI());
            Resource tab = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+tir.getTabId());
            String uriToUse = null;
            if (tir.getURI() != null) {
                uriToUse = tir.getURI();
            } else {
                String dedup = DEFAULT_NAMESPACE+"tabIndividualRelation_"+tir.getTabId()+"_"+individual.getLocalName();
                //while (ontModel.getResource(dedup) != null) {
                //  dedup += "a";
                //}
                uriToUse = dedup;
            }
            OntClass TabIndividualRelationOntClass = ontModel.getOntClass(VitroVocabulary.TAB_INDIVIDUALRELATION);
            if (TabIndividualRelationOntClass != null) {
                com.hp.hpl.jena.ontology.Individual tirInd = TabIndividualRelationOntClass.createIndividual(uriToUse);
                addPropertyResourceValue(tirInd, TAB_INDIVIDUALRELATION_INVOLVESTAB, tab);
                addPropertyResourceValue(tirInd, TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
                addPropertyNonNegativeIntValue(tirInd, DISPLAY_RANK, tir.getDisplayRank(), ontModel);
            } else {
                log.error(VitroVocabulary.TAB_INDIVIDUALRELATION+" class not found");
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void insertTabIndividualRelation(Tab tab, Individual ent) {
        TabIndividualRelation tir = new TabIndividualRelation();
        tir.setTabId(tab.getTabId());
        tir.setEntURI(ent.getURI());
        insertNewTabIndividualRelation(tir);
    }

    public boolean tabIndividualRelationExists(Tab tab, Individual ent) {
        // TODO Auto-generated method stub
        return false;
    }

    public void updateTabIndividualRelation(TabIndividualRelation t2e) {
    	updateTabIndividualRelation(t2e,getOntModel());
    }

    public void updateTabIndividualRelation(TabIndividualRelation tir, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Individual tirInd = ontModel.getIndividual(tir.getURI());
            Resource individual = ontModel.getResource(tir.getEntURI());
            Resource tab = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+tir.getTabId());
            updatePropertyResourceValue(tirInd, TAB_INDIVIDUALRELATION_INVOLVESTAB, tab);
            updatePropertyResourceValue(tirInd, TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, individual);
            updatePropertyNonNegativeIntValue(tirInd, DISPLAY_RANK, tir.getDisplayRank(), ontModel);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    private TabIndividualRelation tabIndividualRelationFromIndividual(com.hp.hpl.jena.ontology.Individual ind) {
        TabIndividualRelation tir = new TabIndividualRelation();
        tir.setURI(ind.getURI());
        try {
            tir.setEntURI(((Resource)ind.getPropertyValue(TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL)).getURI());
        } catch (Exception e) {/* TODO log */}
        try {
            //tir.setTabId(((Resource)ind.getPropertyValue(TAB_INDIVIDUALRELATION_INVOLVESTAB)).getURI());
        } catch (Exception e) {/* TODO log */}
        return tir;
    }

}
