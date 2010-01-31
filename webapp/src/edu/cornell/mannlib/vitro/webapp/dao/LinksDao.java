/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;

import java.util.List;

public interface LinksDao {

    public abstract Link getLinkByURI(String URI);

    /**
     * inserts a new Link
     * @param link
     * @return URI of inserted link; otherwise null
     */
    public abstract String insertNewLink(Link link);

    public abstract void updateLink(Link link);

    public abstract void deleteLink(Link link);

    public abstract void addLinksToIndividual(Individual entity);
    
    public abstract void addPrimaryLinkToIndividual(Individual entity);

    public abstract void addLinksToIndividualsInObjectPropertyStatement(List /* of ObjectPropertyStatement*/objPropertyStmts);

}