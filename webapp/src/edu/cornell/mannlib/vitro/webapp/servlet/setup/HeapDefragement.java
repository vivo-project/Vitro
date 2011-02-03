/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class will request a full garbage collection when 
 * contextInitialized() is called.  The goal is to eliminate fragmentation
 * in the tenured generation and avoid problems with the 'young generation guarantee.'
 * 
 * This should be the last listener before the context starts.
 * 
 * See http://blogs.sun.com/jonthecollector/entry/when_the_sum_of_the (retrieved 2010-10-18)
 * 
 * @author bdc34
 *
 */
public class HeapDefragement implements ServletContextListener {
    private static final Log log = LogFactory.getLog(HeapDefragement.class);
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        
        if (AbortStartup.isStartupAborted(arg0.getServletContext())) {
            return;
        }
        
        try{
            log.info("Calling System.gc() to defragement the heap.");
            long start = System.currentTimeMillis();
            System.gc();           
            log.info("GC took " + (System.currentTimeMillis() - start) + " msec");
        }catch(Exception ex){
            log.error(ex,ex);            
        }                
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //do nothing        
    }
}
