package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */


import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WebappDaoFilteringPortalTest {
    String[][] entsDef = {
            // entity name            flag 1 numeric
            {"0",  "only in portal 1",        Long.toString( portalId2Numeric(1) )},
            {"1",  "In portal 1 and 2",       Long.toString( portalId2Numeric(1) + portalId2Numeric(2) )},
            {"2",  "in portal 3",             Long.toString( portalId2Numeric(3) )},
            {"3",  "checked into no portal",  "0"},
            {"4",  "checked into portal 4",   Long.toString( portalId2Numeric(4) )},
            {"5",  "checked into 1 and 4",    Long.toString( portalId2Numeric(1) + portalId2Numeric(4) )},
            {"6",  "checked into 4",          Long.toString( portalId2Numeric(4) )},
            {"7",  "checked into 5",          Long.toString( portalId2Numeric(5) )},            
            
    };
    
    List <Individual> ents = new ArrayList (10);
    
    @Before
    public void setUp() throws Exception {
        //we need something that makes classes
        for(String entA[] : entsDef){
            Individual ent = new IndividualImpl();
            ent.setName("Item " + entA[0] + ": " + entA[1]);            
            ent.setFlag1Numeric( Integer.parseInt(entA[2]) );
            ents.add(ent);
        }
    }

    private long portalId2Numeric(long i) {
        return FlagMathUtils.portalId2Numeric( i);
    }

    @Test
    public void testPortal(){
        //Is it in portal 2 (aka numeric portal 4, in vivo 'life sci' ) ?
        PortalFlag f = new PortalFlag( 2 );
        VitroFilters vf = VitroFilterUtils.getFilterFromPortalFlag(f);
        Assert.assertNotNull( vf );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(0) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(1) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(2) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(3) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(4) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(5) ) );
        
        //Is it in portal 1 (aka numeric portal 2, in vivo 'vivo portal' ) ?
        f = new PortalFlag(1);
        vf = VitroFilterUtils.getFilterFromPortalFlag(f);
        Assert.assertNotNull( vf );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(0) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(1) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(2) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(3) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(4) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(5) ) );
    }
    
    @Test
    public void testAllPorta(){
        VitroFilters vf = VitroFilterUtils.getCalsPortalFilter();
        Assert.assertNotNull( vf );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(0) ) );
        Assert.assertTrue( vf.getIndividualFilter().fn( ents.get(1) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(2) ) );
        Assert.assertFalse( vf.getIndividualFilter().fn( ents.get(3) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(4) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(5) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(6) ) );
        Assert.assertTrue(  vf.getIndividualFilter().fn( ents.get(7) ) );
    }
}
