/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

/** 
 * Indicates who the user is and what roles/groups they belong to.
 * The objects returned by this could be anything.  For example, RoleBacedPolicy
 * looks for RoleBacedPolicy.AuthRole objects.
 *  
 * This is a marker interface to indicate that a object is an identifier,
 * implementations of Identifier may provide any sort of identifying functionality or
 * methods.  
 * 
 * <h3>Justification for a methodless interface</h3>
 * This is better than using Object since having method signatures that have
 * Identifier at least indicates the intent of the parameter, even if it is the 
 * same to the compiler.
 * 
 * Policy objects are expected to examine the IdentiferBundle to find the
 * information needed to make a decision.  There is no set pattern as to
 * what will and will not be a configuration of Identifiers that will create
 * a AUTHORIZED decision.  Reflection, Pattern Matching or something similar 
 * will be needed.  
 * 
 * We have no compile time information about what will structures will map 
 * to which Authorization, let's not pretend that we do.
 */
public interface Identifier {

}
