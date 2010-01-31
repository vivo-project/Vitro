/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.Date;

import net.sf.jga.fn.UnaryFunctor;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;


public class FiltersTest {

    Boolean ACCEPTED = Boolean.TRUE;
    Boolean REJECTED = Boolean.FALSE;
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testTimeFiltersForFutureEvents(){
        Tab tab = new Tab();
        tab.setDayLimit( 10 );
        UnaryFunctor<Individual,Boolean> filter =
                FiltersForTabs.getTimeFilter(tab, new DateTime());

        Individual ind = new IndividualImpl();
        DateTime timekey;

        // Allow a slight fudge factor for the time it takes the tests to run.
        DateTime now = new DateTime().plusSeconds(1);

        for(int i=1; i < 100 ; i++){
            timekey = now.minusDays(i);
            ind.setTimekey( timekey.toDate() );
            Assert.assertTrue("minus " + i + " days should Reject", 
            		filter.fn( ind ) == REJECTED);
        }

        for(int i=0; i< 10 ; i++){
            timekey = now.plusDays(i);
            ind.setTimekey( timekey.toDate() );
            Assert.assertTrue("plus " + i + " days should Accept", 
            		filter.fn( ind ) == ACCEPTED);
        }

        timekey = now.plusDays( 10 );
        ind.setTimekey( timekey.toDate() );
        Assert.assertTrue("plus 10 days should Reject",
        		filter.fn( ind ) == REJECTED);

        for(int i=10; i < 1000 ; i++){
            timekey = now.plusDays(i);
            ind.setTimekey( timekey.toDate() );
            Assert.assertTrue("plus " + i + " days should Reject", 
            		filter.fn( ind ) == REJECTED);
        }
    }

    @Test
    public void testTimeFiltersForPastReleases(){
        Tab tab = new Tab();
        tab.setDayLimit( -10 );
        UnaryFunctor<Individual,Boolean> filter =
                FiltersForTabs.getTimeFilter(tab, new DateTime());

        Individual ind = new IndividualImpl();
        DateTime sunrise;
        
        // Allow a slight fudge factor for the time it takes the tests to run.
        DateTime now = new DateTime().plusSeconds(1);
        
        for(int i=1; i < 1000 ; i++){
            sunrise = now.plusDays(i);
            ind.setSunrise( sunrise.toDate() );
            Assert.assertTrue("plus " + i + " days should Reject", 
            		filter.fn( ind ) == REJECTED);
        }

        ind.setSunrise(  now.minusMinutes(20).toDate() );
        Assert.assertTrue("minus 20 minutes should Accept", 
        		filter.fn( ind ) == ACCEPTED);

        for(int i=1; i <= 10 ; i++){
            sunrise = now.minusDays(i);
            ind.setSunrise( sunrise.toDate() );
            Assert.assertTrue("minus " + i + " days should Accept", 
            		filter.fn( ind ) == ACCEPTED);
        }

        for(int i=11; i < 100 ; i++){
            sunrise = now.minusDays(i);
            ind.setSunrise( sunrise.toDate() );
            Assert.assertTrue("minus " + i + " days should Reject", 
            		filter.fn( ind ) == REJECTED);
        }
    }

    @Test
    public void testMarkowitzCase(){
    	DateTime now = new DateTime().withTime(0, 0, 0, 0);
    	Date sunrise = now.minusDays(1).toDate();
    	Date timeKey = now.plusDays(2).toDate();
    	
        Tab tab = new Tab();
        tab.setDayLimit( -10 );
        UnaryFunctor<Individual,Boolean> filter =
                FiltersForTabs.getTimeFilter(tab, new DateTime());

        Individual ind = new IndividualImpl();
        ind.setSunrise( sunrise );
        ind.setTimekey( timeKey );

        Assert.assertTrue("Should accept with day limit -10",
        		filter.fn( ind ) == ACCEPTED);

        tab.setDayLimit( 10 );
        filter = FiltersForTabs.getTimeFilter(tab, new DateTime());

        Assert.assertTrue("Should accept with day limit +10", 
        		filter.fn( ind ) == ACCEPTED );
    }


}
