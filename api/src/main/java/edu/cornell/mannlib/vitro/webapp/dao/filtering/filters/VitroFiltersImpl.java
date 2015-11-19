/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.adaptor.AdaptorFunctors;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.User;

/**
 * A object to hold all the filters commonly used by the vitro webapp.
 *
 * @author bdc34
 *
 */
public class VitroFiltersImpl extends VitroFiltersBase {

    /* *************** filters used by Webapp and Core ******************* */
    /** filter for vitro.bean.Individual objects */
    UnaryFunctor<Individual,Boolean>              individualFilter;

   /** filter for DataProperty objects */
    UnaryFunctor<DataProperty,Boolean>            dataPropertyFilter;
    /** filter for ObjectProperty objects */
    UnaryFunctor<ObjectProperty,Boolean>          objectPropertyFilter;

    /** filter for DataPropertyStatement objects */
    UnaryFunctor<DataPropertyStatement,Boolean>   dataPropertyStatementFilter;
    /** filter for ObjectPropertyStatement objects */
    UnaryFunctor<ObjectPropertyStatement,Boolean> objectPropertyStatementFilter;

    /** filter for VClass objects */
    UnaryFunctor<VClass,Boolean>                  classFilter;

    /* *************** filters only used by Webapp ******************* */
    /** filter for VClassGroup objects */
    UnaryFunctor<VClassGroup,Boolean>          vClassGroupFilter;

    /** fitler for PropertyGroup objects */
    UnaryFunctor<PropertyGroup, Boolean>      propertyGroupFilter;

    public final static UnaryFunctor FILTER_OUT_NOTHING =
        AdaptorFunctors.constantUnary(Boolean.TRUE);

    /**
     * Builds a filter that does no filtering.
     */
    @SuppressWarnings("unchecked")
    public VitroFiltersImpl(){
        individualFilter = FILTER_OUT_NOTHING;
        dataPropertyFilter = FILTER_OUT_NOTHING;
        objectPropertyFilter = FILTER_OUT_NOTHING;
        dataPropertyStatementFilter  = FILTER_OUT_NOTHING;
        objectPropertyStatementFilter= FILTER_OUT_NOTHING;
        classFilter= FILTER_OUT_NOTHING;
        vClassGroupFilter = FILTER_OUT_NOTHING;
        propertyGroupFilter = FILTER_OUT_NOTHING;
    }

    public VitroFiltersImpl(
            UnaryFunctor<Individual, Boolean> individualFilter,
            UnaryFunctor<DataProperty, Boolean> dataPropertyFilter,
            UnaryFunctor<ObjectProperty, Boolean> objectPropertyFilter,
            UnaryFunctor<DataPropertyStatement, Boolean> dataPropertyStatementFilter,
            UnaryFunctor<ObjectPropertyStatement, Boolean> objectPropertyStatementFilter,
            UnaryFunctor<VClass, Boolean> classFilter,
            UnaryFunctor<VClassGroup, Boolean> classGroupFilter,
            UnaryFunctor<PropertyGroup,Boolean>propertyGroupFilter) {
        super();
        this.individualFilter = individualFilter;
        this.dataPropertyFilter = dataPropertyFilter;
        this.objectPropertyFilter = objectPropertyFilter;
        this.dataPropertyStatementFilter = dataPropertyStatementFilter;
        this.objectPropertyStatementFilter = objectPropertyStatementFilter;
        this.classFilter = classFilter;
        vClassGroupFilter = classGroupFilter;
        this.propertyGroupFilter = propertyGroupFilter;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getIndividualFilter()
     */
    public UnaryFunctor<Individual, Boolean> getIndividualFilter() {
        return individualFilter;
    }

    public VitroFilters setIndividualFilter(
            UnaryFunctor<Individual, Boolean> individualFilter) {
        this.individualFilter = individualFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getDataPropertyFilter()
     */
    public UnaryFunctor<DataProperty, Boolean> getDataPropertyFilter() {
        return dataPropertyFilter;
    }

    public VitroFilters setDataPropertyFilter(
            UnaryFunctor<DataProperty, Boolean> dataPropertyFilter) {
        this.dataPropertyFilter = dataPropertyFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getObjectPropertyFilter()
     */
    public UnaryFunctor<ObjectProperty, Boolean> getObjectPropertyFilter() {
        return objectPropertyFilter;
    }

    public VitroFilters setObjectPropertyFilter(
            UnaryFunctor<ObjectProperty, Boolean> objectPropertyFilter) {
        this.objectPropertyFilter = objectPropertyFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getDataPropertyStatementFilter()
     */
    public UnaryFunctor<DataPropertyStatement, Boolean> getDataPropertyStatementFilter() {
        return dataPropertyStatementFilter;
    }

    public VitroFilters setDataPropertyStatementFilter(
            UnaryFunctor<DataPropertyStatement, Boolean> dataPropertyStatementFilter) {
        this.dataPropertyStatementFilter = dataPropertyStatementFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getObjectPropertyStatementFilter()
     */
    public UnaryFunctor<ObjectPropertyStatement, Boolean> getObjectPropertyStatementFilter() {
        return objectPropertyStatementFilter;
    }

    public VitroFilters setObjectPropertyStatementFilter(
            UnaryFunctor<ObjectPropertyStatement, Boolean> objectPropertyStatementFilter) {
        this.objectPropertyStatementFilter = objectPropertyStatementFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getClassFilter()
     */
    public UnaryFunctor<VClass, Boolean> getClassFilter() {
        return classFilter;
    }

    public VitroFilters setClassFilter(UnaryFunctor<VClass, Boolean> classFilter) {
        this.classFilter = classFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.dao.filtering.VitroFilters#getVClassGroupFilter()
     */
    public UnaryFunctor<VClassGroup, Boolean> getVClassGroupFilter() {
        return vClassGroupFilter;
    }

    public VitroFilters setVClassGroupFilter(
            UnaryFunctor<VClassGroup, Boolean> classGroupFilter) {
        vClassGroupFilter = classGroupFilter;
        return this;
    }

    public UnaryFunctor<PropertyGroup, Boolean> getPropertyGroupFilter() {
        return propertyGroupFilter;
    }

    public void setPropertyGroupFilter(
            UnaryFunctor<PropertyGroup, Boolean> propertyGroupFilter) {
        this.propertyGroupFilter = propertyGroupFilter;
    }


}
