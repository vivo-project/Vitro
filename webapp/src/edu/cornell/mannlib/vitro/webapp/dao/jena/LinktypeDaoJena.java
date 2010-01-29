package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Linktype;
import edu.cornell.mannlib.vitro.webapp.dao.LinktypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class LinktypeDaoJena extends JenaBaseDao implements LinktypeDao {

    public LinktypeDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getABoxModel();
    }
    
    public List<Linktype> getAllLinktypes() {
        List<Linktype> linkTypes = new LinkedList<Linktype>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
           OntClass linkClass = getOntModel().getOntClass(LINK.getURI());
           ClosableIterator cIt = linkClass.listSubClasses();
           try {
               while (cIt.hasNext()) {
                   OntClass lCls = (OntClass) cIt.next();
                   Linktype lt = new Linktype();
                   lt.setURI(lCls.getURI());
                   lt.setType(getLabelOrId(lCls));
                   linkTypes.add(lt);
               }
           } finally {
               cIt.close();
           }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return linkTypes;
    }

}
