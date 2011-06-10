/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Summarize;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.BaseFiltering;

public class VitroFiltersFactoryTest {

    @Before
    public void setUp() throws Exception {
    }

//    @Test
//    public void testSunsetWindowFilterOnListOfEnts() {
//        DateTime easyDate = new org.joda.time.DateTime(2005,1,1,0,0,0,0); //2005-01-01        
//        Date givenDate = easyDate.toDate();
//        
//        VitroFilters vf = FiltersForTabs.getSunsetWindowFilter(givenDate);
//        Assert.assertNotNull(vf);
//        checkFilterForNull(vf);
//        
//        List<Individual> ents = new LinkedList();
//        for( int i = 0; i< 10; i++){
//            Individual ent = new IndividualImpl();
//            ent.setSunrise( null );
//            ent.setSunset(  null );
//            ents.add(ent);        
//        }
//        
//        BaseFiltering bf = new BaseFiltering();
//        List filtered = bf.filter(ents,vf.getIndividualFilter());
//        Assert.assertNotNull(filtered);
//        Assert.assertTrue("expcted to filter no entities, filtered out " + (10-filtered.size()), 
//                filtered.size() == 10);
//        
//        Individual ent = new IndividualImpl();
//        ent.setSunrise( easyDate.minusDays(3).toDate() );
//        ent.setSunset( easyDate.plusDays(3).toDate() );
//        ents.add(ent);
//        
//        filtered = bf.filter(ents,vf.getIndividualFilter());
//        Assert.assertNotNull(filtered);
//        Assert.assertTrue("expcted to filter no entities, filtered out " + (11-filtered.size()), 
//                filtered.size() == 11);
//        
//        ent = new IndividualImpl();
//        ent.setSunrise( easyDate.minusDays(100).toDate() );
//        ent.setSunset( easyDate.minusDays(110).toDate() );
//        ents.add(ent);
//        
//        filtered = bf.filter(ents,vf.getIndividualFilter());
//        Assert.assertNotNull(filtered);
//        Assert.assertTrue("expcted to filter one entity, filtered out " + (12-filtered.size()), 
//                filtered.size() == 11);
//        
//        long count = Summarize.count(ents,vf.getIndividualFilter());
//        Assert.assertTrue("expected 12, got " + ents.size(), ents.size() == 12);
//        Assert.assertTrue("expected count of 11, got " + count , count == 11);
//        
//        long a = 20000;
//        int b = (int)a;
//        Assert.assertTrue( b == 20000);
//    }
    
//    @Test
//    public void testGetSunsetWindowFilter() {
//        DateTime easyDate = new org.joda.time.DateTime(2005,1,1,0,0,0,0); //2005-01-01        
//        Date givenDate = easyDate.toDate();
//        
//        VitroFilters vf = FiltersForTabs.getSunsetWindowFilter(givenDate);
//        Assert.assertNotNull(vf);
//        checkFilterForNull(vf);
//        
//        Individual ent = new IndividualImpl();
//        ent.setSunrise( easyDate.minusDays(3).toDate() );
//        ent.setSunset( easyDate.plusDays(3).toDate() );
//        Assert.assertTrue(vf.getIndividualFilter().fn( ent ) );
//                
//        ent.setSunrise( easyDate.toDate() );
//        Assert.assertTrue("items should be not filtered on first sunrise day", vf.getIndividualFilter().fn(ent));
//        
//        ent.setSunrise( easyDate.minusDays(3).toDate() );
//        ent.setSunset( easyDate.minusDays( 2 ).toDate() );
//        Assert.assertFalse("should be sunset and filtered out", vf.getIndividualFilter().fn( ent ));
//        
//        ent.setSunrise( easyDate.plusDays(3).toDate() );
//        ent.setSunset( easyDate.plusDays( 10 ).toDate() );
//        Assert.assertFalse("should not yet be sunrised and filtered out", vf.getIndividualFilter().fn( ent ));
//
//        ent.setSunrise( null  );
//        ent.setSunset( null  );
//        Assert.assertTrue("nulls should not throw exceptions and and not be filtered out", vf.getIndividualFilter().fn( ent ));
//        
//        //should work with webapp too
//        Individual entwa = new IndividualImpl();
//        entwa.setSunrise( easyDate.minusDays(3).toDate() );
//        entwa.setSunset( easyDate.plusDays(3).toDate() );
//        Assert.assertTrue(vf.getIndividualFilter().fn( entwa ) );
//                
//        entwa.setSunrise( easyDate.toDate() );
//        Assert.assertTrue("items should be not filtered on first sunrise day", vf.getIndividualFilter().fn(entwa));
//        
//        entwa.setSunrise( easyDate.minusDays(3).toDate() );
//        entwa.setSunset( easyDate.minusDays( 2 ).toDate() );
//        Assert.assertFalse("should be sunset and filtered out", vf.getIndividualFilter().fn( entwa ));
//        
//        entwa.setSunrise( easyDate.plusDays(3).toDate() );
//        entwa.setSunset( easyDate.plusDays( 10 ).toDate() );
//        Assert.assertFalse("should not yet be sunrised and filtered out", vf.getIndividualFilter().fn( entwa ));
//
//        entwa.setSunrise( null  );
//        entwa.setSunset( null );
//        Assert.assertTrue("null should not throw exceptions and should not be filtered out", vf.getIndividualFilter().fn( entwa ));
//
//        //ObjectPropertyStatements
//        ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
//        ops.setObject(entwa);
//                
//        entwa.setSunrise( easyDate.minusDays(3).toDate() );
//        entwa.setSunset( easyDate.plusDays(3).toDate() );
//        Assert.assertTrue(vf.getIndividualFilter().fn( entwa ) );
//                
//        entwa.setSunrise( easyDate.toDate() );
//        Assert.assertTrue("items should be not filtered on first sunrise day", vf.getObjectPropertyStatementFilter().fn(ops));
//        
//        entwa.setSunrise( easyDate.minusDays(3).toDate() );
//        entwa.setSunset( easyDate.minusDays( 2 ).toDate() );
//        Assert.assertFalse("should be sunset and filtered out", vf.getObjectPropertyStatementFilter().fn(ops));
//        
//        entwa.setSunrise( easyDate.plusDays(3).toDate() );
//        entwa.setSunset( easyDate.plusDays( 10 ).toDate() );
//        Assert.assertFalse("should not yet be sunrised and filtered out", vf.getObjectPropertyStatementFilter().fn(ops));
//
//        entwa.setSunrise( null  );
//        entwa.setSunset( null );
//        Assert.assertTrue("null should not throw exceptions and should not be filtered out", vf.getObjectPropertyStatementFilter().fn(ops));
//        
//        ops.setSunrise( null );
//        ops.setSunset(  null );
//        Assert.assertTrue("null should not throw exceptions and should not be filtered out", vf.getObjectPropertyStatementFilter().fn( ops ) );                       
//
//        //DataPropertyStatements
//        DataPropertyStatement dps = new DataPropertyStatementImpl();
//        dps.setSunrise( easyDate.minusDays(3).toDate() );
//        dps.setSunset(  easyDate.plusDays( 3).toDate() );
//        Assert.assertTrue( vf.getDataPropertyStatementFilter().fn( dps ) );
//
//        dps.setSunrise( easyDate.toDate() );
//        dps.setSunset(  easyDate.plusDays( 3).toDate() );
//        Assert.assertTrue( vf.getDataPropertyStatementFilter().fn( dps ) );
//
//        dps.setSunrise( null );
//        dps.setSunset(  null );
//        Assert.assertTrue("should be not throw exceptions and should not be filtered out", vf.getDataPropertyStatementFilter().fn( dps ) );                       
//
//    }

