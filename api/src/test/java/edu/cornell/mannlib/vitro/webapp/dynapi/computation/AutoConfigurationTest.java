/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SparqlSelectQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.ConditionalStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.OperationalStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.RDFType;
import org.junit.Test;

public class AutoConfigurationTest {
    @Test
    public void testActionRequirements1() throws InitializationException {
        Procedure action = new Procedure();
        OperationalStep step1 = new OperationalStep();
        OperationalStep step2 = new OperationalStep();
        OperationalStep step3 = new OperationalStep();

        action.setStep(step1);
        step1.setNextStep(step2);
        step2.setNextStep(step3);

        step1.setOperation(query(arr("A", "B"), arr("C")));
        step2.setOperation(query(arr("B", "C", "E"), arr("D")));
        step3.setOperation(query(arr("D", "F", "G"), arr("D")));

        assertEquals(0, action.getInputParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(5, action.getInputParams().size());
        assertTrue(action.getInputParams().contains("A"));
        assertTrue(action.getInputParams().contains("B"));
        assertTrue(action.getInputParams().contains("E"));
        assertTrue(action.getInputParams().contains("F"));
        assertTrue(action.getInputParams().contains("G"));
    }

    @Test
    public void testActionRequirements2() throws InitializationException {
        Procedure action = new Procedure();
        OperationalStep step1 = new OperationalStep();
        OperationalStep step2 = new OperationalStep();

        action.setStep(step1);
        step1.setNextStep(step2);

        step1.setOperation(query(arr(), arr("A")));
        step2.setOperation(query(arr("A"), arr()));

        assertEquals(0, action.getInputParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(0, action.getInputParams().size());
    }

    @Test
    public void testConditionalInActionRequirements() throws InitializationException {
        Procedure action = new Procedure();
        ConditionalStep step1 = new ConditionalStep();
        OperationalStep step2 = new OperationalStep();
        OperationalStep step3 = new OperationalStep();

        action.setStep(step1);

        step1.setNextIfSatisfied(step2);
        step1.setNextIfNotSatisfied(step3);
        step2.setOperation(query(arr("B"), arr("A")));
        step3.setOperation(query(arr("A"), arr("B")));

        assertEquals(0, action.getInputParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(2, action.getInputParams().size());
        assertTrue(action.getInputParams().contains("A"));
        assertTrue(action.getInputParams().contains("B"));

    }

    @Test
    public void testConditionalWithNullStepInActionRequirements() throws InitializationException {
        Procedure action = new Procedure();
        action.addProvidedParameter(param("A"));
        ConditionalStep step1 = new ConditionalStep();
        OperationalStep step2 = new OperationalStep();

        action.setStep(step1);
        step1.setNextIfNotSatisfied(step2);
        step2.setOperation(query(arr("B"), arr("A")));

        assertEquals(0, action.getInputParams().size());
        AutoConfiguration.computeParams(action);
        assertEquals(2, action.getInputParams().size());
        assertTrue(action.getInputParams().contains("A"));
        assertTrue(action.getInputParams().contains("B"));

    }

    private String[] arr(String... str) {
        return str;

    }

    private SparqlSelectQuery query(String[] required, String[] provided) throws InitializationException {
        SparqlSelectQuery query = new SparqlSelectQuery();
        for (int i = 0; i < required.length; i++) {
            query.addInputParameter(param(required[i]));
        }
        for (int i = 0; i < provided.length; i++) {
            query.addOutputParameter(param(provided[i]));
        }
        return query;
    }

    private Parameter param(String name) throws InitializationException {
        Parameter param = new Parameter();
        ParameterType uri1ParamType = new ParameterType();
        DataFormat uri1ImplType = new DefaultFormat();

        ConversionConfiguration config = new ConversionConfiguration();

        try {
            config.setClassName("java.lang.String");
            config.setMethodArguments("");
            config.setMethodName("toString");
            config.setStaticMethod(false);
            uri1ImplType.setDeserializationConfig(config);
            uri1ImplType.setSerializationConfig(config);
            uri1ParamType.addInterface("java.lang.String");

            RDFType rdfType = new RDFType();
            rdfType.setName("anyURI");
            uri1ParamType.setRdfType(rdfType);

            uri1ParamType.addFormat(uri1ImplType);
            param.setType(uri1ParamType);
            param.setName(name);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return param;

    }
}
