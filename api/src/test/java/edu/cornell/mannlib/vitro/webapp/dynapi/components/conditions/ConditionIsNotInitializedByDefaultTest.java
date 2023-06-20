package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ServletContextTest;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.StringParam;

public class ConditionIsNotInitializedByDefaultTest extends ServletContextTest{

   @Test
   public void testConditionIsNotInitilaizedByDefault() {
       DataStore store = new DataStore();
       Parameter testParam = new StringParam("param name");
       Data testData = new Data(testParam);
       testData.setRawString("test value");
       store.addData(testParam.getName(), testData);
       Condition c = new ConditionIsNotInitializedByDefault();
       c.getInputParams().add(testParam);
       assertTrue(c.isSatisfied(store));
       testData.initializeDefault();
       assertFalse(c.isSatisfied(store));
   }
}