    @Test
    public void testGetTestFilter() {
        VitroFilters vf = VitroFilterUtils.getTestFilter();
        checkFilterForNull(vf);
        ArrayList<Individual> ents = new ArrayList<Individual>();
        
        String[] names = {"Greg", "gary", "bob", "Sue", "jim" };
        for( String name : names){
            Individual ent = new IndividualImpl();
            ent.setName(name);
            ents.add(ent);            
        }
        
        BaseFiltering bf = new BaseFiltering();
        List<Individual> filteredEnts = bf.filter(ents,vf.getIndividualFilter());
        Assert.assertNotNull(filteredEnts);
        Assert.assertEquals("did not filter correctly", 2, filteredEnts.size());        
    }         
    
    public void checkFilterForNull(VitroFilters vf){
        Assert.assertNotNull("filter was null", vf);
        Assert.assertNotNull("getClassFilter was null", vf.getClassFilter());
        Assert.assertNotNull("getDataPropertyFilter was null", vf.getDataPropertyFilter());
        Assert.assertNotNull("getDataPropertyStatementFilter was null", vf.getDataPropertyStatementFilter());
        Assert.assertNotNull("getObjectPropertyFilter was null", vf.getObjectPropertyFilter());
        Assert.assertNotNull("getObjectPropertyStatementFilter was null", vf.getObjectPropertyStatementFilter());
        Assert.assertNotNull("getIndividualFilter was null", vf.getIndividualFilter());
        Assert.assertNotNull("getVClassGroupFilter was null", vf.getVClassGroupFilter());
    }
    
    @Test 
    public void testRoleLevelFilter(){
        
    }
    
    
    private boolean ACCEPT= true;
    private boolean REJECT= false;
    
}
