/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.sys;

import static java.lang.String.format;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable;

/** A policy that checks, but does not enforce, single writer or multiple writer locking policy */ 
public class DatasetControlMRSW implements DatasetControl
{
    private final boolean concurrencyChecking = true ;
    private final AtomicLong epoch = new AtomicLong(5) ;                // Update counters, used to check iterators. No need to start at 0.
    private final AtomicLong readCounter = new AtomicLong(0) ;
    private final AtomicLong writeCounter = new AtomicLong(0) ;
    
    public DatasetControlMRSW()
    { }

    @Override
    public synchronized void startRead()
    {
        readCounter.getAndIncrement() ;
        recordReadIncrement();
        checkConcurrency() ;
    }

	@Override
    public synchronized void finishRead()
    {
        readCounter.decrementAndGet() ;
        recordReadDecrement();
    }

    @Override
    public synchronized void startUpdate()
    {
        epoch.getAndIncrement() ;
        writeCounter.getAndIncrement() ;
        recordWriteIncrement();
        checkConcurrency() ;
    }

    @Override
    public synchronized void finishUpdate()
    {
        writeCounter.decrementAndGet() ;
        recordWriteDecrement();
    }

    private void checkConcurrency()
    {
        long R, W ;
        synchronized (this)
        {
            R = readCounter.get() ;
            W = writeCounter.get() ;
        }

        if ( R > 0 && W > 0 )
        	reportReadWithWriteError();
            policyError(R, W) ;
        if ( W > 1 )
        	reportMultipleWriteError();
            policyError(R, W) ;
    }
    
    

	@Override
    public <T> Iterator<T> iteratorControl(Iterator<T> iter) { return new IteratorCheckNotConcurrent<T>(iter, epoch) ; }
    
    private static class IteratorCheckNotConcurrent<T> implements Iterator<T>, Closeable
    {
        private Iterator<T> iter ;
        private AtomicLong eCount ;
        private boolean finished = false ;
        private long startEpoch ; 

        IteratorCheckNotConcurrent(Iterator<T> iter, AtomicLong eCount )
        {
            // Assumes correct locking to set up, i.e. eCount not changing (writer on separate thread).
            this.iter = iter ;
            this.eCount = eCount ;
            this.startEpoch = eCount.get();
        }

        private void checkCourrentModification()
        {
            if ( finished )
                return ;
            
            long now = eCount.get() ;
            if ( now != startEpoch )
            {
                policyError(format("Iterator: started at %d, now %d", startEpoch, now)) ;

            }
        }
        
        @Override
        public boolean hasNext()
        {
            checkCourrentModification() ;
            boolean b = iter.hasNext() ;
            if ( ! b )
                close() ;
            return b ;
        }

        @Override
        public T next()
        {
            checkCourrentModification() ;
            try { 
                return iter.next();
            } catch (NoSuchElementException ex) { close() ; throw ex ; }
        }

        @Override
        public void remove()
        {
            checkCourrentModification() ;
            iter.remove() ;
        }

        @Override
        public void close()
        {
            finished = true ;
            Iter.close(iter) ;
        }
    }

    
    private static void policyError(long R, long W)
    {
        policyError(format("Reader = %d, Writer = %d", R, W)) ;
    }
    
    private static void policyError(String message)
    {
        throw new ConcurrentModificationException(message) ;
    }
    
    ///////////////////////////////////////////////////////
    // TODO
    ///////////////////////////////////////////////////////

    private Set<Throwable> readOpens = new HashSet<>();
    private Set<Throwable> readCloses = new HashSet<>();
    private Set<Throwable> writeOpens = new HashSet<>();
    private Set<Throwable> writeCloses = new HashSet<>();
    
	private void recordReadIncrement() {
		readOpens.add(new Throwable());
	}

	private void recordReadDecrement() {
		if (readCounter.get() == 0L) {
			readOpens.clear();
			readCloses.clear();
		} else {
			readCloses.add(new Throwable());
		}
	}
	
	private void recordWriteIncrement() {
		writeOpens.add(new Throwable());
	}

	private void recordWriteDecrement() {
		if (writeCounter.get() == 0L) {
			writeOpens.clear();
			writeCloses.clear();
		} else {
			writeCloses.add(new Throwable());
		}
	}
	
	private void reportReadWithWriteError() {
		dumpem("ReadOpens", readOpens);
		dumpem("ReadCloses", readCloses);
		dumpem("WriteOpens", writeOpens);
		dumpem("WriteCloses", writeCloses);
	}

	private void reportMultipleWriteError() {
		dumpem("WriteOpens", writeOpens);
		dumpem("WriteCloses", writeCloses);
	}

	private void dumpem(String label, Set<Throwable> set) {
		System.out.println("----------" + label + ": " + set.size());
		for (Throwable t : set) {
			t.printStackTrace(System.out);
		}
	}

}
