/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

/**
 *
 */
public class Classes2ClassesDaoJena extends JenaBaseDao implements Classes2ClassesDao {
	
    public Classes2ClassesDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    public void deleteClasses2Classes( Classes2Classes c2c ) {
    	deleteClasses2Classes(c2c, getOntModelSelector().getTBoxModel());
    }

    public void deleteClasses2Classes( Classes2Classes c2c, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            OntResource subclass = getOntClass(ontModel,c2c.getSubclassURI());
            OntResource superclass = getOntClass(ontModel,c2c.getSuperclassURI());
            if ((subclass != null) && (superclass != null)) {
                ontModel.removeAll(subclass, RDFS.subClassOf, superclass);
            }
            if (subclass.isAnon()) {
            	smartRemove(subclass, getOntModel());
            }
            if (superclass.isAnon()) {
            	smartRemove(superclass, getOntModel());
            }
        } finally {
        	ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
            ontModel.leaveCriticalSection();
        }
    }

    public void insertNewClasses2Classes( Classes2Classes c2c ) {
    	insertNewClasses2Classes(c2c, getOntModelSelector().getTBoxModel());
    }

    public void insertNewClasses2Classes( Classes2Classes c2c, OntModel ontModel )
    {
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            Resource subclass = ontModel.getResource(c2c.getSubclassURI());
            Resource superclass = ontModel.getResource(c2c.getSuperclassURI());
            if ((subclass != null) && (superclass != null)) {
                ontModel.add(subclass, RDFS.subClassOf, superclass);
            }
        } finally {
        	ontModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
            ontModel.leaveCriticalSection();
        }
    }
}
