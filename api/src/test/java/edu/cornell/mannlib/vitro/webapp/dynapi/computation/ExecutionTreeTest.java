package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.ConditionalStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.NullStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.OperationalStep;


public class ExecutionTreeTest {
  
   NullStep exit = NullStep.getInstance();
    
   @Test
   public void testOperationGraphActionEmpty() {
       Procedure action = new Procedure();
       ExecutionTree graph = new ExecutionTree(action);
       
       assertEquals(1, graph.getNextOf(action).size());
       assertEquals(exit, graph.getNextOf(action).get(0));
       assertEquals(0, graph.getPrevOf(action).size());
   }

   
   @Test
   public void testOperationGraphOneStep() {
       Procedure action = new Procedure();
       OperationalStep step = new OperationalStep();
       action.setStep(step);
       ExecutionTree graph = new ExecutionTree(action);

       assertEquals(1, graph.getNextOf(action).size());
       assertEquals(step, graph.getNextOf(action).get(0));
       assertEquals(1, graph.getPrevOf(exit).size());
       assertEquals(step, graph.getPrevOf(exit).get(0));
   }

   @Test
   public void testOperationGraphTwoSteps() {
       Procedure action = new Procedure();
       OperationalStep step1 = new OperationalStep();
       action.setStep(step1);
       OperationalStep step2 = addNextStep(step1);
       ExecutionTree graph = new ExecutionTree(action);
       assertEquals(1, graph.getNextOf(step2).size());
       assertEquals(exit, graph.getNextOf(step2).get(0));

   }
    
   @Test
   public void testOperationGraphConditional() {
       Procedure action = new Procedure();
       ConditionalStep conditionalStep = new ConditionalStep();
       action.setStep(conditionalStep);

       OperationalStep operationStep1 = new OperationalStep();
       OperationalStep operationStep2 = new OperationalStep();

       conditionalStep.setNextIfNotSatisfied(operationStep1);
       conditionalStep.setNextIfSatisfied(operationStep2);

       ExecutionTree graph = new ExecutionTree(action);

       assertEquals(2, graph.getPrevOf(exit).size());
       assertEquals(2, graph.getNextOf(conditionalStep).size());

       assertTrue(graph.getPrevOf(exit).contains(operationStep1));
       assertTrue(graph.getPrevOf(exit).contains(operationStep2));
   }
   
   
   //TODO: Such configurations are invalid and should be rejected at the validation stage
   
   @Test
   public void testOperationGraphOneStepLoop() {
       Procedure action = new Procedure();
       OperationalStep step = new OperationalStep();
       action.setStep(step);
       step.setNextStep(step);
       ExecutionTree graph = new ExecutionTree(action);

       assertEquals(0, graph.getPrevOf(exit).size());
   }

   private OperationalStep addNextStep(OperationalStep step1) {
       OperationalStep step2 = new OperationalStep();
       step1.setNextStep(step2);
       return step2;
   }
}
