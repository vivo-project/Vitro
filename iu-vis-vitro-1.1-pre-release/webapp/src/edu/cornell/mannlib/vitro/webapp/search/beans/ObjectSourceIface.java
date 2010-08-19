/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.Date;
import java.util.Iterator;

public interface ObjectSourceIface {

    Iterator getAllOfThisTypeIterator();

    Iterator getUpdatedSinceIterator(long msSinceEpoc);

}
