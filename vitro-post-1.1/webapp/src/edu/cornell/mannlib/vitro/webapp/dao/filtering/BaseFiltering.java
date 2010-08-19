/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseFiltering {

//    public Collection<DataProperty> filter(Collection<DataProperty> cin,
//                                            UnaryFunctor<DataProperty,Boolean> test){
//        ArrayList<DataProperty> cout = new ArrayList<DataProperty>();
//        Filter.filter(cin,test,cout);
//        return cout;
//    }

    public <T> Collection<T> filter(Collection<? extends T> cin,
                                                 UnaryFunctor<T,Boolean> test){
        if( cin == null ) return new ArrayList<T>(0);
        ArrayList<T> cout = new ArrayList<T>();
        Filter.filter(cin,test,cout);
        return cout;
    }


    public <T> List<T> filter(List<? extends T> cin,
                               UnaryFunctor<T,Boolean> test){
        if( cin == null ) return new ArrayList<T>(0);

        ArrayList<T> cout = new ArrayList<T>();
        Filter.filter(cin,test,cout);
        return cout;
    }
}
