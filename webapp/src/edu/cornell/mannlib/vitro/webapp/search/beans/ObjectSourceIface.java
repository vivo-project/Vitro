/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.util.Iterator;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

public interface ObjectSourceIface {

    Iterator<Individual> getAllOfThisTypeIterator();

    Iterator<Individual> getUpdatedSinceIterator(long msSinceEpoc);

}
