/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 4:33:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PortalDao {
    Portal getPortal(int id );

    @SuppressWarnings("unchecked")
    Collection<Portal> getAllPortals();

    void deletePortal(Portal portal);

    void deletePortal(int portalId);
    
    int insertPortal(Portal portal) throws InsertException;

    void updatePortal(Portal portal);

    public Portal getPortalByURI(String uri);
    
    public boolean isSinglePortal();
    
}
