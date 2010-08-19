/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

/**
 * This interface is to be implemented by classes that want to know
 * when entities change.  A class that imeplements this will be able
 * to register with classes that change entities.
 *
 * These can be thought of as callbacks.
 *
 * @author bdc34
 *
 */
public interface EntityChangeListener {
    public void entityAdded(String entityURI );
    public void entityDeleted(String entityURI);
    public void entityUpdated(String entityURI);
}
