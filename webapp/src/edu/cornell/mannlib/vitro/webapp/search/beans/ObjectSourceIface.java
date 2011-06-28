/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.Iterator;

public interface ObjectSourceIface {

    Iterator<String> getAllOfThisTypeIterator();

    Iterator<String> getUpdatedSinceIterator(long msSinceEpoc);

}
