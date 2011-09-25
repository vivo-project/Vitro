/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

/**
 * This will attempt to run System.gc() once a night. 
 * 
 * @author bdc34
 *
 */
public class NightlyDefragement implements ServletContextListener, Runnable {
    
    private static NightlyDefragement nightlyDefragement = null;
    private static boolean stop = false;
    private static final Log log = LogFactory.getLog(NightlyDefragement.class);
            
    protected DateTime lastRun;
    
    @Override
    public void run() {
        while( ! stop ){
            DateTime now = new DateTime();
            
            if( now.hourOfDay().get() > 0 
                 && now.hourOfDay().get() < 2 
                 && lastRun.isBefore( now.minusHours(22) ) ){ 
                
                log.info("running defragement");
                long start = System.currentTimeMillis();
                System.gc();
                log.info("Finished defragement, " + (start - System.currentTimeMillis()) + "msec");
                lastRun = now;
            }
            
            try{       
                synchronized( nightlyDefragement ){
                    this.wait(30*60*1000); //30 min;
                }
            }catch( InterruptedException ex){
                log.debug("woken up");                
            }
        }  
        log.info(" Stopping NightlyDefragement thread.");
    }

    
    public void stopNicely(){
        stop = true;
        synchronized( nightlyDefragement ){
            nightlyDefragement.notifyAll();
        }
    }
    
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        lastRun = new DateTime().minusHours( 400 );
        if( nightlyDefragement != null ){
            log.warn("NightlyDefragement listener has already been setup. Check your web.xml for duplicate listeners.");            
        }else{        
            nightlyDefragement = this;
            Thread thread = new Thread(this , "nightlyDefragementThread");                
            thread.start();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        nightlyDefragement.stopNicely();
    }

    
}
