/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.util.List;

/** 
 * A group of Identifiers, very commonly used in method signatures
 * since a session will usually have more than one associated identifier.
*/
public interface IdentifierBundle extends List <Identifier>{
    /* this is just typed List, and just barely. */
}
