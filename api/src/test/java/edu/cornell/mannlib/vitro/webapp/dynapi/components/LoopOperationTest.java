package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class LoopOperationTest {

    @Test
    public void testRun() {
        LoopOperation lo = new LoopOperation();
        OperationResult opResult = lo.run(null);
        //assertFalse(opResult.hasError());
    }
}
