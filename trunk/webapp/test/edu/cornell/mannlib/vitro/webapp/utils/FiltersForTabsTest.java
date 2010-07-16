/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import net.sf.jga.fn.UnaryFunctor;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FiltersForTabs;

/**
 * User: bdc34
 * Date: Dec 13, 2007
 * Time: 2:47:03 PM
 */
public class FiltersForTabsTest {

    @Before
    public void setup(){
    }

    @Test
    public void testTabCollegeFiltering(){
        String flags [] =  {"CALS","BOAK","FUNGORK","MACAWI"};
        UnaryFunctor<Individual,Boolean> testFn =
                FiltersForTabs.getCollegeFilter(flags);
        Assert.assertTrue ( testFn!=null);

        IndividualImpl ind = new IndividualImpl();
        Assert.assertTrue(testFn.fn(ind) == false);

        ind.setFlag2Set("BOAK");
        Assert.assertTrue( testFn.fn(ind) == true);

        ind.setFlag2Set("");
       Assert.assertTrue(testFn.fn(ind) == false);

        ind.setFlag2Set("CALS,BOAK,FUNGORK");
        Assert.assertTrue(testFn.fn(ind) == true); 

        ind.setFlag2Set("FINKLY,HAPTO,FOOTOP");
        Assert.assertTrue(testFn.fn(ind) == false);

        ind.setFlag2Set("FINKLY,HAPTO,FOOTOP,CALS");
        Assert.assertTrue(testFn.fn(ind) == true);
    }

    @Test
    public  void testCollegeFilterCreation(){
        Tab tab = new Tab();
        tab.setFlag2Set("CALS,BOAK,FUNGORK");
        tab.setPortalId(7);

        UnaryFunctor<Individual,Boolean> testFn =
                FiltersForTabs.getFilterForTab(tab, new Portal());
        Assert.assertTrue ( testFn!=null);


        IndividualImpl ind = new IndividualImpl();
        Assert.assertFalse(  testFn.fn( ind) );

        ind.setFlag2Set("CALS");
        ind.setFlag1Numeric((int)FlagMathUtils.portalId2Numeric( tab.getPortalId() ));

        DateTime dt = new DateTime();
        ind.setSunrise(dt.minusDays(1000).toDate());
        ind.setSunset(dt.plusDays(1000).toDate());
        Assert.assertTrue( testFn.fn( ind));

        tab.setFlag2Mode("OMIT");        
        testFn = FiltersForTabs.getFilterForTab(tab, new Portal());

        Assert.assertFalse( testFn.fn(ind));

    }
}
