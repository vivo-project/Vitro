/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.Arrays;
import java.util.List;

import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.comparison.ComparisonFunctors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class BaseFilteringTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFilterMethods(){        
        List numbers = Arrays.asList( 1,2,3,4,5,6,7,8,9,10 );
        UnaryFunctor<Integer,Boolean> greaterThan3 =
            ComparisonFunctors.greater(3);
        
        BaseFiltering b = new BaseFiltering();
        List filteredNum = b.filter(numbers,greaterThan3);
        Assert.assertNotNull(filteredNum);
        Assert.assertTrue("expected 7 found "+filteredNum.size() , filteredNum.size() == 7);
    }
}
