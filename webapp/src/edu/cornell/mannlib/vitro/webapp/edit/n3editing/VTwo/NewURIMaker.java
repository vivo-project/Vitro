/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;

/**
 * Interface for an object that returns a new, unused URI, 
 * in the default namespace or in a specified namespace. 
 *
 */
public interface NewURIMaker {
    /**
     * @param prefixURI - may be null, the use the default namespace. If it is 
     * not null, then it is used as prefix of the new URI, a random integer may be added. 
     * @return a URI that is not currently in use by the system.
     * @throws InsertException 
     */
    String getUnusedNewURI( String prefixURI ) throws InsertException;    
}
