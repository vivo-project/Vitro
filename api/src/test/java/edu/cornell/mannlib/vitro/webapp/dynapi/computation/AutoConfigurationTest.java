package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationalStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.SPARQLQuery;

public class AutoConfigurationTest {
    @Test
    public void testActionRequirements1() {
        Action action = new Action();
        OperationalStep step1 = new OperationalStep();
        OperationalStep step2 = new OperationalStep();
        OperationalStep step3 = new OperationalStep();

        action.setStep(step1);
        step1.setNextStep(step2);
        step2.setNextStep(step3);
        
        step1.setOperation(query(arr("A","B"), arr("C")));
        step2.setOperation(query(arr("B","C","E"), arr("D")));
        step3.setOperation(query(arr("D","F","G"), arr("D")));

        assertEquals(0, action.getRequiredParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(5, action.getRequiredParams().size());
        assertTrue(action.getRequiredParams().contains("A"));
        assertTrue(action.getRequiredParams().contains("B"));
        assertTrue(action.getRequiredParams().contains("E"));
        assertTrue(action.getRequiredParams().contains("F"));
        assertTrue(action.getRequiredParams().contains("G"));
    }

    @Test
    public void testActionRequirements2() {
        Action action = new Action();
        OperationalStep step1 = new OperationalStep();
        OperationalStep step2 = new OperationalStep();

        action.setStep(step1);
        step1.setNextStep(step2);

        step1.setOperation(query(arr(), arr("A")));
        step2.setOperation(query(arr("A"), arr()));

        assertEquals(0, action.getRequiredParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(0, action.getRequiredParams().size());
    }
    
    private String[] arr(String... str) {
        return str;
        
    }
    
    private SPARQLQuery query(String[] required, String[] provided) {
        SPARQLQuery query = new SPARQLQuery();
        for (int i = 0; i < required.length; i++) {
            query.addRequiredParameter(param(required[i]));
        }
        for (int i = 0; i < provided.length; i++) {
            query.addProvidedParameter(param(provided[i]));
        }
        return query;
    }

    private Parameter param(String name) {
        Parameter param = new Parameter();
        param.setName(name);
        return param;
    }
}
