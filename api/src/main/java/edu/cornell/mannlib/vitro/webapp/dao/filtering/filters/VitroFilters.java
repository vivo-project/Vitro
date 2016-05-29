/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import net.sf.jga.fn.UnaryFunctor;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public interface VitroFilters {

    public VitroFilters and(VitroFilters other);

    public UnaryFunctor<Individual, Boolean> getIndividualFilter();

    public UnaryFunctor<DataProperty, Boolean> getDataPropertyFilter();

    public UnaryFunctor<ObjectProperty, Boolean> getObjectPropertyFilter();

    public UnaryFunctor<DataPropertyStatement, Boolean> getDataPropertyStatementFilter();

    public UnaryFunctor<ObjectPropertyStatement, Boolean> getObjectPropertyStatementFilter();

    public UnaryFunctor<VClass, Boolean> getClassFilter();

    public UnaryFunctor<VClassGroup, Boolean> getVClassGroupFilter();
    
    public UnaryFunctor<PropertyGroup, Boolean> getPropertyGroupFilter();

}