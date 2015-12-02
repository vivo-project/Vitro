/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.*;


public class IterableAdaptor <T> implements Iterable<T> {
    private final Enumeration<T> en;
    private final Iterator<T> it;
    
    /** sometimes you have an Enumeration and you want an Iterable */
    public IterableAdaptor(Enumeration<T> en) {
        this.en = en;
        this.it = null;
    }
    
    /** sometimes you have an Iterator but you want to use a for */
    public IterableAdaptor(Iterator <T> it){
        this.it = it;
        this.en = null;
    }
    
    // return an adaptor for the Enumeration
    public Iterator<T> iterator() {
        if( en != null ){
            return new Iterator<T>() {
                public boolean hasNext() {
                    return en.hasMoreElements();
                }
                public T next() {
                    return en.nextElement();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else if( it != null ){
            return it;
        } else {
            return new Iterator<T>() {
                public boolean hasNext() { return false; }                
                public T next() { return null; }
                public void remove() { throw new UnsupportedOperationException(); }
            };
        }
        
    }
    
    
    public static <T> Iterable<T> adapt(Enumeration<T> enin) {
        return new IterableAdaptor<T>(enin);
    }
    

    public static <T> Iterable<T> adapt(Iterator<T> itin) {
        return new IterableAdaptor<T>(itin);
    }
}

