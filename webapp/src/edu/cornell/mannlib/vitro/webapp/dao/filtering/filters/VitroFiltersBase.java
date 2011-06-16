/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import net.sf.jga.fn.adaptor.AdaptorFunctors;

public abstract class VitroFiltersBase implements VitroFilters {


    public VitroFilters and(VitroFilters other){
        if( other == null )
            return this;
        else
            return new VitroFiltersImpl(
             AdaptorFunctors.and(this.getIndividualFilter(), other.getIndividualFilter()),
             AdaptorFunctors.and(this.getDataPropertyFilter(), other.getDataPropertyFilter()),
             AdaptorFunctors.and(this.getObjectPropertyFilter(),other.getObjectPropertyFilter()),
             AdaptorFunctors.and(this.getDataPropertyStatementFilter(),other.getDataPropertyStatementFilter()),
             AdaptorFunctors.and(this.getObjectPropertyStatementFilter(),other.getObjectPropertyStatementFilter()),
             AdaptorFunctors.and(this.getClassFilter(),other.getClassFilter()),
             AdaptorFunctors.and(this.getVClassGroupFilter(),other.getVClassGroupFilter()),
             AdaptorFunctors.and(this.getPropertyGroupFilter(), other.getPropertyGroupFilter())
            );
    }
}
