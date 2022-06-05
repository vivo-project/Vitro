package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;

public class ConditionIsNotEmptyTest {

    @Test
    public void testIsEmpty() {
        ConditionIsNotEmpty condition = new ConditionIsNotEmpty();
        
        Parameter a = new Parameter();
        a.setName("a");
        condition.addRequiredParameter(a);

        Parameter b = new Parameter();
        b.setName("b");
        condition.addRequiredParameter(b);
        
    }
}
