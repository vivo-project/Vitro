/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IndividualURIQueue<E> implements Queue<E> {

    private ConcurrentLinkedQueue<E> q = new ConcurrentLinkedQueue<E>();
    private ConcurrentHashMap<E, Boolean> m = new ConcurrentHashMap<E, Boolean>();
    
    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            if(!m.containsKey(e)) {
                m.put(e, Boolean.TRUE);
                q.add(e);
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public synchronized void clear() {
        m.clear();
        q.clear();
    }
    
    @Override
    public boolean contains(Object o) {
        return m.contains(o);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        boolean contains = true;
        for(Object e : c) {
            contains |= m.contains(e);
        }
        return contains;
    }
    
    @Override
    public boolean isEmpty() {
        return q.isEmpty();
    }
    
    @Override
    public Iterator<E> iterator() {
        return q.iterator();
    }
    
    @Override
    public synchronized boolean remove(Object o) {
        m.remove(o);
        return q.remove(o);
    }
    
    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        for (Object e : c) {
            m.remove(e);
        }
        return q.removeAll(c);
    }
    
    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<E> it = m.keySet().iterator();
        while(it.hasNext()) {
            E e = it.next();
            if(!c.contains(e)) {
               m.remove(e);
               q.remove(e);
               changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public int size() {
        return m.size();
    }
    
    @Override
    public Object[] toArray() {
        return q.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return q.toArray(a);
    }
    
    @Override
    public synchronized boolean add(E e) {
        if(m.containsKey(e)) {
            return false;
        } else {
            m.put(e, Boolean.TRUE);
            q.add(e);
            return true;
        }
    }
    
    @Override
    public E element() {
        return q.element();
    }
    
    @Override
    public boolean offer(E e) {
        return q.offer(e);
    }
    
    @Override
    public E peek() {
        return q.peek();
    }
    
    @Override
    public synchronized E poll() {
        E e =  q.poll();
        m.remove(e);
        return e;
    }
    
    @Override
    public synchronized E remove() {
        E e = q.remove();
        m.remove(e);
        return e;
    }
    
}