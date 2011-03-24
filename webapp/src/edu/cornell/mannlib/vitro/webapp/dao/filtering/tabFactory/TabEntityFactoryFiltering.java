package edu.cornell.mannlib.vitro.webapp.dao.filtering.tabFactory;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.BaseFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FiltersForTabs;
import net.sf.jga.algorithms.*;
import net.sf.jga.fn.UnaryFunctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TabEntityFactoryFiltering extends BaseFiltering implements TabEntityFactory {

    TabEntityFactory _innerTabFactory;
    UnaryFunctor<Individual,Boolean>  _entityFilter;
    List<Individual> _innerEnts;
    List<Individual> _filteredEnts;
    UnaryFunctor<List<Individual>,List<Individual>> _sorting;

    public TabEntityFactoryFiltering(TabEntityFactory factory,
            UnaryFunctor<Individual, Boolean> filter,
            UnaryFunctor<List<Individual>,List<Individual>> sorting) {
        super();
        _innerTabFactory = factory;
        _entityFilter = filter;
        _sorting = sorting;
    }

    @SuppressWarnings("unchecked")
    public List getLettersOfEnts() {
        ArrayList out = new ArrayList();
        List<Individual> ents = getInnerEnts();
        filter(ents,_entityFilter);
        return FiltersForTabs.getLettersOfEnts( ents);
    }

    /** Get entities from innerTabFactory filtered by entityFilter. */
    @SuppressWarnings("unchecked")
    public List getRelatedEntites(String alpha) {
         List<Individual> ents = _innerTabFactory.getRelatedEntites( alpha );
        if( ents == null ) return Collections.EMPTY_LIST;


        List<Individual> outEnts = new LinkedList<Individual>();
        if(_entityFilter != null)
            Filter.filter( ents , _entityFilter , outEnts);
        if( _sorting != null )
            outEnts = _sorting.fn( outEnts );

        if( alpha != null && ! "all".equals(alpha))
            filter(outEnts, FiltersForTabs.getAlphaFilter(alpha));

        return outEnts;
    }

    public int getRelatedEntityCount() {
        return (int)Summarize.count(getInnerEnts(), _entityFilter);
    }

    @SuppressWarnings("unchecked")
    protected List<Individual> getInnerEnts(){
        if( _innerEnts == null )
            _innerEnts = _innerTabFactory.getRelatedEntites("all");
        return _innerEnts;
    }

    public String toString(){
        return "TabEntityFactoryFiltering: " + (_entityFilter == null ? "null" : _entityFilter.toString()) +
                " innerDao: " + (_innerTabFactory == null ? "null" : _innerTabFactory.toString());
    }
}
