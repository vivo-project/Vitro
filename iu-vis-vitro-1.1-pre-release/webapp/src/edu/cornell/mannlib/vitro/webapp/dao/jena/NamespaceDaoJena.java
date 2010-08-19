/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import edu.cornell.mannlib.vitro.webapp.beans.Namespace;
import edu.cornell.mannlib.vitro.webapp.dao.NamespaceDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

public class NamespaceDaoJena extends JenaBaseDao implements NamespaceDao {

    public NamespaceDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    public void deleteNamespace(int namespaceId) {
        // should be deprecated
    }

    public void deleteNamespace(Namespace namespace) {
        // may not need/want to implement this
    }

    public List<Namespace> getAllNamespaces() {
        // TODO Auto-generated method stub
        return null;
    }

    public Namespace getNamespaceById(int namespaceId) {
        // should be deprecated
        return null;
    }

    public int insertNewNamespace(Namespace namespace) {
        // may not need/want to implement this
        return 0;
    }

    public void updateNamespace(Namespace ont) {
        // may not need/want to implement this
    }

}
