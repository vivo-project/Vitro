/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import edu.cornell.mannlib.vitro.webapp.beans.TabVClassRelation;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.TabVClassRelationDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TabVClassRelationDaoJena extends JenaBaseDao implements TabVClassRelationDao {

    public TabVClassRelationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
    public void deleteTabVClassRelation( TabVClassRelation t2t ) {
    	deleteTabs2Types(t2t,getOntModel());
    }

    public void deleteTabs2Types( TabVClassRelation t2t, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Property typeLinkedToTab = ontModel.getProperty(VitroVocabulary.TAB_AUTOLINKEDTOTAB);
            Resource type = ontModel.getResource(t2t.getVClassURI());
            Resource tab = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+t2t.getTabId());
            if ((typeLinkedToTab != null) && (tab != null) && (type != null)) {
                ontModel.removeAll(type, typeLinkedToTab, tab);
            } else {
                log.error("No good - something was null");
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public int insertTabVClassRelation( TabVClassRelation t2t ) {
    	insertNewTabs2Types(t2t,getOntModel());
        return 0;
    }

    public void insertNewTabs2Types( TabVClassRelation t2t, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Property typeLinkedToTab = ontModel.getProperty(VitroVocabulary.TAB_AUTOLINKEDTOTAB);
            Resource type = ontModel.getResource(t2t.getVClassURI());
            Resource tab = ontModel.getResource(DEFAULT_NAMESPACE+"tab"+t2t.getTabId());
            if ((typeLinkedToTab != null) && (tab != null) && (type != null)) {
                ontModel.add(type, typeLinkedToTab, tab);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }


}
